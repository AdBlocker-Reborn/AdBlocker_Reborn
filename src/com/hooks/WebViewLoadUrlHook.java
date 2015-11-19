package com.hooks;

import java.net.SocketException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class WebViewLoadUrlHook extends XC_MethodHook
{
	public MainHook main;
	public WebViewLoadUrlHook(MainHook main)
	{
		this.main = main;
	}
	
	@Override
	protected void beforeHookedMethod(MethodHookParam param) throws Throwable
	{
		param.setResult(null);
	}
}