package com.aviraxp.adblocker.continued.hook;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XModuleResources;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class ReceiversHook {

    void init(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/receivers");
        String decoded = new String(array, "UTF-8");
        String[] sUrls = decoded.split("\n");
        HookLoader.receiversList = new HashSet<>();
        Collections.addAll(HookLoader.receiversList, sUrls);
    }

    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isReceiversHookEnabled()) {
            return;
        }

        ArrayList<String> arrayReceivers = new ArrayList<>();
        Object activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
        Context systemContext = (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
        ActivityInfo[] receiverInfo = new ActivityInfo[0];

        try {
            receiverInfo = systemContext.getPackageManager().getPackageInfo(lpparam.packageName, PackageManager.GET_RECEIVERS).receivers;
        } catch (Throwable ignored) {
        }

        if (receiverInfo != null) {
            for (ActivityInfo info : receiverInfo) {
                arrayReceivers.add(info.name);
            }
        }

        for (String checkReceiver : HookLoader.receiversList) {
            if (!PreferencesHelper.whiteListElements().contains(checkReceiver) && arrayReceivers.contains(checkReceiver)) {
                XposedHelpers.findAndHookMethod(checkReceiver, lpparam.classLoader, "onReceive", Context.class, Intent.class, XC_MethodReplacement.DO_NOTHING);
                LogUtils.logRecord("Receiver Block Success: " + lpparam.packageName + "/" + checkReceiver, true);
            }
        }
    }
}