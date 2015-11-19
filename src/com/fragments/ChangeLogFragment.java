package com.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.peerblock.R;

public class ChangeLogFragment extends Activity
{
	public static final String ARG_SECTION_NUMBER = "section_number";

	public ChangeLogFragment()
	{
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changelogs_activity);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		TextView aboutTextView = (TextView)findViewById(R.id.txtChanges);
		aboutTextView.setMovementMethod(new ScrollingMovementMethod());
		ArrayList<ChangeLog> Changes = new ArrayList<ChangeLog>();

		Changes.add(new ChangeLog("1.04", "Fixed most app crashes\r\n" +
										  "New User Interface\r\n" +
										  "Designed my own database format\r\n" +
										  "Removed the Block DNS option, as default it's always on\r\n" +
										  "Less format errors in rebuilding the cache list\r\n" +
										  ""));
		
		Changes.add(new ChangeLog("1.03", "Fixed DNS/Connection counters\r\n" +
										  "Fixed double icons\r\n" +
										  "Memory leak fix\r\n" +
										  "Added history, system apps included (no root required)\r\n" +
										  "Created CSV Format for saving settings\r\n" +
										  "Improved performance\r\n" +
										  "Apps with abnormal I/O behavior will get no internet\r\n" +
										  "Updated error handling in rebuilding the cache list\r\n" +
										  "Block hosts/subnet/ip from the history\r\n" +
										  "App will be shown now in the 'Last Blocked Host'\r\n" +
										  "Added comma's at the 'Blocking xx Ips' to read it better\r\n" +
										  "Hooking a extra API if some rooted apps are calling it directly\r\n" +
										  "Added to changelogs and about scrollbars\r\n"));
		Changes.add(new ChangeLog("1.02", "Changed the required API SDK to 14\r\nAdded a progress window for rebuilding the cache"));
		Changes.add(new ChangeLog("1.01", "Fixed App crash when PeerBlockLists directory did not exist"));
		Changes.add(new ChangeLog("1.00", "Initial creation and upload of the app"));
		
		String changesStr = "";
		for(int i = 0; i < Changes.size(); i++)
		{
			changesStr += Changes.get(i).Version + "\r\n" + Changes.get(i).Changes + "\r\n\r\n";				
		}
		
		aboutTextView.setText(changesStr);
	}
	
	private class ChangeLog
	{
		public String Version;
		public String Changes;
		
		public ChangeLog(String Version, String Changes)
		{
			this.Version = Version;
			this.Changes = Changes;
		}
	}
}