package com.example.administrator.downloadtest;

public interface DownloadListener {

    void onStart();

    void onPaused();

    void onCanceled();

    void onSuccess();

    void onFailed();

    void onProgress(int progress);

}
