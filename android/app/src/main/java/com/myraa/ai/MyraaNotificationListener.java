package com.myraa.ai;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.content.Intent;

public class MyraaNotificationListener extends NotificationListenerService {
    private static volatile MyraaNotificationListener instance;
    @Override public void onListenerConnected() { instance = this; }
    @Override public void onListenerDisconnected() { if (instance == this) instance = null; }
    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        // The app can later consume this through the native bridge; no notification is modified or dismissed here.
    }
}
