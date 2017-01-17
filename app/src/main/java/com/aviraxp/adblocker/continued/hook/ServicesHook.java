package com.aviraxp.adblocker.continued.hook;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.os.Build;
import android.os.Environment;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ServicesHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private Set<String> servicesList;
    private boolean isMIUI = false;

    private final XC_MethodHook servicesStartHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            Intent intent = (Intent) param.args[1];
            handleServiceStart(param, intent);
        }
    };

    private void isMIUI() {
        Properties properties = new Properties();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(Environment.getRootDirectory(), "build.prop"));
            properties.load(fileInputStream);
            if (properties.getProperty("ro.miui.ui.version.name") != null || properties.getProperty("ro.miui.ui.version.code") != null || properties.getProperty("ro.miui.internal.storage") != null) {
                isMIUI = true;
            }
            LogUtils.logRecord("MIUI Based: " + isMIUI, false);
        } catch (Throwable t) {
            LogUtils.logRecord("Load System Property Failed, Printing StackTrace", false);
            LogUtils.logRecord(t, false);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Throwable t) {
                    LogUtils.logRecord(t, false);
                }
            }
        }
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

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
                if ((!isMIUI && servicesList.contains(splitServicesName)) || (isMIUI && servicesList.contains(splitServicesName) && (!splitServicesName.toLowerCase().contains("xiaomi") || splitServicesName.toLowerCase().contains("ad")))) {
                    param.setResult(null);
                    LogUtils.logRecord("Service Block Success: " + serviceName, true);
                }
            }
        }
    }

    public void initZygote(StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/services");
        String decoded = new String(array, "UTF-8");
        String[] sUrls = decoded.split("\n");
        servicesList = new HashSet<>();
        Collections.addAll(servicesList, sUrls);
        isMIUI();
    }
}