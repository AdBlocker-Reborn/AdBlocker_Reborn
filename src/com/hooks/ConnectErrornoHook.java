package com.hooks;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.csv.CsvHistoryInfo;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class ConnectErrornoHook extends XC_MethodHook
{
	public MainHook main;
	public ConnectErrornoHook(MainHook main)
	{
		this.main = main;
	}
	
	@Override
	protected void beforeHookedMethod(MethodHookParam param) throws Throwable
	{
		param.setResult(null);
		param.setThrowable(new UnknownHostException("Durp"));
		XposedBridge.log("[PeerBlock][ConnectErrornoHook] blocking shit");

		/*InetAddress addr = (InetAddress)param.args[1];
		int Port = Integer.parseInt(param.args[2].toString());
		
		if(addr != null)
		{
			XposedBridge.log("[PeerBlock][ConnectErrornoHook] Creating connection to " + addr.getHostName());
			if(MainHook.Instance.IsHostBlocked(param, addr.getHostName(), addr.getHostAddress(), Port))
			{
				if(MainHook.History != null)
				{
					MainHook.History.AddHistory(new CsvHistoryInfo(MainHook.GetCurrentDate(), addr.getHostName(), addr.getHostAddress(), 0, "TCP", false));
				}
				MainHook.Settings.setLastBlockedHost("Host:" + addr.getHostName() +
												     "\r\nIP Address:" + addr.getHostAddress() +
												     "\r\nPort:" + Port +
												     "\r\nType:TCP" +
												     (MainHook.AppParam != null ? "\r\nApp:" + MainHook.AppParam.packageName : "") +
												     "\r\nLow-Level Blocking [Root App?]");
				
				MainHook.Settings.IncrementConnectionBlocks(1);
				MainHook.Log("[Before]Blocked connection: " + addr.getHostName() + ", IP:" + addr.getHostAddress() + ", Port:" + Port);
				param.setResult(false);
				param.setThrowable(new UnknownHostException("Unable to resolve host"));
				return;
			}
			
			if(MainHook.History != null)
			{
				MainHook.History.AddHistory(new CsvHistoryInfo(MainHook.GetCurrentDate(), addr.getHostName(), addr.getHostAddress(), 0, "TCP", true));
			}
		}*/
	}
	
	@Override
	protected void afterHookedMethod(MethodHookParam param) throws Throwable
	{

	}
}
