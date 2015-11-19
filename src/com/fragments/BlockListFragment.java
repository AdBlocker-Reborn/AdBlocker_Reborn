package com.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.activities.MainActivity;
import com.activities.UpdateConfigActivity;
import com.peerblock.R;

public class BlockListFragment extends Activity
{
	public static final String ARG_SECTION_NUMBER = "section_number";
	public LayoutInflater inflater;
	public static ListView listView;
	
	public static BlockListFragment instance;
	private static Object InstanceLock = new Object();
	public static BlockListFragment getInstance()
	{
		synchronized(InstanceLock)
		{
			if(instance == null)
				return new BlockListFragment();
			return instance;
		}
	}
	
	public BlockListFragment()
	{
		instance = this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		listView = (ListView)findViewById(R.id.historyList);
		Button RebuildCachebtn = (Button)findViewById(R.id.button1);
		
		RebuildCachebtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				UpdateBlockList();
			}
		});

		//ArrayAdapter adapter = new ArrayAdapter(PeerBlock.Instance, R.layout.item_view, R.id.textView1, test);
		ArrayAdapter<String> adapter = new ListAdapter();
		listView.setAdapter(adapter);
	}
	
	public void UpdateBlockList()
	{
		try
		{
			Toast toast = Toast.makeText(MainActivity.Instance, "This can take a while please wait...", Toast.LENGTH_LONG);
			toast.show();
			ArrayAdapter<String> adapter = new ListAdapter();
			listView.setAdapter(adapter);
			
			Intent myIntent = new Intent(MainActivity.Instance, UpdateConfigActivity.class);
            startActivityForResult(myIntent, 0);
		}
		catch(Exception e)
		{
			Toast toastss = Toast.makeText(MainActivity.Instance, e.getMessage(), Toast.LENGTH_LONG);
			toastss.show();
		}
	}
	
	public class ListAdapter extends ArrayAdapter<String>
	{
		public ListAdapter()
		{
			super(MainActivity.Instance, R.layout.item_view, R.id.txtList, MainActivity.BlockLists.GetList());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			return super.getView(position, convertView, parent);
		}
	}
}