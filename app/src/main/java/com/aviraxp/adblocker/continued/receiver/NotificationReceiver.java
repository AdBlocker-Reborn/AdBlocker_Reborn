package com.aviraxp.adblocker.continued.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.aviraxp.adblocker.continued.util.NotificationUtils;

public class NotificationReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        if (action.equals("AdBlocker.intent.action.POST_NOTIFICATION") && extras != null) {
            String description = extras.getString("description");
            int id = extras.getInt("id");
            String title = extras.getString("title");
            NotificationUtils.postNotification(title, description, id, context);
        }
    }
}
