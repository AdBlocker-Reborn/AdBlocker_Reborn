package com.aviraxp.adblocker.continued.hook;

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

class ServicesHook {

    private final XC_MethodHook servicesStartHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) {
            Intent intent = (Intent) param.args[1];
            handleServiceStart(param, intent);
        }
    };

    private final XC_MethodHook servicesBindHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) {
            Intent intent = (Intent) param.args[2];
            handleServiceStart(param, intent);
        }
    };

    private static boolean isMIUI() {
        return !SystemProperties.get("ro.miui.ui.version.name", "").equals("");
    }

    void init(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/services");
        String decoded = new String(array, "UTF-8");
        String[] sUrls = decoded.split("\n");
        HookLoader.servicesList = new HashSet<>();
        Collections.addAll(HookLoader.servicesList, sUrls);
        isMIUI();
        LogUtils.logRecord("MIUI Based: " + isMIUI(), true);
    }

    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isServicesHookEnabled()) {
            return;
        }

        if (lpparam.packageName.equals("android")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.am.ActiveServices", lpparam.classLoader), "startServiceLocked", servicesStartHook);
                XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.am.ActiveServices", lpparam.classLoader), "bindServiceLocked", servicesBindHook);
            } else {
                XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.am.ActivityManagerService", lpparam.classLoader), "startServiceLocked", servicesStartHook);
                XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.am.ActivityManagerService", lpparam.classLoader), "bindService", servicesBindHook);
            }
        }
    }

    private void handleServiceStart(XC_MethodHook.MethodHookParam param, Intent serviceIntent) {
        if (serviceIntent != null && serviceIntent.getComponent() != null) {
            String serviceName = serviceIntent.getComponent().flattenToShortString();
            if (serviceName != null) {
                String packageName = serviceName.substring(0, serviceName.indexOf("/"));
                String splitServicesName = serviceName.substring(serviceName.indexOf("/") + 1);
                if (!PreferencesHelper.isAndroidApp(packageName) && !PreferencesHelper.disabledApps().contains(packageName) && !PreferencesHelper.whiteListElements().contains(serviceName) && ((!isMIUI() && HookLoader.servicesList.contains(splitServicesName)) || (isMIUI() && HookLoader.servicesList.contains(splitServicesName) && (!splitServicesName.toLowerCase().contains("xiaomi") || splitServicesName.toLowerCase().contains("ad"))))) {
                    param.setResult(null);
                    LogUtils.logRecord("Service Block Success: " + serviceName, true);
                }
            }
        }
    }
}