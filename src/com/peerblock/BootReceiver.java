package com.peerblock;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class BootReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent bootIntent)
	{
		// Check if enabled
		if (!Utils.isXposedEnabled())
		{
			// Create Xposed installer intent
			Intent xInstallerIntent = context.getPackageManager().getLaunchIntentForPackage("de.robv.android.xposed.installer");
			if (xInstallerIntent != null)
				xInstallerIntent.putExtra("opentab", 1);

			PendingIntent pi = (xInstallerIntent == null ? null : PendingIntent.getActivity(context, 0,
					xInstallerIntent, PendingIntent.FLAG_UPDATE_CURRENT));

			// Build notification
			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
			notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
			notificationBuilder.setContentTitle(context.getString(R.string.app_name));
			notificationBuilder.setContentText("PeerBlock For Android is not enabled in XPosed");
			notificationBuilder.setWhen(System.currentTimeMillis());
			notificationBuilder.setAutoCancel(true);
			if (pi != null)
				notificationBuilder.setContentIntent(pi);
			Notification notification = notificationBuilder.build();

			// Display notification
			NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(0, notification);
		}
	}
}