package com.hooks;

import java.net.InetAddress;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class SendToHook extends XC_MethodHook
{
	public MainHook main;
	public SendToHook(MainHook main)
	{
		this.main = main;
	}
	
	@Override
	protected void beforeHookedMethod(MethodHookParam param) throws Throwable
	{
		main.DumpArgumentValues(param, true);
		InetAddress addr = null;
		
		if(param.args.length == 5)
			addr = (InetAddress)param.args[4];
		else
			addr = (InetAddress)param.args[5];
		
		if(addr != null)
		{
			main.Log("Somebody is using UDP, Dest:" + addr.getHostAddress());
		}
		else
		{
			main.Log("addr UDP is null");
		}
	}
	
	@Override
	protected void afterHookedMethod(MethodHookParam param) throws Throwable
	{
		
	}
}
