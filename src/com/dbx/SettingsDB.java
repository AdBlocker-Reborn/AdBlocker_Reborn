package com.dbx;

public class SettingsDB extends DatabaseX
{
	public SettingsDB()
	{
		super("/data/data/com.peerblock/databases/settings.dbx");
	}

	@Override
	public void onCreate()
	{
		String error = "";
		try
		{
			DatabaseTable table = super.CreateNewTable("Settings", new DatabaseColumn("Name", super.STRING),
					 						 				       new DatabaseColumn("Value", super.STRING));
			table.Insert("BlockDNS", "true");
			table.Insert("False?lol", "FALSE");
			DatabaseRow[] rows = table.SelectAll();
		}
		catch (Exception e)
		{
			error = e.getMessage();
		}
	}
}