package de.unikl.hci.abbas.physiometrics.Demo;


import android.os.Bundle;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.DataOutputStream;

import de.unikl.hci.abbas.physiometrics.R;
import de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel.NGramModel;
import de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel.SVM;
import de.unikl.hci.abbas.physiometrics.TouchAuth.Service.AuthService;
import de.unikl.hci.abbas.physiometrics.TouchAuth.Service.SensorDataCollectingService;
import de.unikl.hci.abbas.physiometrics.TouchAuth.Service.SensorPredictingService;
import de.unikl.hci.abbas.physiometrics.TouchAuth.Service.TouchDataCollectingService;
import de.unikl.hci.abbas.physiometrics.TouchAuth.Service.TouchPredictingService;
import de.unikl.hci.abbas.physiometrics.TouchAuth.Util.FileUtils;
import de.unikl.hci.abbas.physiometrics.TouchAuth.Util.DataUtils;

public class MainActivity extends AppCompatActivity {
    public static SVM clickModel;
    public static SVM slideModel;

    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
            }, 1);
            return false;
        }
        return false;
    }

    public static boolean RootCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            Log.d("*** DEBUG ***", "ROOT REE" + e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        Log.d("*** DEBUG ***", "Root SUC ");
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        FileUtils.makeRootDirectory(dir + "/Auth/");
        FileUtils.makeRootDirectory(dir + "/Auth/Touch/");
        FileUtils.makeRootDirectory(dir + "/Auth/Sensor/");

        isGrantExternalRW(this);
    }

    public void onCollectStartButtonClick(View view) {
        try {
            stopService(new Intent(this, TouchPredictingService.class));
            stopService(new Intent(this, SensorPredictingService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Collecting Start...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setClass(this, TouchDataCollectingService.class);
        startService(intent);

        Intent intent1 = new Intent();
        intent1.setClass(this, SensorDataCollectingService.class);
        startService(intent1);
    }

    public void onTrainStartButtonClick(View view) {

        Toast.makeText(this, "Training Start...", Toast.LENGTH_SHORT).show();

        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auth/";
        double[][] clickFeatures = FileUtils.readFileToMatrix(dir + "Touch/click_features.txt");
        double[][] slideFeatures = FileUtils.readFileToMatrix(dir + "Touch/slide_features.txt");

        double[] clickLabel = new double[clickFeatures.length];
        for (int i = 0; i < clickLabel.length; ++i) {
            clickLabel[i] = 1;
        }

        double[] slideLabel = new double[slideFeatures.length];
        for (int i = 0; i < slideLabel.length; ++i) {
            slideLabel[i] = 1;
        }

        clickFeatures = DataUtils.cleanData(clickFeatures, true);
        clickFeatures = DataUtils.scaleData(clickFeatures, dir + "Touch/click_coefs.txt", false);

        slideFeatures = DataUtils.cleanData(slideFeatures, true);
        slideFeatures = DataUtils.scaleData(slideFeatures, dir + "Touch/slide_coefs.txt", false);

        clickModel = new SVM();
        clickModel.train(clickFeatures, clickLabel);

        slideModel = new SVM();
        slideModel.train(slideFeatures, slideLabel);

        String trainFvFilename = dir + "Sensor/FeatureVectors.txt";
        String modelFilename = dir + "Sensor/Model.txt";
        String centroidsFilename = dir + "Sensor/Centroids.txt";
        double[][] fv = FileUtils.readFileToMatrix(trainFvFilename);
        NGramModel model = new NGramModel(120, 2);
        model.train(fv);

        model.saveModel(modelFilename);
        model.saveCentroids(centroidsFilename);

        Toast.makeText(this, "Training End!", Toast.LENGTH_SHORT).show();
    }

    public void onTestStartButtonClick(View view) {

        Toast.makeText(this, " Start...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setClass(this, AuthService.class);
        startService(intent);
    }

    public void onStopButtonClick(View view) {

        // Kill the previous getevent process
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

        try {
            stopService(new Intent(this, TouchDataCollectingService.class));
            stopService(new Intent(this, SensorDataCollectingService.class));
            stopService(new Intent(this, TouchPredictingService.class));
            stopService(new Intent(this, SensorPredictingService.class));
            stopService(new Intent(this, AuthService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
