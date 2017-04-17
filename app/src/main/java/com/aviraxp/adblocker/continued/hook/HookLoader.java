package com.aviraxp.adblocker.continued.hook;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.BlocklistInitUtils;

import java.io.IOException;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookLoader implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    static final HashSet<String> actViewList = new HashSet<>();
    static final HashSet<String> actViewList_aggressive = new HashSet<>();
    static final HashSet<String> actViewList_specific = new HashSet<>();
    static final HashSet<String> hostsList = new HashSet<>();
    static final HashSet<String> receiversList = new HashSet<>();
    static final HashSet<String> servicesList = new HashSet<>();
    static final HashSet<String> urlList = new HashSet<>();

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {

        new SelfHook().hook(lpparam);
        new ServicesHook().hook(lpparam);
        new ShortcutHook().hook(lpparam);

        if (PreferencesHelper.isAndroidApp(lpparam.packageName) || PreferencesHelper.isDisabledSystemApp(lpparam) || PreferencesHelper.isWhitelisted(lpparam.packageName)) {
            return;
        }

        new ActViewHook().hook(lpparam);
        new AntiXposedHook().hook(lpparam);
        new BackPressHook().hook(lpparam);
        new HostsHook().hook(lpparam);
        new ReceiversHook().hook(lpparam);
        new URLHook().hook(lpparam);
        new WebViewHook().hook(lpparam);
    }

    public void initZygote(StartupParam startupParam) throws IOException {
        new BlocklistInitUtils().init(startupParam, "blocklist/av", HookLoader.actViewList);
        new BlocklistInitUtils().init(startupParam, "blocklist/av_aggressive", HookLoader.actViewList_aggressive);
        new BlocklistInitUtils().init(startupParam, "blocklist/av_specific", HookLoader.actViewList_specific);
        new BlocklistInitUtils().init(startupParam, "blocklist/hosts", HookLoader.hostsList);
        new BlocklistInitUtils().init(startupParam, "blocklist/hosts_yhosts", HookLoader.hostsList);
        new BlocklistInitUtils().init(startupParam, "blocklist/services", HookLoader.servicesList);
        new BlocklistInitUtils().init(startupParam, "blocklist/urls", HookLoader.urlList);
        new BlocklistInitUtils().init(startupParam, "blocklist/receivers", HookLoader.receiversList);
    }
}