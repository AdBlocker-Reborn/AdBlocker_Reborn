package com.aviraxp.adblocker.continued;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ActViewHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private Set<String> patterns;

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param)
                    throws Throwable {
                Activity activity = (Activity) param.thisObject;
                String activityClassName = activity.getClass().getName();
                if ((activityClassName != null) && (!activityClassName.startsWith("android")) && (patterns.contains(activityClassName))) {
                    activity.overridePendingTransition(0, 0);
                    activity.finish();
                    activity.overridePendingTransition(0, 0);
                    if (BuildConfig.DEBUG) {
                        XposedBridge.log("Activity Block Success: " + lpparam.packageName + "/" + activityClassName);
                    }
                }
            }
        });

        Object activityObject = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param)
                    throws Throwable {
                ComponentName Component = ((Intent) param.args[0]).getComponent();
                String activityClassName = null;
                if (Component != null) {
                    activityClassName = Component.getClassName();
                }
                if ((activityClassName != null) && (!activityClassName.startsWith("android")) && (patterns.contains(activityClassName))) {
                    param.setResult(null);
                    if (BuildConfig.DEBUG) {
                        XposedBridge.log("Activity Block Success: " + lpparam.packageName + "/" + activityClassName);
                    }
                }
            }
        };
        XposedHelpers.findAndHookMethod(Activity.class, "startActivity", Intent.class, activityObject);
        XposedHelpers.findAndHookMethod(ContextWrapper.class, "startActivity", Intent.class, activityObject);
        XposedHelpers.findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, Integer.TYPE, activityObject);
        XposedHelpers.findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, Integer.TYPE, Bundle.class, activityObject);

        Object viewObject = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param)
                    throws Throwable {
                hideIfAdView(param.thisObject, lpparam.packageName);
            }
        };
        XposedBridge.hookAllConstructors(View.class, (XC_MethodHook) viewObject);
        XposedBridge.hookAllConstructors(ViewGroup.class, (XC_MethodHook) viewObject);
        XposedHelpers.findAndHookMethod(View.class, "setVisibility", Integer.TYPE, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param)
                    throws Throwable {
                if ((Integer) param.args[0] != 8) {
                    hideIfAdView(param.thisObject, lpparam.packageName);
                }
            }
        });
    }

    public void initZygote(StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/av");
        String decoded = new String(array);
        String[] sUrls = decoded.split("\n");
        patterns = new HashSet<>();
        Collections.addAll(patterns, sUrls);
    }

    private void hideIfAdView(Object paramObject, String paramString) {
        String str = paramObject.getClass().getName();
        if ((str != null) && (!str.startsWith("android")) && ((patterns.contains(str)))) {
            ((View) paramObject).setVisibility(View.GONE);
            if (BuildConfig.DEBUG) {
                XposedBridge.log("View Block Success: " + paramString + "/" + str);
            }
        }
    }
}