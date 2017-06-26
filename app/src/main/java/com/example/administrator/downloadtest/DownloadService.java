package com.example.administrator.downloadtest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

public class DownloadService extends Service {
    private DownLoadTask downLoadTask;
    private String downloadUrl;
    public DownloadService() {
    }


    private DownLoadListener downLoadListener =new DownLoadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1,getNotification("Downloading.....",progress));
        }

        @Override
        public void onSuccess() {
               downLoadTask =null;
              stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Success",-1));
            Toast.makeText(DownloadService.this,"\"Download Success\"",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downLoadTask=null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Failed",-1));
            Toast.makeText(DownloadService.this,"Download Failed",Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onPaused() {
           downLoadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this,"Paused",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downLoadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this,"Canceled",Toast.LENGTH_SHORT).show();
        }
    };

     private  DownloadBinder binder =new DownloadBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    class DownloadBinder extends Binder{

        public void startDownload(String url){
            if (downLoadTask==null){
                downloadUrl=url;
                downLoadTask = new DownLoadTask(downLoadListener);
                downLoadTask.execute(downloadUrl);
                startForeground(1,getNotification("Downloading",0));
                Toast.makeText(DownloadService.this,"Downloading",Toast.LENGTH_SHORT).show();

            }
        }

        public void pauseDownload(){
            if (downLoadTask!=null){
                downLoadTask.pauseDownload();
            }
        }

        public  void cancelDownload(){
            if (downLoadTask!=null){
                downLoadTask.cancelDownload();
            }else {
                if (downloadUrl!=null){
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory+fileName);

                    if (file.exists()){
                        file.delete();
                    }

                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this,"Canceled",Toast.LENGTH_SHORT).show();

                }
            }
        }

      }

    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title,int progress){
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress>0){
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);
        }
        return  builder.build();
    }

}
