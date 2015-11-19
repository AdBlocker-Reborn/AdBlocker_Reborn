package com.peerblock;

import android.graphics.drawable.Drawable;

public class AppCache
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