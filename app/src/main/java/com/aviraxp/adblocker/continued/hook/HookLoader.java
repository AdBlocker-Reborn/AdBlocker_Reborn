package com.aviraxp.adblocker.continued.hook;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.BlocklistInit;

import java.io.IOException;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookLoader implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    static HashSet<String> actViewList = new HashSet<>();
    static HashSet<String> actViewList_aggressive = new HashSet<>();
    static HashSet<String> actViewList_specific = new HashSet<>();
    static HashSet<String> hostsList = new HashSet<>();
    static HashSet<String> receiversList = new HashSet<>();
    static HashSet<String> servicesList = new HashSet<>();
    static HashSet<String> urlList = new HashSet<>();

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {

        new SelfHook().hook(lpparam);
        new ServicesHook().hook(lpparam);
        new ShortcutHook().hook(lpparam);

        if (PreferencesHelper.isAndroidApp(lpparam.packageName) || PreferencesHelper.isDisabledSystemApp(lpparam) || PreferencesHelper.isWhitelisted(lpparam.packageName)) {
            return;
        }

        new ActViewHook().hook(lpparam);
        new BackPressHook().hook(lpparam);
        new HostsHook().hook(lpparam);
        new ReceiversHook().hook(lpparam);
        new URLHook().hook(lpparam);
        new WebViewHook().hook(lpparam);
    }

    public void initZygote(StartupParam startupParam) throws IOException {
        new BlocklistInit().init(startupParam, "blocklist/av", HookLoader.actViewList);
        new BlocklistInit().init(startupParam, "blocklist/av_specific", HookLoader.actViewList);
        new BlocklistInit().init(startupParam, "blocklist/av_aggressive", HookLoader.actViewList);
        new BlocklistInit().init(startupParam, "blocklist/hosts", HookLoader.actViewList);
        new BlocklistInit().init(startupParam, "blocklist/hosts_yhosts", HookLoader.actViewList);
        new BlocklistInit().init(startupParam, "blocklist/services", HookLoader.actViewList);
        new BlocklistInit().init(startupParam, "blocklist/urls", HookLoader.actViewList);
        new BlocklistInit().init(startupParam, "blocklist/receivers", HookLoader.actViewList);
    }
}