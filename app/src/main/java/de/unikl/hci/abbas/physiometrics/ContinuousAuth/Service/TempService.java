package de.unikl.hci.abbas.physiometrics.ContinuousAuth.Service;

/**
 * Created by abbas on 1/1/18.
 */

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import de.unikl.hci.abbas.physiometrics.ContinuousAuth.Activity.AuthenticationActivity;

public class TempService extends IntentService {

    Handler handler = new Handler();
    private int timeRemain = 30;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (timeRemain == 0) {
                Intent intent = new Intent();
                intent.setClass(TempService.this, AuthenticationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Message message = new Message();
                message.what = 1;
                handlerStop.sendMessage(message);
            }
            handler.postDelayed(this, 1000);
            timeRemain--;
        }
    };
    final Handler handlerStop = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                timeRemain = 0;
                handler.removeCallbacks(runnable);
            }
            super.handleMessage(msg);
        }
    };

    public TempService() {
        super("TempService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        timeRemain = 30;
        handler.postDelayed(runnable, 1000);
    }
}
