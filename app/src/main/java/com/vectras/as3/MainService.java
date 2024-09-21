package com.vectras.as3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MainService extends Service {

    private static final String CHANNEL_ID = "AndStation3Channel";

    public static boolean isStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStarted = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("AndStation3 is running in the background...")
                        .setSmallIcon(R.drawable.ic_stat_main)
                        .setOngoing(true)
                        .build();

        startForeground(1, notification);

        isStarted = true;

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "AndStation3 Service Channel",
                            NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
