package com.peerblock;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class PeerReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		MessageHandler handler = new MessageHandler(context, true);
		handler.HandleMessage(intent, context);
		
		/*intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		intent.setAction("com.peerblock.PeerBlockReceiver");
		intent.putExtra("derp","lol");
		context.sendBroadcast(intent);*/
		
		
	}
}