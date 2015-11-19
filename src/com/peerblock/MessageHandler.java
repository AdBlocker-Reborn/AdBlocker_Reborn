package com.peerblock;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class MessageHandler
{
	public static final int AD_BLOCKED = 1;
	
	private Context context;
	private long OwnUniqueId;
	private boolean ThisIsMain;
	
	public MessageHandler(Context context, boolean ThisIsMain)
	{
		this.context = context;
		this.OwnUniqueId = Utils.GetUniqueAppId(context);
		this.ThisIsMain = ThisIsMain;
	}
	
	public void SendMessage(byte[] data, long TargetId, int PacketId)
	{
		Intent intent = new Intent();
		intent.putExtra("PacketId", PacketId);
		intent.putExtra("ToMain", !ThisIsMain);
		intent.putExtra("TargetId", TargetId);
		intent.putExtra("FromId", OwnUniqueId);
		intent.putExtra("data", data);
	    
	    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		intent.setAction("com.peerblock.PeerBlockReceiver");
		intent.putExtra("data", data);
		context.sendBroadcast(intent);
	}
	
	public void HandleMessage(Intent intent, Context context)
	{
		Bundle bundle = intent.getExtras();
		
		try
		{
			int PacketId = bundle.getInt("PacketId");
			boolean ToMain = bundle.getBoolean("ToMain");
			long TargetId = bundle.getLong("TargetId");
			long FromId = bundle.getLong("FromId");
			byte[] data = bundle.getByteArray("data");
			
			if(TargetId != this.OwnUniqueId && !ThisIsMain)
				return;
			
			switch(PacketId)
			{
				case AD_BLOCKED:
				{
					PayloadReader pr = new PayloadReader(data);
					String msg = pr.ReadString();
					Toast.makeText(context, "msgzz: " + msg, Toast.LENGTH_LONG).show();
					break;
				}
			}
		}
		catch (Exception e)
		{
			
		}
	}
}