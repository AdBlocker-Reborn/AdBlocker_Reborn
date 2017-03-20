package com.aviraxp.adblocker.continued.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.aviraxp.adblocker.continued.R;
import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.ui.SettingsActivity;

@SuppressWarnings("deprecation")
public class NotificationUtils extends BroadcastReceiver {

    public static void setNotify(Context ctx) {
        if (PreferencesHelper.isShowNotification()) {
            Intent postNotification = new Intent("AdBlocker.intent.action.POST_NOTIFICATION");
            postNotification.putExtra("description", ctx.getResources().getString(R.string.notification_des))
                    .putExtra("id", 42)
                    .putExtra("title", ctx.getResources().getString(R.string.notification));
            ctx.sendBroadcast(postNotification);
        }
    }

    private void postNotification(String title, String description, int id, Context ctx) {
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, new Intent(ctx, SettingsActivity.class), 0);
        Notification.Builder notification = new Notification.Builder(ctx)
                .setTicker(description)
                .setContentTitle(title)
                .setContentText(description)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pi)
                .setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification.getNotification());
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        boolean hasExtras = extras != null;
        if (action.equals("AdBlocker.intent.action.POST_NOTIFICATION") && hasExtras) {
            String description = extras.getString("description");
            int id = extras.getInt("id");
            String title = extras.getString("title");
            postNotification(title, description, id, context);
        }
    }
}
