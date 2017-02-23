package com.aviraxp.adblocker.continued.hook;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.wechatdonationhelper.Donation;

import java.util.HashSet;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookLoader implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    static HashSet<String> actViewList;
    static HashSet<String> actViewList_aggressive;
    static HashSet<String> hideList;
    static HashSet<String> hostsList;
    static HashSet<String> hostsList_yhosts;
    static HashSet<String> receiversList;
    static HashSet<String> servicesList;
    static HashSet<String> urlList;

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        new ServicesHook().hook(lpparam);

        if (lpparam.packageName.equals("android") || PreferencesHelper.isAndroidApp(lpparam.packageName)) {
            return;
        }

        Object activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
        Context systemContext = (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
        ApplicationInfo info = systemContext.getPackageManager().getApplicationInfo(lpparam.packageName, 0);

        if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && PreferencesHelper.isDisableSystemApps()) {
            return;
        }

        new ActViewHook().hook(lpparam);
        new BackPressHook().hook(lpparam);
        new HidingHook().hook(lpparam);
        new HostsHook().hook(lpparam);
        new ReceiversHook().hook(lpparam);
        new SelfHook().hook(lpparam);
        new WebViewHook().hook(lpparam);
        Donation.hook(lpparam, "wxid_90m10eigpruz21");
    }

    public void initZygote(StartupParam startupParam) throws Throwable {
        new ActViewHook().init(startupParam);
        new HidingHook().init(startupParam);
        new HostsHook().init(startupParam);
        new ReceiversHook().init(startupParam);
        new ServicesHook().init(startupParam);
        new WebViewHook().init(startupParam);
    }
}