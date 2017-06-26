package com.example.administrator.downloadtest;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/6/26.
 */
public class DownLoadTask extends AsyncTask<String,Integer,Integer> {
    private static final int TYPE_SUCCESS=0;
    private static final int TYPE_FAILED=1;
    private static final int TYPE_PAUSED=2;
    private static final int TYPE_CANCELED=3;
    private DownLoadListener listener;
    private boolean isCanceled = false;
    private boolean isPaused = false;
    private int lastProgress;

    public DownLoadTask(DownLoadListener listener) {
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(String... params) {
        InputStream is =null;
        RandomAccessFile saveFile=null;
        File file =null;
        long downloadedLength = 0;
        String downloadUrl = params[0];
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        file = new File(directory+fileName);
        if (file.exists()){
            downloadedLength =file.length();
        }
        long contentlength = getContentLength(downloadUrl);
        if (contentlength==0){
            return  TYPE_FAILED;
        }else if (contentlength==downloadedLength){
            return  TYPE_SUCCESS;
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().addHeader("RANGE","bytes="+downloadedLength+"-").url(downloadUrl).build();
        try {
            Response response = client.newCall(request).execute();
            if (response!=null){
                is = response.body().byteStream();
                saveFile =new RandomAccessFile(file,"rw");
                saveFile.seek(downloadedLength);
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len=is.read(b))!=-1){
                    if (isCanceled){
                        return  TYPE_CANCELED;
                    }else if (isPaused){
                        return  TYPE_PAUSED;
                    }else{
                        total+=len;
                        saveFile.write(b,0,len);
                        int progress = (int) ((total+downloadedLength)*100/contentlength);
                        publishProgress(progress);
                    }
                }

                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (is!=null){
                    is.close();
                }
                if (saveFile!=null){
                    saveFile.close();
                }
                if (isCanceled&&file!=null){
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return TYPE_FAILED;
    }

    private long getContentLength(String downloadUrl) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        try {
            Response response = client.newCall(request).execute();
            if (response!=null&&response.isSuccessful()){
                long contentLength = response.body().contentLength();
                response.body().close();
                return  contentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress>lastProgress){
            listener.onProgress(progress);
            lastProgress=progress;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer){
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_PAUSED:
                listener.onPaused();
                break;
            case TYPE_CANCELED:
                listener.onCanceled();
                default:
                break;
            }
    }

    public  void pauseDownload(){
        isPaused= true;
    }

    public  void cancelDownload(){
        isCanceled=true;
    }
}
