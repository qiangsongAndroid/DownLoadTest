package com.example.administrator.downloadtest;

/**
 * Created by Administrator on 2017/6/26.
 */
public interface DownLoadListener {
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPaused();
    void onCanceled();
}
