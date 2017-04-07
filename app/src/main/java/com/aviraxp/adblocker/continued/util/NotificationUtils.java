package com.aviraxp.adblocker.continued.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.aviraxp.adblocker.continued.R;
import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.ui.SettingsActivity;

@SuppressWarnings("deprecation")
public class NotificationUtils {

    private static int timeCount = 1;

    public static void setNotify(Context ctx) {
        if (PreferencesHelper.isShowNotification()) {
            Intent postNotification = new Intent("AdBlocker.intent.action.POST_NOTIFICATION");
            postNotification.putExtra("description", ctx.getResources().getString(R.string.notification_des))
                    .putExtra("id", 42)
                    .putExtra("title", ctx.getResources().getString(R.string.notification));
            ctx.sendBroadcast(postNotification);
        }
    }

    public static void postNotification(String title, String description, final int id, Context ctx) {
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, new Intent(ctx, SettingsActivity.class), 0);
        Notification.Builder notification = new Notification.Builder(ctx)
                .setTicker(description)
                .setContentTitle(title)
                .setContentText(ctx.getResources().getString(R.string.notification_count) + String.valueOf(timeCount))
                .setSmallIcon(R.mipmap.ic_notification)
                .setContentIntent(pi)
                .setAutoCancel(true);
        timeCount = timeCount + 1;
        final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification.getNotification());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                notificationManager.cancel(id);
            }
        }, 5000);
    }
}
