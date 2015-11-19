package com.hooks;

import java.net.SocketException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class BlockGuard_ConnectHook extends XC_MethodHook
{
	public MainHook main;
	public BlockGuard_ConnectHook(MainHook main)
	{
		this.main = main;
	}
	
	@Override
	protected void beforeHookedMethod(MethodHookParam param) throws Throwable
	{
		param.setResult(null);
		param.setResult(new SocketException());
	}
}