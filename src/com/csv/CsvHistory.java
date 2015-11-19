package com.csv;

import java.util.ArrayList;

import com.peerblock.HistoryCount;

import android.os.Environment;

public class CsvHistory extends CsvFormat
{
	public static final String HistoryPath = Environment.getExternalStorageDirectory() + "/PeerBlock/history/";
	public static final int DATE_INDEX = 0;
	public static final int HOSTNAME_INDEX = 1;
	public static final int IPADDRESS_INDEX = 2;
	public static final int PORT_INDEX = 3;
	public static final int CONNECT_TYPE_INDEX = 4;
	public static final int IS_ALLOWED_INDEX = 5;
	public static final String History_Key = "History";
	private HistoryCount historyCount;
	
	public CsvHistory(String AppName) throws Exception
	{
		super(HistoryPath + AppName + ".csv");
	}

	@Override
	protected void onCreate()
	{
		
	}
	
	public void AddHistory(CsvHistoryInfo value)
	{
		super.WriteKey(History_Key, value.Date, value.HostName, value.IpAddress, ""+value.Port, value.ConnectType, (value.IsAllowed ? "true" : "false"));
	}
	
	public CsvHistoryInfo GetFirstHistory()
	{
		String[] val = super.GetValue(History_Key);
		if(val.length > 0)
			return ValueToHistory(val);
		return null;
	}
	
	private CsvHistoryInfo ValueToHistory(String[] val)
	{
		if(val.length >= 6)
		{
			return new CsvHistoryInfo(val[DATE_INDEX], val[HOSTNAME_INDEX], val[IPADDRESS_INDEX],
									  Integer.parseInt(val[PORT_INDEX]), val[CONNECT_TYPE_INDEX],
									  val[IS_ALLOWED_INDEX].equals("true"));
		}
		return null;
	}
	
	public HistoryCount GetHistoryCount()
	{
		historyCount = new HistoryCount();
		
		super.GetValues(History_Key, new CsvCallback()
		{
			@Override
			public void onCallback(String[] val)
			{
				CsvHistoryInfo inf = ValueToHistory(val);
				if(inf != null)
				{
					if(inf.IsAllowed)
						historyCount.AllowCount++;
					else
						historyCount.BlockCount++;
				}
			}
		});
		return historyCount;
	}
	
	public ArrayList<CsvHistoryInfo> GetAllHistory()
	{
		ArrayList<String[]> valStr = super.GetValues("History", null);
		ArrayList<CsvHistoryInfo> info = new ArrayList<CsvHistoryInfo>();
		
		for(int i = 0; i < valStr.size(); i++)
		{
			String[] val = valStr.get(i);
			info.add(ValueToHistory(val));
		}
		return info;
	}
}