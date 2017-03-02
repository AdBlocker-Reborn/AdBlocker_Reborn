package com.aviraxp.adblocker.continued.hook;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.LogUtils;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class ReceiversHook {

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