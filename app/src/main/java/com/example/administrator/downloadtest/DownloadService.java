package com.example.administrator.downloadtest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class DownloadService extends Service implements DownloadListener {

    private DownloadTask task;
    private int progress;

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadIBinder();
    }

    @Override
    public void onStart() {
        Toast.makeText(this, "下载开始", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPaused() {
        task = null;
        Toast.makeText(this, "下载暂停", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCanceled() {
        task = null;
        progress = 0;
        stopForeground(true);
        Toast.makeText(this, "下载取消", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess() {
        task = null;
        stopForeground(true);
        showNotification("下载完成");
        Toast.makeText(this, "下载完成", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailed() {
        task = null;
        stopForeground(true);
        showNotification("下载失败");
        Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProgress(int progress) {
        this.progress = progress;
        showNotification("下载中");
        Log.i("nico", progress + "");
    }

    private Notification getNotification(String title) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(title);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setProgress(100, progress, false);
        builder.setContentText("王者荣耀");
        builder.setContentInfo(progress + "%");
        return builder.build();
    }


    private void showNotification(String title) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, getNotification(title));
    }

    class DownloadIBinder extends Binder {

        private String url;

        void startDownload(String url) {
            if (task == null) {
                this.url = url;
                task = new DownloadTask(DownloadService.this);
                task.execute(url);
                startForeground(1, getNotification("开始下载"));
            }
        }

        void pauseDownload() {
            if (task != null) task.pause();
        }

        void cancelDownload() {
            if (task != null) task.cancel();
            else {
                task = new DownloadTask(DownloadService.this);
                task.cancel();
                task.execute(url);
            }
        }
    }
}
