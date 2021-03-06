package de.unikl.hci.abbas.physiometrics.TouchAuth.Service;

/**
 * Created by abbas on 12/31/17.
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View.OnTouchListener;

import de.unikl.hci.abbas.physiometrics.R;
import de.unikl.hci.abbas.physiometrics.Demo.MainActivity;
import de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel.TouchAuth;
import de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel.TouchFeatureExtraction;
import de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel.TouchEvent;
import de.unikl.hci.abbas.physiometrics.TouchAuth.Util.FileUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TouchDataCollectingService extends Service {
    // Get External Storage Directory & the filename of raw data and features
    public final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auth/Touch/";
    public final String rawFilename = dir + "touch.txt";
    public final String clickFeatureFilename = dir + "click_features.txt";
    public final String slideFeatureFilename = dir + "slide_features.txt";
    protected FileUtils fileUtils;

    public TouchDataCollectingService() {
    }

    public static void collect(PostEventMethod postEvent) {
        try {
            // Kill the previous getEvent process
            try {
                String[] cmd = {
                        "/system/bin/sh",
                        "-c",
                        "ps | grep getevent | awk \'{print $2}\' | xargs su am kill"
                };
                Process p = Runtime.getRuntime().exec(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Start Collection
            String[] cmd = {"su", "-c", "getevent -t /dev/input/event5"};
            //String[] cmd = {"su chmod 666 /dev/input/event5", "su -c getevent -t /dev/input/event5"};
            //String[] cmd = {"/system/bin/sh", "-c", "ps | getevent -t /storage/emulated/0/Auth/Touch"};

            Process p = Runtime.getRuntime().exec(cmd);
            InputStream is = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String s;
            int i = 0;
            StringBuilder sb = new StringBuilder();
            while (((s = reader.readLine()) != null)) {
                String line = reader.readLine();
                sb.append(line).append("\n");
                ++i;
                if (i >= 10) {
                    TouchEvent event = TouchEvent.getTouchEventFromString(sb.toString());
                    if (event != null) {
                        postEvent.setParam(event, sb);
                        postEvent.call();
                        sb.delete(event.start, event.end);
                        i -= 10;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            collect(postEvent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /*
        String[] commands = new String[8];
        for (int i = 0; i < commands.length; i++) {
            commands[i] = "chmod 777 /dev/input/event" + i + "\n";
        }
        fileUtils.rootPermission(commands);
        */
        improvePriority();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                collect(new PostEventMethod() {

                    @Override
                    public Void call() throws Exception {
                        double[] features = TouchFeatureExtraction.extract(this.event);
                        String raw = this.sb.substring(this.event.start, this.event.end);
                        FileUtils.writeFile(rawFilename, raw, true);
                        if (features.length < 5) {
                            FileUtils.writeFileFromNums(clickFeatureFilename, features, true, false, 1);
                        } else {
                            FileUtils.writeFileFromNums(slideFeatureFilename, features, true, false, 1);
                        }
                        return null;
                    }
                });
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    private void improvePriority() {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Touch Auth")
                .setContentText("Data Collecting Service Started.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        notification.contentIntent = contentIntent;
        startForeground(1, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    static class PostEventMethod {

        TouchEvent event;
        StringBuilder sb;

        private TouchEvent getEvent() {
            return event;
        }

        private StringBuilder getSb() {
            return sb;
        }

        void setParam(TouchEvent event, StringBuilder sb) {
            this.event = event;
            this.sb = sb;
        }

        public Void call() throws Exception {
            return null;
        }
    }
}
