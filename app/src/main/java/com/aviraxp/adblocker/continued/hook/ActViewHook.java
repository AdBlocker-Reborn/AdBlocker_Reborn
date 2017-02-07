package com.aviraxp.adblocker.continued.hook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.LogUtils;

import java.util.Collections;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.aviraxp.adblocker.continued.hook.HookLoader.actViewList;

class ActViewHook {

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isActViewHookEnabled()) {
            return;
        }

        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;
                String activityClassName = activity.getClass().getName();
                if (activityClassName != null && actViewList.contains(activityClassName)) {
                    activity.overridePendingTransition(0, 0);
                    activity.finish();
                    activity.overridePendingTransition(0, 0);
                    LogUtils.logRecord("Activity Block Success: " + lpparam.packageName + "/" + activityClassName, true);
                }
            }
        });

        Object activityObject = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (param.args[0] != null) {
                    ComponentName Component = ((Intent) param.args[0]).getComponent();
                    if (Component != null) {
                        String activityClassName = Component.getClassName();
                        if (activityClassName != null && actViewList.contains(activityClassName)) {
                            param.setResult(null);
                            LogUtils.logRecord("Activity Block Success: " + lpparam.packageName + "/" + activityClassName, true);
                        }
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
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                hideIfAdView(param.thisObject, lpparam.packageName);
            }
        };
        XposedBridge.hookAllConstructors(View.class, (XC_MethodHook) viewObject);
        XposedBridge.hookAllConstructors(ViewGroup.class, (XC_MethodHook) viewObject);
        XposedHelpers.findAndHookMethod(View.class, "setVisibility", Integer.TYPE, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if ((Integer) param.args[0] != 8) {
                    hideIfAdView(param.thisObject, lpparam.packageName);
                }
            }
        });
    }

    void init(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/av");
        String decoded = new String(array, "UTF-8");
        String[] sUrls = decoded.split("\n");
        actViewList = new HashSet<>();
        Collections.addAll(actViewList, sUrls);
    }

    private void hideIfAdView(Object paramObject, String paramString) {
        String str = paramObject.getClass().getName();
        if (str != null && actViewList.contains(str)) {
            ((View) paramObject).setVisibility(View.GONE);
            LogUtils.logRecord("View Block Success: " + paramString + "/" + str, true);
        }
    }
}