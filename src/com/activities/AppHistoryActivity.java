package com.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import com.csv.CsvHistory;
import com.csv.CsvHistoryInfo;
import com.fragments.BlockListFragment;
import com.peerblock.HistoryCount;
import com.peerblock.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AppHistoryActivity  extends Activity
{
	public static AppHistoryActivity Instance;
	public static CsvHistory History;
	public ListView listView;
	private CsvHistoryInfo SelectedHistory;
	
	public AppHistoryActivity()
	{
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Instance = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.apphistory_activity);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		this.listView = (ListView)findViewById(R.id.historyList);
		TextView txtAppName = (TextView)findViewById(R.id.appHistory_name);
		
		String FileName = new File(History.FilePath).getName();
    	String AppName = FileName.substring(0, FileName.length()-4);
    	txtAppName.setText("App: " + AppName);
		
		//appName.settext("App:" + History.)
		ArrayList<CsvHistoryInfo> histories = History.GetAllHistory();
		listView.setAdapter(new AppHistoryAdapterAdapter((CsvHistoryInfo[])histories.toArray(new CsvHistoryInfo[histories.size()])));
	
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
			{
				SelectedHistory = (CsvHistoryInfo)listView.getItemAtPosition(position);
				
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
				{
				    @Override
				    public void onClick(DialogInterface dialog, int which)
				    {
				        switch (which)
				        {
					        case DialogInterface.BUTTON_POSITIVE: //ip address
					        {
					        	WriteBlock(false);
					        	RebuildCacheList();
					            break;
					        }
					        case DialogInterface.BUTTON_NEGATIVE: //host name
					        {
					        	MainActivity.HostBlocks.WriteHost(SelectedHistory.HostName);
					            break;
					        }
					        case DialogInterface.BUTTON_NEUTRAL: //subnet
					        {
					        	WriteBlock(true);
					        	RebuildCacheList();
					            break;
					        }
				        }
				    }
				};
				
				AlertDialog.Builder builder = new AlertDialog.Builder(AppHistoryActivity.Instance);
				builder.setMessage("Do you want to block this host?\r\n" +
								   "Blocking the host name will block the URL/Host\r\n" +
								   "Blocking the Ip Address will block only 1 host\r\n" +
								   "Blocking a subnet /24 will block a range of 256 hosts\r\n" +
								   "If you select one of these please rebuild the cache list!")
								   .setPositiveButton("Ip Address", dialogClickListener)
								   .setNegativeButton("Host Name", dialogClickListener)
								   .setNeutralButton("Subnet /24", dialogClickListener)
								   .show();
			}
		});
	}
	
	private void RebuildCacheList()
	{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
		{
		    @Override
		    public void onClick(DialogInterface dialog, int which)
		    {
		        switch (which)
		        {
			        case DialogInterface.BUTTON_POSITIVE:
			        {
			        	BlockListFragment.getInstance().UpdateBlockList();
			            break;
			        }
		        }
		    }
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(AppHistoryActivity.Instance);
		builder.setMessage("Would you like to rebuild the cache list ?")
						   .setPositiveButton("Yes", dialogClickListener)
						   .setNegativeButton("No", dialogClickListener)
						   .show();
	}
	
	private void WriteBlock(boolean EntireSubnet)
	{
		try
    	{
    		File targetFile = new File(Environment.getExternalStorageDirectory() + "/PeerBlockLists/PeerBlockList.txt");
			RandomAccessFile input = new RandomAccessFile(targetFile, "rws");
			
			if(!targetFile.exists())
					targetFile.createNewFile();
			
			input.seek(input.length()); //go to end of file
			if(EntireSubnet)
			{
				String[] IpStr = SelectedHistory.IpAddress.split("\\.");
				String SubNet = IpStr[0] + "." + IpStr[1] + "." + IpStr[2] + ".0";
				String EndSubNet = IpStr[0] + "." + IpStr[1] + "." + IpStr[2] + ".255";
				input.writeBytes(SelectedHistory.HostName + ":" + SubNet + "-" + EndSubNet + "\r\n");
			}
			else
			{
				input.writeBytes(SelectedHistory.HostName + ":" + SelectedHistory.IpAddress + "-" + SelectedHistory.IpAddress + "\r\n");
			}
			input.close();
		}
    	catch (Exception e)
		{
    		Toast toastss = Toast.makeText(MainActivity.Instance, e.getMessage(), 100000);
			toastss.show();
		}
	}
	
	public class AppHistoryAdapterAdapter extends ArrayAdapter<CsvHistoryInfo>
	{
		private final Context context;
		private final CsvHistoryInfo[] values;
		private ArrayList<AppCache> IconCache;
		private LayoutInflater inflater;
		
		public AppHistoryAdapterAdapter(CsvHistoryInfo[] values)
		{
			super(MainActivity.Instance, R.layout.apphistory_item, values);
			this.values = values;
			this.context = MainActivity.Instance;
			this.IconCache = new ArrayList<AppCache>();
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
	 
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View rowView = inflater.inflate(R.layout.apphistory_item, parent, false);
			CsvHistoryInfo history = values[position];
			TextView txtDate = (TextView) rowView.findViewById(R.id.appHistory_txtDate);
			TextView txtHost = (TextView) rowView.findViewById(R.id.appHistory_txtHost);
			TextView txtIsBlocked = (TextView) rowView.findViewById(R.id.appHistory_txtIsBlocked);

			txtDate.setText("Date:" + history.Date);
			txtHost.setText("Host:" + history.HostName);
			txtIsBlocked.setText("Is Blocked:" + (history.IsAllowed ? "false" : "true"));
			
			
			
			return rowView;
		}
		
		private class AppCache
		{
			public String PackageName;
			public Drawable Icon;
			public HistoryCount Count;
			
			public AppCache(String PackageName, Drawable Icon, HistoryCount Count)
			{
				this.PackageName = PackageName;
				this.Icon = Icon;
				this.Count = Count;
			}
		}
	}
}
