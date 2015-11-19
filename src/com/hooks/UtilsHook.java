package com.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class UtilsHook extends XC_MethodHook
{
	public MainHook main;
	public UtilsHook(MainHook main)
	{
		this.main = main;
	}

	@Override
	protected void beforeHookedMethod(MethodHookParam param) throws Throwable
	{
		XposedBridge.log("[UTILS][Before] Setting True");
		param.setResult(true);
	}
	
	@Override
	protected void afterHookedMethod(MethodHookParam param) throws Throwable
	{
		
	}
}