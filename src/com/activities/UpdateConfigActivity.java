package com.activities;

import com.peerblock.R;
import com.peerblock.R.id;
import com.peerblock.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateConfigActivity extends Activity
{
	private Handler progressBarHandler = new Handler();
	public String CurrentName = "";
	public int progressBarStatus;
	public int RangesToScan = 0;
	public String ScanStatus = "Analyzing PeerBlock Lists...";
	public static UpdateConfigActivity Instance;
	public boolean IsDone = false;
	public String ErrorMsg = "";
	public boolean ErrorOccured = false;
	public long FormatErrors = 0;
	
	public UpdateConfigActivity()
	{
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Instance = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.updateconfig_activity);
		
		new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					MainActivity.BlockLists.RebuildCache(UpdateConfigActivity.Instance);
					//MainActivity.BlockLists.ApplyIpTable(UpdateConfigActivity.Instance);
				}
				catch (Exception e)
				{
					while(true)
					{
						ScanStatus = e.getMessage();
						UpdateProgress();
					}
				}
				IsDone = true;
			}
		}).start();
		
		new Thread(new Runnable()
		{
			public void run()
			{
				while (!IsDone && !ErrorOccured)
				{
					try { Thread.sleep(100); } catch (InterruptedException e) { }
				}
				
				if(ErrorOccured)
				{
					ScanStatus = "Error occured... " + ErrorMsg;
					UpdateProgress();
				}
				else
				{
					try
					{
						ScanStatus = "Reloading cache...";
						UpdateProgress();
						
						//PeerBlock.Config.ReloadCacheList(PeerBlock.Config.blockLists);
					} catch (Exception e1) {
					}
					ScanStatus = "DONE!";
				}
				
				// sleep 2 seconds, so that you can see the 100%
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			
				Intent intent = new Intent();
		        setResult(RESULT_OK, intent);
		        finish();
			}
		}).start();
	}
	
	public void UpdateProgress()
	{
		progressBarHandler.post(new Runnable()
		{
			public void run()
			{
				ProgressBar progressBar = (ProgressBar)findViewById(R.id.UpdateProgressBar);
				TextView StatusTxt = (TextView)findViewById(R.id.txtUpdateStatus);
				TextView progressTxt = (TextView)findViewById(R.id.txtUpdateProgress);
				TextView txtErrors = (TextView)findViewById(R.id.txtUpdateErrors);
				progressBar.setProgress(progressBarStatus);
				progressBar.setMax(RangesToScan);
				StatusTxt.setText("Status:" + ScanStatus);
				progressTxt.setText("Progress:" + progressBarStatus + " / " + RangesToScan);
				txtErrors.setText("Format Errors:" + FormatErrors);
			}
		});
	}
	
	@Override
	public void onBackPressed()
	{
		//prevent people from going back to main activity
	}
}
