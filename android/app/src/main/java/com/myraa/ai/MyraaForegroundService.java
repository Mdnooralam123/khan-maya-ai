package com.myraa.ai;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Build;

public class MyraaForegroundService extends Service {
    private static final String CHANNEL = "myraa_background";
    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(new NotificationChannel(CHANNEL, "MYRAA background assistant", NotificationManager.IMPORTANCE_LOW));
        }
        Notification n = new Notification.Builder(this, CHANNEL)
            .setContentTitle("MYRAA is active")
            .setContentText("Background assistant is running")
            .setSmallIcon(android.R.drawable.ic_dialog_info).setOngoing(true).build();
        startForeground(152, n);
    }
    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override public IBinder onBind(Intent intent) { return null; }
}
