package com.hooks;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.csv.CsvHistoryInfo;
import com.peerblock.Utils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class InetAddressHook extends XC_MethodHook
{
	public MainHook main;
	public InetAddressHook(MainHook main)
	{
		this.main = main;
	}

	@Override
	protected void beforeHookedMethod(MethodHookParam param) throws Throwable
	{
		param.setResult(null);
		param.setThrowable(new UnknownHostException("Durp"));
		
		String host = "";
		String IpAddress = "";
		
		if(param.method.getName().toLowerCase().equals("getallbyname")) //Get All by Name
		{
			host = param.args[0] != null ? param.args[0].toString() : "";
		}
		else if(param.method.getName().toLowerCase().equals("getbyaddress")) //Get by Address
		{
			InetAddress addr = param.args[0] != null ? (InetAddress)param.args[0] : null;
			if(addr != null)
			{
				host = param.args[0] != null ? param.args[0].toString() : "";
			}
		}
		else if(param.method.getName().toLowerCase().equals("getbyname")) //Get by Name
		{
			host = param.args[0] != null ? param.args[0].toString() : "";
			//throw new UnknownHostException("Blocked by PeerBlock");
		}
		
		
	}
	
	@Override
	protected void afterHookedMethod(MethodHookParam param) throws Throwable
	{
		Object result = param.getResult();
		if(result == null)
			return;
		
		InetAddress[] addresses;
		if (result.getClass().equals(InetAddress.class))
			addresses = new InetAddress[] { (InetAddress) result };
		else if (result.getClass().equals(InetAddress[].class))
			addresses = (InetAddress[]) result;
		else
			addresses = new InetAddress[0];
		
		for(int i = 0; i < addresses.length; i++)
		{
			String HostName = "";
			String HostAddress = "";
			try
			{
				HostName = addresses[i].getHostName();
				HostAddress = addresses[i].getHostAddress();
			}
			catch(Exception ex)
			{
				
			}
			
			XposedBridge.log("[PeerBlock][InetAddressHook] Creating connection to " + HostName);
			
			
			/*if(MainHook.Instance.IsHostBlocked(param, HostName, HostAddress, 0))
			{
				if(MainHook.History != null)
				{
					MainHook.History.AddHistory(new CsvHistoryInfo(MainHook.GetCurrentDate(), HostName, HostAddress, 0, "DNS", false));
				}
				MainHook.Settings.setLastBlockedHost("Host:" + HostName +
												     "\r\nIP Address:" + HostAddress +
												     "\r\nType:DNS" +
												     (MainHook.AppParam != null ? "\r\nApp:" + MainHook.AppParam.packageName : ""));
				
				MainHook.Settings.IncrementDnsBlocks(1);
				MainHook.Log("Blocked DNS, IP:" + HostAddress);
				param.setResult(false);
				param.setThrowable(new UnknownHostException("Unable to resolve host"));
				return;
			}
			
			if(MainHook.History != null)
			{
				MainHook.History.AddHistory(new CsvHistoryInfo(MainHook.GetCurrentDate(), HostName, HostAddress, 0, "DNS", true));
			}*/
		}
	}
}