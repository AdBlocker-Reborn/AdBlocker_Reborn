package com.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.activities.AppHistoryActivity;
import com.activities.MainActivity;
import com.activities.UpdateConfigActivity;
import com.csv.CsvHistory;
import com.csv.CsvHistoryInfo;
import com.peerblock.AppCache;
import com.peerblock.HistoryCount;
import com.peerblock.R;

public class HistoryFragment extends Activity
{
	static final ArrayList<CsvHistory> HistoryList;
	public static ActivityManager activityManager;
	public ListView listView;
	public List<PackageInfo> packs;
	public PackageManager pk;
	public static ArrayList<AppCache> IconCache;

	static
	{
		IconCache = new ArrayList<AppCache>();
		HistoryList = new ArrayList<CsvHistory>();
	}
	
	public HistoryFragment()
	{
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_activity);
		
		activityManager = (ActivityManager)MainActivity.Instance.getSystemService(Context.ACTIVITY_SERVICE);
		this.pk = MainActivity.Instance.getPackageManager();
		this.packs = pk.getInstalledPackages(0);
		
		File blockLists = new File(CsvHistory.HistoryPath);
		if(!blockLists.exists())
			blockLists.mkdirs();
		
	    File list[] = blockLists.listFiles();
	    HistoryList.clear();
	    for( int i = 0; i< list.length; i++)
	    {
	    	String FileName = new File(list[i].getAbsolutePath()).getName();
	    	String AppName = FileName.substring(0, FileName.length()-4);
	    	
	    	try
	    	{
	    		CsvHistory history = new CsvHistory(AppName);
	    		if(history.GetFirstHistory() != null)
	    			HistoryList.add(history);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
		listView = (ListView)findViewById(R.id.historyList);
		listView.setScrollingCacheEnabled(true);
		listView.setAdapter(new HistoryAdapter((CsvHistory[])HistoryList.toArray(new CsvHistory[HistoryList.size()])));
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
			{
				CsvHistory history = (CsvHistory)listView.getItemAtPosition(position);
					
				//open up the history activity
				Intent myIntent = new Intent(MainActivity.Instance, AppHistoryActivity.class);
				AppHistoryActivity.History = history;
				startActivityForResult(myIntent, 0);
			}
		});
	}
	
	public class HistoryAdapter extends ArrayAdapter<CsvHistory>
	{
		private final Context context;
		private final CsvHistory[] values;
		private LayoutInflater inflater;
		
		public HistoryAdapter(CsvHistory[] values)
		{
			super(MainActivity.Instance, R.layout.history_item, values);
			this.values = values;
			this.context = MainActivity.Instance;
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
	 
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View rowView = inflater.inflate(R.layout.history_item, parent, false);
			TextView txtAppName = (TextView) rowView.findViewById(R.id.txtAppName);
			TextView txtBlocks = (TextView) rowView.findViewById(R.id.txtBlocks);
			TextView txtAllows = (TextView) rowView.findViewById(R.id.txtAllows);
			ImageView imgHistory = (ImageView) rowView.findViewById(R.id.imgHistory);
			
			CsvHistory history = values[position];
			File file = new File(history.FilePath);
			
			String TargetPackage = file.getName().substring(0, file.getName().length()-4);
			txtAppName.setText("App: " + TargetPackage);
			
			//get app icon
			AppCache cache = null;
			
			for(int i = 0; i < IconCache.size(); i++)
			{
				if(IconCache.get(i).PackageName.equals(TargetPackage))
				{
					cache = IconCache.get(i);
					break;
				}
			}
			
			if(cache == null)
			{
				for(int i = 0; i < packs.size(); i++)
				{
					if(packs.get(i).packageName.equals(TargetPackage))
					{
						Drawable icon = packs.get(i).applicationInfo.loadIcon(pk);
						cache = new AppCache(TargetPackage, icon, history.GetHistoryCount());
						IconCache.add(cache);
						break;
					}
				}
			}
			
			if(cache != null)
			{
				txtBlocks.setText("Blocks: " + cache.Count.BlockCount);
				txtAllows.setText("Allows: " + cache.Count.AllowCount);
				if(cache.Icon != null)
				{
					imgHistory.setImageDrawable(cache.Icon);
				}
			}
			return rowView;
		}
	}
}