package com.google.ar.core.codelab.function;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.ar.core.codelab.Activities.NotificationActivity;

public class TimerEndedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("notification_message");
        int notificationId = intent.getIntExtra("notification_id", 0); // Default to 0 if not found
        Notificationhelper.sendNotification(context, NotificationActivity.class, "Remember to take image", message, notificationId);
    }
}

