package com.aviraxp.adblocker.continued.hook;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.os.Build;
import android.os.SystemProperties;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.LogUtils;

import java.util.Collections;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.aviraxp.adblocker.continued.hook.HookLoader.servicesList;

class ServicesHook {

    private final XC_MethodHook servicesStartHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            Object activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
            Context systemContext = (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
            for (String i : PreferencesHelper.disabledApps()) {
                int whiteUid = systemContext.getPackageManager().getApplicationInfo(i, 0).uid;
                if ((Integer) param.args[4] == whiteUid) {
                    return;
                }
            }
            Intent intent = (Intent) param.args[1];
            handleServiceStart(param, intent);
        }
    };

    private static boolean isMIUI() {
        return !SystemProperties.get("ro.miui.ui.version.name", "").equals("");
    }

    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isServicesHookEnabled()) {
            return;
        }

        if (lpparam.packageName.equals("android")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.am.ActiveServices", lpparam.classLoader), "startServiceLocked", servicesStartHook);
            } else {
                XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.am.ActivityManagerService", lpparam.classLoader), "startServiceLocked", servicesStartHook);
            }
        }
    }

    private void handleServiceStart(XC_MethodHook.MethodHookParam param, Intent serviceIntent) {
        if (serviceIntent != null && serviceIntent.getComponent() != null) {
            String serviceName = serviceIntent.getComponent().flattenToShortString();
            if (serviceName != null) {
                String splitServicesName = serviceName.substring(serviceName.indexOf("/") + 1);
                if ((!isMIUI() && servicesList.contains(splitServicesName)) || (isMIUI() && servicesList.contains(splitServicesName) && (!splitServicesName.toLowerCase().contains("xiaomi") || splitServicesName.toLowerCase().contains("ad")))) {
                    param.setResult(null);
                    LogUtils.logRecord("Service Block Success: " + serviceName, true);
                }
            }
        }
    }

    void init(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/services");
        String decoded = new String(array, "UTF-8");
        String[] sUrls = decoded.split("\n");
        servicesList = new HashSet<>();
        Collections.addAll(servicesList, sUrls);
        isMIUI();
        LogUtils.logRecord("MIUI Based: " + isMIUI(), true);
    }
}