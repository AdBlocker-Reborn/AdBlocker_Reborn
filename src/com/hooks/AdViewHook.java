package com.hooks;

import java.lang.reflect.Method;
import java.net.UnknownHostException;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import com.activities.AppHistoryActivity;
import com.activities.MainActivity;
import com.peerblock.MessageHandler;
import com.peerblock.PayloadWriter;
import com.peerblock.Utils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class AdViewHook extends XC_MethodHook
{
	public MainHook main;
	public AdViewHook(MainHook main)
	{
		this.main = main;
	}
	

	@Override
	protected void beforeHookedMethod(MethodHookParam param) throws Throwable
	{
		MainHook.Instance.RegisterReceiver();
		
		PayloadWriter pw = new PayloadWriter();
		pw.WriteString("Ad Blocked");
		new MessageHandler(Utils.getCurrentApplication(), false).SendMessage(pw.ToByteArray(), 0, MessageHandler.AD_BLOCKED);

		
		param.setResult(null); //don't execute the load for the ad
	}
	
	@Override
	protected void afterHookedMethod(MethodHookParam param) throws Throwable
	{
		
	}
}