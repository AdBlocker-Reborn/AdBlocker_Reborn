package com.aviraxp.adblocker.continued;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XModuleResources;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ReceiversHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private Set<String> receiversList;

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!PreferencesHelper.isReceiversHookEnabled()) {
            return;
        }

        try {
            for (String receivers : receiversList) {
                XposedHelpers.findAndHookMethod(receivers, lpparam.classLoader, "onReceive", Context.class, Intent.class, XC_MethodReplacement.DO_NOTHING);
                if (BuildConfig.DEBUG) {
                    XposedBridge.log("Receiver Block Success: " + lpparam.packageName + "/" + receivers);
                }
            }
        } catch (XposedHelpers.ClassNotFoundError ignored) {
        }
    }

    public void initZygote(StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/receivers");
        String decoded = new String(array, "UTF-8");
        String[] sUrls = decoded.split("\n");
        receiversList = new HashSet<>();
        Collections.addAll(receiversList, sUrls);
    }
}