package com.csv;

import android.os.Environment;

public class CsvSettings extends CsvFormat
{
	private static final String ALLOWHTTP = "AllowHTTP";
	private static final String ALLOWHTTPS = "AllowHTTPS";
	private static final String BLOCK_ALL_TRAFFIC = "BlockAllTraffic";
	private static final String BLOCK_DNS = "BlockDns";
	private static final String Block_Ad_Keyword = "BlockAdKeyword";
	private static final String LAST_HOST_BLOCKED = "LastHostBlocked";
	private static final String TOTAL_DNS_BLOCKED = "TotalDnsBlocks";
	private static final String TOTAL_CONNECTIONS_BLOCKED = "TotalConnectionBlocks";
	private static final String TOTAL_IP_COUNT = "TotalIpBlockCount";
	
	public CsvSettings() throws Exception
	{
		super(Environment.getExternalStorageDirectory() + "/PeerBlock/settings.csv");
	}

	@Override
	protected void onCreate()
	{
		super.WriteKey(ALLOWHTTP, "false");
		super.WriteKey(ALLOWHTTPS, "false");
		super.WriteKey(BLOCK_ALL_TRAFFIC, "false");
		super.WriteKey(BLOCK_DNS, "true");
		super.WriteKey(Block_Ad_Keyword, "true");
		super.WriteKey(LAST_HOST_BLOCKED, "");
		super.WriteKey(TOTAL_DNS_BLOCKED, "0");
		super.WriteKey(TOTAL_CONNECTIONS_BLOCKED, "0");
		super.WriteKey(TOTAL_IP_COUNT, "0");
	}
	
	public boolean getAllowHttp()
	{
		return super.GetBoolean(ALLOWHTTP, 0);
	}
	
	public boolean getAllowHttps()
	{
		return super.GetBoolean(ALLOWHTTPS, 0);
	}
	
	public boolean getBlockAllTraffic()
	{
		return super.GetBoolean(BLOCK_ALL_TRAFFIC, 0);
	}
	
	public boolean getBlockDns()
	{
		return super.GetBoolean(BLOCK_DNS, 0);
	}
	
	public boolean getBlockAdKeyword()
	{
		return super.GetBoolean(Block_Ad_Keyword, 0);
	}
	
	public String getLastBlockedHost()
	{
		return super.GetString(LAST_HOST_BLOCKED, 0);
	}
	
	public int getTotalDnsBlocks()
	{
		return super.GetInteger(TOTAL_DNS_BLOCKED, 0);
	}
	
	public int getTotalConnectionsBlocks()
	{
		return super.GetInteger(TOTAL_CONNECTIONS_BLOCKED, 0);
	}
	
	public long getTotalIpBlockCount()
	{
		return super.GetLong(TOTAL_IP_COUNT, 0);
	}
	
	public void setAllowHttp(boolean value)
	{
		super.WriteValue(ALLOWHTTP, (value ? "true" : "false"));
	}
	
	public void setAllowHttps(boolean value)
	{
		super.WriteValue(ALLOWHTTPS, (value ? "true" : "false"));
	}
	
	public void setBlockAllTraffic(boolean value)
	{
		super.WriteValue(BLOCK_ALL_TRAFFIC, (value ? "true" : "false"));
	}
	
	public void setBlockDns(boolean value)
	{
		super.WriteValue(BLOCK_DNS, (value ? "true" : "false"));
	}
	
	public void setBlockAdKeyword(boolean value)
	{
		super.WriteValue(Block_Ad_Keyword, (value ? "true" : "false"));
	}
	
	public void setLastBlockedHost(String value)
	{
		super.WriteValue(LAST_HOST_BLOCKED, value);
	}
	
	public void IncrementDnsBlocks(int value)
	{
		super.WriteValue(TOTAL_DNS_BLOCKED, Integer.toString(getTotalDnsBlocks() + value));
	}
	
	public void IncrementConnectionBlocks(int value)
	{
		super.WriteValue(TOTAL_CONNECTIONS_BLOCKED, Integer.toString(getTotalConnectionsBlocks() + value));
	}
	
	public void setTotalIpBlockCount(long value)
	{
		super.WriteValue(TOTAL_IP_COUNT, Long.toString(value));
	}
}
