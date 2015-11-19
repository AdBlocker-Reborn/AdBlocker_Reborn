package com.hooks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;

import com.csv.CsvHistory;
import com.csv.CsvSettings;
import com.peerblock.BlockListStream;
import com.peerblock.HostNameBlocks;
import com.peerblock.PayloadWriter;
import com.peerblock.Utils;

import static de.robv.android.xposed.XposedHelpers.findClass;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class MainHook implements IXposedHookLoadPackage, IXposedHookZygoteInit
{
	public static CsvSettings Settings;
	public static CsvHistory History;
	public static BlockListStream BlockList;
	public static LoadPackageParam AppParam;
	public static HostNameBlocks HostBlocks;
	private static Object SyncLock = new Object();
	public static MainHook Instance;
	private boolean RegisteredReceiver = false;
	
	public MainHook()
	{
		
	}
	
	public void initZygote(StartupParam startupParam) throws Throwable
	{
		Instance = this;
		//root access here
		try { Settings = new CsvSettings(); } catch(Exception ex) {}
    	try { BlockList = new BlockListStream(); } catch(Exception ex) {}
    	try { HostBlocks = new HostNameBlocks(); } catch(Exception ex) {}
	}
	
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable
    {
    	AppParam = lpparam;
    	XposedBridge.log("[PeerBlock] Some app got logged..." + lpparam.packageName);
    	
    	//hook PeerBlock method in Utils "isXposedEnabled"
    	if(AppParam.packageName.equals("com.peerblock"))
    	{
    		XposedBridge.log("[PeerBlock] Hooking isXposedEnabled");
    		HookMethod("com.peerblock.Utils", "isXposedEnabled", new UtilsHook(this), AppParam, false);
    	}
    	else
    	{
        	/*if(HookMethod("libcore.io.BlockGuardOs", "connect", new BlockGuard_ConnectHook(this), lpparam, true)) //Lower-Level Block
        		Log("Hooking BlockGuardOs.connect");
        	else
        		Log("Unable to hook BlockGuardOs.connect");
        	
    		//don't hook ourself with these APIs
        	HookMethod("java.net.InetAddress", "getAllByName", new InetAddressHook(this), lpparam, true); //DNS
        	HookMethod("java.net.InetAddress", "getByAddress", new InetAddressHook(this), lpparam, true); //DNS
        	HookMethod("java.net.InetAddress", "getByName", new InetAddressHook(this), lpparam, true); //DNS
        	//DumpMethodNames("libcore.io.IoBridge", lpparam);
        	//HookMethod("libcore.io.IoBridge", "isConnected", new IsConnectedHook(this), lpparam, true); //TCP
        	HookMethod("libcore.io.IoBridge", "connect", new ConnectHook(this), lpparam, true); //TCP
        	HookMethod("libcore.io.IoBridge", "connectErrno", new ConnectErrornoHook(this), lpparam, true); //TCP
        	*/

        	//block all URLs
        	//HookMethod("android.webkit", "loadUrl", new WebViewLoadUrlHook(this), lpparam, true);
        	//HookMethod("android.webkit", "postUrl", new WebViewLoadUrlHook(this), lpparam, true);
    		
        	//block ad's
        	if(HookMethod("com.google.ads.AdView", "loadAd", new AdViewHook(this), lpparam, true))
        	{
        		Log("Hooked com.google.ads.AdView");
        	}
    	}
    	
    	try { History = new CsvHistory(lpparam.packageName); } catch(Exception ex) { }
    }
    
    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
        	PayloadWriter pw = new PayloadWriter();
    		pw.WriteString("don't mind me at all :D");
    		
    		
        }
    };
    
    public void RegisterReceiver()
    {
    	if(RegisteredReceiver)
    		return;
    	
    	Application app = Utils.getCurrentApplication();
    	if(app != null)
    	{
        	app.registerReceiver(receiver, new IntentFilter("com.peerblock.PeerBlockReceiver"));
        	RegisteredReceiver = true;
    	}
    }
    
    public boolean HookConstructor(String ClassName, XC_MethodHook Hook, LoadPackageParam lpparam, boolean HookOverloads)
    {
    	Class<?> hookClass = findClass(ClassName, lpparam.classLoader);
		if (hookClass == null)
		{
			Log("Class not found wait wot");
			return false;
		}
		
		XposedBridge.hookAllConstructors(hookClass, Hook);
    	return true;
    }
    
    public boolean HookMethod(String ClassName, String MethodName, XC_MethodHook Hook, LoadPackageParam lpparam, boolean HookOverloads)
    {
    	Class<?> hookClass = null;
    	
		//try to find the class with the ClassLoader if not found
    	try
    	{
    		hookClass = findClass(ClassName, lpparam.classLoader);
			if (hookClass == null)
			{
				return false;
			}
    	}
    	catch(ClassNotFoundError ex2)
    	{
    		return false;
    	}
    	catch(Exception ex)
    	{
    		return false;
    	}
    	
		// Add hook
		Set<XC_MethodHook.Unhook> hookSet = new HashSet<XC_MethodHook.Unhook>();
		boolean isHooked = false;
		for (Method method : hookClass.getDeclaredMethods())
		{
			if (method.getName().toLowerCase().equals(MethodName.toLowerCase()))
			{
				hookSet.add(XposedBridge.hookMethod(method, Hook));
				isHooked = true;
				if(!HookOverloads)
					return isHooked;
			}
		}
		
		if(!isHooked)
		{
			XposedBridge.log("Unable to hook method " + MethodName);
		}
		
		return isHooked;
    }
    
    public void HookClass(String ClassName, XC_MethodHook Hook, LoadPackageParam lpparam, String[] ExcludeMethods)
    {
    	Class<?> hookClass = findClass(ClassName, lpparam.classLoader);
		if (hookClass == null) {
			XposedBridge.log("Class not found wait wot");
			return;
		}

		// Add hook
		Set<XC_MethodHook.Unhook> hookSet = new HashSet<XC_MethodHook.Unhook>();
		for (Method method : hookClass.getDeclaredMethods())
		{
			boolean exclude = false;
			for(int i = 0; i < ExcludeMethods.length; i++)
			{
				if(method.getName() == ExcludeMethods[i])
				{
					exclude = true;
					break;
				}
			}
			
			if(!exclude)
			{
				XposedBridge.log("Hooked method " + method.getName());
				hookSet.add(XposedBridge.hookMethod(method, Hook));
			}
			else
			{
				XposedBridge.log("Excluded method " + method.getName());
			}
		}
    }
    
    public static void Log(String msg)
    {
    	try
    	{
	    	Calendar cal = Calendar.getInstance();
	    	cal.getTime();
	    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			XposedBridge.log("[" + sdf.format(cal.getTime()) + "] " + msg);
    	}
    	catch(Exception ex)
    	{
    		//this method does not throw a exception but just to be sure...
    	}
    }
    
    public static String GetCurrentDate()
    {
    	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	Date date = new Date();
    	return dateFormat.format(date);
    }
    
    public void DumpArguments(MethodHookParam param, boolean isBeforeHook)
    {
    	String info = "";
    	
    	if(isBeforeHook)
    		info += "[Before] ";
    	else
    		info += "[After] ";
    	
    	for(int i = 0; i < param.args.length; i++)
    	{
    		info += "arg" + i + "=" + param.args[i] + ", ";
    	}
    	
    	XposedBridge.log("[ArgDump][" + param.method.getName() + "][" + param.getClass().getName() + "." + param.method.getName() + "]" + info);
    }
    
    public void DumpArgumentValues(MethodHookParam param, boolean isBeforeHook)
    {
    	String info = "";
    	
    	if(isBeforeHook)
    		info += "[Before] ";
    	else
    		info += "[After] ";
    	
    	for(int i = 0; i < param.args.length; i++)
    	{
    		info += param.args[i] == null ? "NULL" : param.args[i].toString();
    		if(i+1<param.args.length)
    			info += ", ";
    	}
    	Log("[ValueDump][" + param.method.getName() + "]" + info);
    }
    
    public void DumpMethodNames(String ClassName, LoadPackageParam lpparam)
    {
    	Class<?> hookClass = findClass(ClassName, lpparam.classLoader);
		if (hookClass != null)
		{
			for (Method method : hookClass.getDeclaredMethods())
			{
				String Arguments = "";
				
				for(int i = 0; i < method.getParameterTypes().length; i++)
				{
					if(i+1<method.getParameterTypes().length)
						Arguments += method.getParameterTypes()[i].getName() + ", ";
					else
						Arguments += method.getParameterTypes()[i].getName();
				}
				XposedBridge.log("["+ClassName+"] " + method.getReturnType().getName() + "\t " + method.getName() + "(" + Arguments + ")");
			}
		}
		else
		{
			XposedBridge.log("Unable to dump " + ClassName);
		}
    }
    
    public boolean IsHostBlocked(MethodHookParam param, String Host, String IpAddress, int Port)
    {
    	synchronized(SyncLock)
    	{
    		/*Context context = AndroidAppHelper.currentApplication();
    		if(context != null)
    		{
	    		if(AppParam.packageName.toLowerCase().contains("usb") || 
	    		   AppParam.packageName.toLowerCase().contains("mass"))
	    		{
	    			PublicTest++;
	    			Toast toast = Toast.makeText(context, "incremented test", Toast.LENGTH_SHORT);
		    		toast.show();
	    		}
	    		
	    		Toast toast = Toast.makeText(context, "TEST:" + PublicTest, Toast.LENGTH_SHORT);
	    		toast.show();
    		}*/
    		
    		//XposedBridge.log("[" + MainHook.AppParam.packageName + "] Initializing History");
    		if(MainHook.History == null)
    		{
    			try { MainHook.History = new CsvHistory(MainHook.AppParam.packageName); } catch(Exception ex) { }
    		}
    		//XposedBridge.log("[" + MainHook.AppParam.packageName + "] Is history null? " + (MainHook.History == null ? "true" : "false"));
    		
    		//Strangely enough some apps have this...
    		if(MainHook.History == null)
    			return true;
    		
    		if(Settings.getBlockAllTraffic())
    			return true;
    		
    		if((Port == 80 && MainHook.Settings.getAllowHttp()) || (Port == 443 && MainHook.Settings.getAllowHttps()))
    		{
    			return false;
    		}
    		
			if(MainHook.Settings.getBlockAdKeyword())
			{
				if(Host.toLowerCase().contains("ad"))
				{
					return true;
				}
			}
			else
			{
				try
				{
					String IpRange = MainHook.BlockList.IsIpBlocked(IpAddress);
					if(IpRange.length() > 0)
						return true;
				}
				catch (Exception e)
				{
					
				}
			}
			
			//check if hostname is being blocked
			if(MainHook.HostBlocks.ContainsHost(Host))
				return true;
    		
    		return false;
    	}    
    }
}