package com.example.administrator.downloadtest;

import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


class DownloadTask extends AsyncTask<String, Integer, Integer> {

    private static final int TYPE_SUCCESS = 0;
    private static final int TYPE_FAILED = 1;
    private static final int TYPE_PAUSED = 2;
    private static final int TYPE_CANCELED = 3;

    private DownloadListener listener;

    private long doneLength;

    private int progress;

    private boolean isPaused;
    private boolean isCanceled;

    DownloadTask(DownloadListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (isCanceled | isPaused) return;
        listener.onStart();
    }

    @Override
    protected Integer doInBackground(String... params) {
        String url = params[0];
        if (TextUtils.isEmpty(url)) return TYPE_FAILED;
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        String name = url.substring(url.lastIndexOf("/"));
        File file = new File(path + name);
        if (file.exists()) {
            doneLength = file.length();
        }
        if (isCanceled) {
            file.delete();
            return TYPE_CANCELED;
        }
        InputStream in = null;
        RandomAccessFile out = null;
        try {
            long totalLength = getTotalLength(url);
            if (totalLength == -1) {
                return TYPE_FAILED;
            } else if (totalLength == doneLength) {
                return TYPE_SUCCESS;
            }
            OkHttpClient client = new OkHttpClient.Builder().build();
            Request request = new Request.Builder().addHeader("Range", "bytes=" + doneLength + "-").url(url).build();
            Response response = client.newCall(request).execute();
            if (response == null || !response.isSuccessful()) return TYPE_FAILED;
            in = response.body().byteStream();
            out = new RandomAccessFile(file, "rw");
            out.seek(doneLength);
            byte[] b = new byte[1024];
            int total = 0;
            int len;
            while (!isPaused && !isCanceled && (len = in.read(b)) != -1) {
                total += len;
                out.write(b, 0, len);
                int progress = (int) ((doneLength + total) * 100 / totalLength);
                publishProgress(progress);
            }
            response.body().close();
            if (isPaused) return TYPE_PAUSED;
            else if (isCanceled) {
                file.delete();
                return TYPE_CANCELED;
            } else return TYPE_SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (values[0] > progress) {
            listener.onProgress(values[0]);
            progress = values[0];
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        switch (integer) {
            case TYPE_PAUSED:
                listener.onPaused();
                break;
            case TYPE_CANCELED:
                listener.onCanceled();
                break;
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
        }
    }

    void pause() {
        isPaused = true;
    }

    void cancel() {
        isCanceled = true;
    }

    private long getTotalLength(String url) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long totalLength = response.body().contentLength();
            response.close();
            return totalLength;
        }
        return -1;
    }

}
