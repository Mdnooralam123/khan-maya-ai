package com.myraa.ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        // Do not auto-start microphone/camera work at boot. The user can explicitly enable the assistant.
    }
}
