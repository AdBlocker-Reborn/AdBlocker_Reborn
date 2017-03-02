package com.aviraxp.adblocker.continued.hook;

import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.content.res.XModuleResources;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;

import java.util.Collections;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookLoader implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    static HashSet<String> actViewList;
    static HashSet<String> actViewList_aggressive;
    static HashSet<String> hostsList;
    static HashSet<String> receiversList;
    static HashSet<String> servicesList;
    static HashSet<String> urlList;

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {

        new SelfHook().hook(lpparam);
        new ServicesHook().hook(lpparam);
        new ShortcutHook().hook(lpparam);

        if (lpparam.packageName.equals("android") || PreferencesHelper.isAndroidApp(lpparam.packageName) || (PreferencesHelper.isDisableSystemApps() && (lpparam.appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) || PreferencesHelper.disabledApps().contains(lpparam.packageName)) {
            return;
        }

        new ActViewHook().hook(lpparam);
        new BackPressHook().hook();
        new HostsHook().hook(lpparam);
        new ReceiversHook().hook(lpparam);
        new WebViewHook().hook(lpparam);
    }

    public void initZygote(StartupParam startupParam) throws Throwable {
        initBlockList(startupParam, "blocklist/av", actViewList);
        initBlockList(startupParam, "blocklist/av_aggressive", actViewList_aggressive);
        initBlockList(startupParam, "blocklist/hosts", hostsList);
        initBlockList(startupParam, "blocklist/receivers", receiversList);
        initBlockList(startupParam, "blocklist/services", servicesList);
        initBlockList(startupParam, "blocklist/urls", urlList);
    }

    private void initBlockList(StartupParam startupParam, String path, HashSet<String> name) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        if (path.equals("blocklist/hosts")) {
            byte[] array = XposedHelpers.assetAsByteArray(res, path);
            byte[] array2 = XposedHelpers.assetAsByteArray(res, "blocklist/hosts_yhosts");
            String decoded = new String(array, "UTF-8");
            String decoded2 = new String(array2, "UTF-8");
            String decoded3 = decoded2.replace("127.0.0.1 ", "").replace("localhost", "workaround");
            String[] sUrls = decoded.split("\n");
            String[] sUrls2 = decoded3.split("\n");
            name = new HashSet<>();
            Collections.addAll(name, sUrls);
            Collections.addAll(name, sUrls2);
        } else {
            byte[] array = XposedHelpers.assetAsByteArray(res, path);
            String decoded = new String(array, "UTF-8");
            String[] sUrls = decoded.split("\n");
            name = new HashSet<>();
            Collections.addAll(name, sUrls);
        }
    }
}