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
import com.aviraxp.adblocker.continued.util.ContextUtils;
import com.aviraxp.adblocker.continued.util.LogUtils;
import com.aviraxp.adblocker.continued.util.NotificationUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class ActViewHook {

    void init(IXposedHookZygoteInit.StartupParam startupParam) throws IOException {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/av");
        byte[] array2 = XposedHelpers.assetAsByteArray(res, "blocklist/av_aggressive");
        byte[] array3 = XposedHelpers.assetAsByteArray(res, "blocklist/av_specific");
        String decoded = new String(array, "UTF-8");
        String decoded2 = new String(array2, "UTF-8");
        String decoded3 = new String(array3, "UTF-8");
        String[] sUrls = decoded.split("\n");
        String[] sUrls2 = decoded2.split("\n");
        String[] sUrls3 = decoded3.split("\n");
        HookLoader.actViewList = new HashSet<>();
        HookLoader.actViewList_aggressive = new HashSet<>();
        HookLoader.actViewList_specific = new HashSet<>();
        Collections.addAll(HookLoader.actViewList, sUrls);
        Collections.addAll(HookLoader.actViewList_aggressive, sUrls2);
        Collections.addAll(HookLoader.actViewList_specific, sUrls3);
    }

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isActViewHookEnabled()) {
            return;
        }

        XC_MethodHook activityStartHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0] != null) {
                    ComponentName Component = ((Intent) param.args[0]).getComponent();
                    if (Component != null) {
                        String activityClassName = Component.getClassName();
                        if (activityClassName != null && !PreferencesHelper.whiteListElements().contains(activityClassName) && (HookLoader.actViewList.contains(activityClassName) || isSpecificBlock(lpparam.packageName, activityClassName) || PreferencesHelper.isAggressiveHookEnabled() && isAggressiveBlock(activityClassName))) {
                            param.setResult(null);
                            LogUtils.logRecord("Activity Block Success: " + lpparam.packageName + "/" + activityClassName);
                            NotificationUtils.setNotify(ContextUtils.getOwnContext());
                        }
                    }
                }
            }
        };

        XC_MethodHook viewHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                hideIfAdView((View) param.thisObject, lpparam.packageName);
            }
        };

        XC_MethodHook visibilityHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if ((Integer) param.args[0] != 8) {
                    hideIfAdView((View) param.thisObject, lpparam.packageName);
                }
            }
        };

        XposedHelpers.findAndHookMethod(ContextWrapper.class, "startActivity", Intent.class, activityStartHook);
        XposedHelpers.findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, int.class, activityStartHook);
        XposedHelpers.findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, int.class, Bundle.class, activityStartHook);
        XposedHelpers.findAndHookMethod(View.class, "setVisibility", int.class, visibilityHook);
        XposedBridge.hookAllConstructors(View.class, viewHook);
        XposedBridge.hookAllConstructors(ViewGroup.class, viewHook);
    }

    private boolean isAggressiveBlock(String string) {
        for (String listItem : HookLoader.actViewList_aggressive) {
            if (string.contains(listItem)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSpecificBlock(String string, String string2) {
        for (String listItem : HookLoader.actViewList_specific) {
            if (listItem.startsWith(string) && listItem.endsWith(string2)) {
                return true;
            }
        }
        return false;
    }

    private void hideIfAdView(View paramView, String paramString) {
        String viewName = paramView.getClass().getName();
        if (viewName != null && !PreferencesHelper.whiteListElements().contains(viewName) && (HookLoader.actViewList.contains(viewName) || isSpecificBlock(paramString, viewName) || PreferencesHelper.isAggressiveHookEnabled() && isAggressiveBlock(viewName))) {
            paramView.clearAnimation();
            paramView.setVisibility(View.GONE);
            LogUtils.logRecord("View Block Success: " + paramString + "/" + viewName);
            NotificationUtils.setNotify(ContextUtils.getOwnContext());
        }
    }
}