package com.aviraxp.adblocker.continued.hook;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;

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

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {

        new SelfHook().hook(lpparam);
        new ServicesHook().hook(lpparam);
        new ShortcutHook().hook(lpparam);

        if (lpparam.packageName.equals("android") || PreferencesHelper.isAndroidApp(lpparam.packageName) || PreferencesHelper.disabledApps().contains(lpparam.packageName)) {
            return;
        }

        if (PreferencesHelper.isDisableSystemApps()) {

            Object activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
            Context systemContext = (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");

            try {
                ApplicationInfo info = systemContext.getPackageManager().getApplicationInfo(lpparam.packageName, 0);
                if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    return;
                }
            } catch (Throwable ignored) {
            }
        }

        new ActViewHook().hook(lpparam);
        new BackPressHook().hook(lpparam);
        new HostsHook().hook(lpparam);
        new ReceiversHook().hook(lpparam);
        new WebViewHook().hook(lpparam);
    }

    public void initZygote(StartupParam startupParam) throws Throwable {
        new ActViewHook().init(startupParam);
        new HostsHook().init(startupParam);
        new ReceiversHook().init(startupParam);
        new ServicesHook().init(startupParam);
        new WebViewHook().init(startupParam);
    }
}