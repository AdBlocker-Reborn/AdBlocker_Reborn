package com.peerblock;

import java.io.File;

import com.activities.MainActivity;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

public class PeerBlockProvider extends ContentProvider
{
	//content://com.peerblock.provider/TABLE_HERE
	public static final String AUTHORITY = "com.peerblock.provider";
	public static final String PREF_RESTRICTION = AUTHORITY;
	public static final String PATH_RESTRICTION = "restriction";
	public static final Uri URI_RESTRICTION = Uri.parse("content://" + AUTHORITY + "/" + PATH_RESTRICTION);
	private static final UriMatcher sUriMatcher;
	public static final String COL_RESTRICTED = "Restricted";
	public static final String COL_METHOD = "Method";
	private static final int TYPE_RESTRICTION = 1;
	
	static
	{
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, PATH_RESTRICTION, TYPE_RESTRICTION);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{

		return 0;
	}

	@Override
	public String getType(Uri uri)
	{

		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{

		return null;
	}

	@Override
	public boolean onCreate()
	{
		updateRestriction(0, "test", "TestMethod", true);
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder)
	{

		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs)
	{

		return 0;
	}
	
	private void updateRestriction(int uid, String restrictionName, String methodName, boolean allowed)
	{
		// Update restriction
		@SuppressWarnings("deprecation")
		SharedPreferences prefs = getContext().getSharedPreferences(PREF_RESTRICTION + "." + uid, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = prefs.edit();
		if (methodName == null || !allowed)
			editor.putBoolean(getRestrictionPref(restrictionName), !allowed);
		if (methodName != null)
			editor.putBoolean(getExceptionPref(restrictionName, methodName), allowed);
		editor.apply();
		setPrefFileReadable(PREF_RESTRICTION, uid);
	}
	
	private static void setPrefFileReadable(String preference)
	{
		new File(getPrefFileName(preference)).setReadable(true, false);
	}

	private static void setPrefFileReadable(String preference, int uid)
	{
		new File(getPrefFileName(preference, uid)).setReadable(true, false);
	}
	private static String getPrefFileName(String preference)
	{
		String packageName = MainActivity.class.getPackage().getName();
		return Environment.getDataDirectory() + File.separator + "data" + File.separator + packageName + File.separator
				+ "shared_prefs" + File.separator + preference + ".xml";
	}

	private static String getPrefFileName(String preference, int uid)
	{
		String packageName = MainActivity.class.getPackage().getName();
		return Environment.getDataDirectory() + File.separator + "data" + File.separator + packageName + File.separator
				+ "shared_prefs" + File.separator + preference + "." + uid + ".xml";
	}
	private static String getRestrictionPref(String restrictionName)
	{
		return String.format("%s.%s", COL_RESTRICTED, restrictionName);
	}
	private static String getExceptionPref(String restrictionName, String methodName)
	{
		return String.format("%s.%s.%s", COL_METHOD, restrictionName, methodName);
	}

}
