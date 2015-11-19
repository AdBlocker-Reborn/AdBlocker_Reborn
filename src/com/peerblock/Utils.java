package com.peerblock;

import java.lang.reflect.Method;
import java.security.MessageDigest;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

public class Utils
{
	public static String PeerLocalIp = "127.0.0.1";
	public static int PeerLocalPort = 6005;
	
	public static boolean isXposedEnabled()
	{
		return false;
	}
	
	public static Application getCurrentApplication()
	{
		try
		{
			final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
		    final Method method = activityThreadClass.getMethod("currentApplication");
		    return (Application) method.invoke(null, (Object[]) null);
		}
		catch(Exception ex)
		{
			return null;
		}
	}
	
	public static long GetUniqueAppId(Context context)
	{
		if(context == null)
			return 0;
		
        try
        {
        	ApplicationInfo inf = context.getApplicationInfo();
        	MessageDigest mdSha1 = MessageDigest.getInstance("SHA-1");
        	mdSha1.update((inf.packageName + inf.uid + inf.targetSdkVersion).getBytes("ASCII"));
        	byte[] data = mdSha1.digest();

        	long num1 = BitConverter.toInt64(data,  0);
        	long num2 = BitConverter.toInt64(data,  8);
        	int num3 = BitConverter.toInt32(data,  16);
        	
        	//convert the 20bytes into 6
        	return num1 + num2 + num3;
        }
        catch (Exception e1)
        {
        	return 0;
        }
	}
}