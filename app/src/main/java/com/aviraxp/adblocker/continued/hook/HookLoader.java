package com.aviraxp.adblocker.continued.hook;

import com.aviraxp.wechatdonationhelper.Donation;

import java.util.HashSet;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
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
        new ActViewHook().hook(lpparam);
        new BackPressHook().hook(lpparam);
        new HidingHook().hook(lpparam);
        new HostsHook().hook(lpparam);
        new ReceiversHook().hook(lpparam);
        new SelfHook().hook(lpparam);
        new ServicesHook().hook(lpparam);
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