package com.hooks;

import java.net.ConnectException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class IsConnectedHook extends XC_MethodHook
{
	public MainHook main;
	public IsConnectedHook(MainHook main)
	{
		this.main = main;
	}
	
	@Override
	protected void beforeHookedMethod(MethodHookParam param) throws Throwable
	{
		throw new ConnectException("Blocked by PeerBlock");
	}
	
	@Override
	protected void afterHookedMethod(MethodHookParam param) throws Throwable
	{

	}
}