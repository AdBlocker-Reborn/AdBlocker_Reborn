package com.activities;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Timer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.peerblock.R;
import com.peerblock.UpdateTimer;

public class LastBlockedHostActivity extends Activity
{
	public static TextView BlockedIpsView;
	public static TextView BlockedDnsView;
	public static TextView BlockedUdpView;
	public static TextView LastHostBlocked;
	public static Timer timer = new Timer();
	
	public LastBlockedHostActivity()
	{
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId())
        {
	        case android.R.id.home: 
	            onBackPressed();
	            break;
	
	        default:
	            return super.onOptionsItemSelected(item);
        }
        return true;
    }

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainmenu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		try
		{
			TextView blockingIps = (TextView)findViewById(R.id.textView5);
			CheckBox allowHttp = (CheckBox)findViewById(R.id.chAppHistoryShowBlocked);
			CheckBox allowHttps = (CheckBox)findViewById(R.id.checkBox2);
			CheckBox paranoidMode = (CheckBox)findViewById(R.id.checkBox3);
			CheckBox BlockAdKeyword = (CheckBox)findViewById(R.id.checkBox5);
			TextView blockingHostNames = (TextView)findViewById(R.id.txtBlockingHosts);
			LastBlockedHostActivity.BlockedIpsView = (TextView)findViewById(R.id.txtUpdateName);
			LastBlockedHostActivity.LastHostBlocked = (TextView)findViewById(R.id.textView6);
			LastBlockedHostActivity.BlockedDnsView = (TextView)findViewById(R.id.txtChanges);
			LastBlockedHostActivity.BlockedUdpView = (TextView)findViewById(R.id.txtUpdateProgress);
			LastBlockedHostActivity.timer.schedule(new UpdateTimer(),  0,500);
			
			blockingIps.setText("Blocking " + NumberFormat.getNumberInstance(Locale.US).format(MainActivity.BlockLists.getIpCountBlocked()) + " IPs");
			blockingHostNames.setText("Blocking Hostnames: " + MainActivity.HostBlocks.getHostNameCount());
			allowHttp.setChecked(MainActivity.Settings.getAllowHttp());
			allowHttps.setChecked(MainActivity.Settings.getAllowHttps());
			paranoidMode.setChecked(MainActivity.Settings.getBlockAllTraffic());
			BlockAdKeyword.setChecked(MainActivity.Settings.getBlockAdKeyword());
			LastBlockedHostActivity.LastHostBlocked.setText("Last host blocked\r\n" + MainActivity.Settings.getLastBlockedHost());
			LastBlockedHostActivity.BlockedDnsView.setText("Blocked DNS Requests: " + MainActivity.Settings.getTotalDnsBlocks());

			allowHttp.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					try
					{
						MainActivity.Settings.setAllowHttp(((CheckBox) v).isChecked());
					}
					catch (Exception e)
					{
						
					}
				}
			});
			allowHttps.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					try
					{
						MainActivity.Settings.setAllowHttps(((CheckBox) v).isChecked());
					}
					catch (Exception e)
					{
						
					}
				}
			});
			paranoidMode.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					try
					{
						MainActivity.Settings.setBlockAllTraffic(((CheckBox) v).isChecked());
					}
					catch (Exception e)
					{
						Toast toastss = Toast.makeText(MainActivity.Instance, e.getMessage(), 100000);
						toastss.show();
					}
				}
			});
			BlockAdKeyword.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					try
					{ 
						MainActivity.Settings.setBlockAdKeyword(((CheckBox) v).isChecked());
					}
					catch (Exception e)
					{
						
					}
				}
			});
		}
		catch (Exception e)
		{

		}
	}
}