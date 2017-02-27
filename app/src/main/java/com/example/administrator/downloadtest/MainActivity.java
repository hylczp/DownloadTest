package com.example.administrator.downloadtest;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button start;
    private Button pause;
    private Button cancel;

    private DownloadService.DownloadIBinder binder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (DownloadService.DownloadIBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = (Button) findViewById(R.id.button);
        pause = (Button) findViewById(R.id.button2);
        cancel = (Button) findViewById(R.id.button3);
        pause.setEnabled(false);
        cancel.setEnabled(false);
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);

        //aBranch
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                binder.startDownload("http://dlied5.myapp.com/myapp/1104466820/sgame/2017_com.tencent.tmgp.sgame_h100_1.17.1.15.apk");
                start.setEnabled(false);
                pause.setEnabled(true);
                cancel.setEnabled(true);
                break;
            case R.id.button2:
                binder.pauseDownload();
                pause.setEnabled(false);
                start.setEnabled(true);
                start.setText("继续");
                break;
            case R.id.button3:
                binder.cancelDownload();
                start.setEnabled(true);
                start.setText("开始");
                pause.setEnabled(false);
                cancel.setEnabled(false);
                break;
        }
    }

}
