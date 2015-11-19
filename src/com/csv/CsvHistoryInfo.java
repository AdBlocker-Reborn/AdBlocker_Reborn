package com.csv;

public class CsvHistoryInfo
{
	public String Date;
	public String HostName;
	public String IpAddress;
	public int Port;
	public String ConnectType;
	public boolean IsAllowed;
	
	public CsvHistoryInfo(String Date, String HostName, String IpAddress, int Port,
						  String ConnectType, boolean IsAllowed)
	{
		this.Date = Date;
		this.HostName = HostName;
		this.IpAddress = IpAddress;
		this.Port = Port;
		this.ConnectType = ConnectType;
		this.IsAllowed = IsAllowed;
	}
}