package com.activities;

import com.csv.CsvSettings;
import com.dbx.DatabaseX;
import com.dbx.SettingsDB;
import com.fragments.AboutFragment;
import com.fragments.BlockListFragment;
import com.fragments.ChangeLogFragment;
import com.fragments.HistoryFragment;
import com.fragments.BlockListFragment.ListAdapter;
import com.peerblock.BlockListStream;
import com.peerblock.HostNameBlocks;
import com.peerblock.PeerBlockProvider;
import com.peerblock.R;
import com.peerblock.Utils;
import com.root.Iptables;
import com.root.ShellCommand;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.ArrayAdapter;
import android.widget.Toast;


public class MainActivity extends PreferenceActivity
{
	public static MainActivity Instance;
	public static CsvSettings Settings;
	public static BlockListStream BlockLists;
	public static HostNameBlocks HostBlocks;
	
	public MainActivity()
	{
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Instance = this;
		super.onCreate(savedInstanceState);
		
		Utils.GetUniqueAppId(this);
		
		SettingsDB settings = new SettingsDB();
		
		// Check if PeerBlock is enabled
		if (!Utils.isXposedEnabled())
		{
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle(getString(R.string.app_name));
			alertDialogBuilder.setMessage("PeerBlock for Android is not enabled in XPosed Framework");
			alertDialogBuilder.setIcon(R.drawable.ic_launcher);
			alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					Intent xInstallerIntent = getPackageManager().getLaunchIntentForPackage("de.robv.android.xposed.installer");
					if (xInstallerIntent != null)
					{
						xInstallerIntent.putExtra("opentab", 1);
						startActivity(xInstallerIntent);
					}
				}
			});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
		
		try
		{
			Settings = new CsvSettings();
			BlockLists = new BlockListStream();
			HostBlocks = new HostNameBlocks();
		}
		catch(Exception e)
		{
			Toast toast = Toast.makeText(this, e.getMessage(), 100000);
			toast.show();
		}
		
		super.addPreferencesFromResource(R.xml.mainpreferences);
		Preference btnLastBlocked = (Preference)findPreference("btnLastBlocked");
		Preference btnRebuildCache = (Preference)findPreference("btnRebuildCache");
		Preference btnAddNewList = (Preference)findPreference("btnAddNewList");
		Preference btnUpdateLists = (Preference)findPreference("btnUpdateLists");
		Preference cbAutoUpdate = (Preference)findPreference("cbAutoUpdate");
		Preference cbEnableBlockedNotifications = (Preference)findPreference("cbEnableBlockedNotifications");
		Preference cbEnableAllowedNotifications = (Preference)findPreference("cbEnableAllowedNotifications");
		Preference btnViewHistory = (Preference)findPreference("btnViewHistory");
		Preference btnChangeLog = (Preference)findPreference("btnChangeLog");
		Preference btnAbout = (Preference)findPreference("btnAbout");
		
		btnLastBlocked.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
            	Intent myIntent = new Intent(MainActivity.Instance, LastBlockedHostActivity.class);
				startActivityForResult(myIntent, 0);
                return true;
            }
        });
		
		btnRebuildCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
            	Intent myIntent = new Intent(MainActivity.Instance, UpdateConfigActivity.class);
                startActivityForResult(myIntent, 0);
                return true;
            }
        });
		
		btnViewHistory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
            	Intent myIntent = new Intent(MainActivity.Instance, HistoryFragment.class);
                startActivityForResult(myIntent, 0);
                return true;
            }
        });

		btnChangeLog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
            	Intent myIntent = new Intent(MainActivity.Instance, ChangeLogFragment.class);
                startActivityForResult(myIntent, 0);
                return true;
            }
        });
		btnAbout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
            	Intent myIntent = new Intent(MainActivity.Instance, AboutFragment.class);
                startActivityForResult(myIntent, 0);
                return true;
            }
        });
	}
}
