package com.peerblock;

import java.util.TimerTask;

import com.activities.LastBlockedHostActivity;
import com.activities.MainActivity;

public class UpdateTimer extends TimerTask
{
	public UpdateTimer()
	{
		
	}
	
    @Override
    public void run()
    {
    	MainActivity.Instance.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
            	try
            	{
            		long blockedConnections = MainActivity.Settings.getTotalConnectionsBlocks();
            		LastBlockedHostActivity.BlockedIpsView.setText("Connections blocked:" + blockedConnections);
            		LastBlockedHostActivity.LastHostBlocked.setText("Last host blocked\r\n" + MainActivity.Settings.getLastBlockedHost());
					LastBlockedHostActivity.BlockedDnsView.setText("Blocked DNS Requests: " + MainActivity.Settings.getTotalDnsBlocks());
					LastBlockedHostActivity.BlockedUdpView.setText("Blocked UDP Requests:" + 0);
				}
            	catch (Exception e)
            	{
					
				}
            }
        });
    }
}