package com.aviraxp.adblocker.continued;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.os.Build;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ServicesHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private Set<String> patterns;

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("android")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                XposedHelpers.findAndHookMethod("com.android.server.am.ActiveServices", lpparam.classLoader, "startServiceLocked", "android.app.IApplicationThread", Intent.class, String.class, int.class, int.class, String.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Intent intent = (Intent) param.args[1];
                        handleServiceStart(param, intent);
                        if (BuildConfig.DEBUG) {
                            XposedBridge.log("Hook Services Flag Success:" + Build.VERSION.SDK_INT);
                        }
                    }
                });
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                try {
                    XposedHelpers.findAndHookMethod("com.android.server.am.ActiveServices", lpparam.classLoader, "startServiceLocked", "android.app.IApplicationThread", Intent.class, String.class, int.class, int.class, int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Intent intent = (Intent) param.args[1];
                            handleServiceStart(param, intent);
                            if (BuildConfig.DEBUG) {
                                XposedBridge.log("Hook Services Flag Success:" + Build.VERSION.SDK_INT);
                            }
                        }
                    });
                } catch (NoSuchMethodError e) {
                    XposedHelpers.findAndHookMethod("com.android.server.am.ActiveServices", lpparam.classLoader, "startServiceLocked", "android.app.IApplicationThread", Intent.class, String.class, int.class, int.class, int.class, Context.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Intent intent = (Intent) param.args[1];
                            handleServiceStart(param, intent);
                            if (BuildConfig.DEBUG) {
                                XposedBridge.log("Hook Services Flag Success:" + Build.VERSION.SDK_INT);
                            }
                        }
                    });
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                XposedHelpers.findAndHookMethod("com.android.server.am.ActivityManagerService", lpparam.classLoader, "startServiceLocked", "android.app.IApplicationThread", Intent.class, String.class, int.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Intent intent = (Intent) param.args[1];
                        handleServiceStart(param, intent);
                        if (BuildConfig.DEBUG) {
                            XposedBridge.log("Hook Services Flags Success:" + Build.VERSION.SDK_INT);
                        }
                    }
                });
            }
        }
    }

    private void handleServiceStart(XC_MethodHook.MethodHookParam param, Intent serviceIntent) {
        if (serviceIntent != null && serviceIntent.getComponent() != null) {
            String serviceName = serviceIntent.getComponent().flattenToShortString();
            if (serviceName != null) {
                String splitServicesName = serviceName.substring(serviceName.indexOf("/") + 1);
                if (patterns.contains(splitServicesName)) {
                    param.setResult(null);
                    if (BuildConfig.DEBUG) {
                        XposedBridge.log("Services Block Success:" + serviceName);
                    }
                }
            }
        }
    }

    public void initZygote(StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "services");
        String decoded = new String(array);
        String[] sUrls = decoded.split("\n");
        patterns = new HashSet<>();
        Collections.addAll(patterns, sUrls);
    }
}