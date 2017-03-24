package com.aviraxp.adblocker.continued.hook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
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
        String decoded = new String(array, "UTF-8");
        String decoded2 = new String(array2, "UTF-8");
        String[] sUrls = decoded.split("\n");
        String[] sUrls2 = decoded2.split("\n");
        HookLoader.actViewList = new HashSet<>();
        HookLoader.actViewList_aggressive = new HashSet<>();
        Collections.addAll(HookLoader.actViewList, sUrls);
        Collections.addAll(HookLoader.actViewList_aggressive, sUrls2);
    }

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isActViewHookEnabled()) {
            return;
        }

        XC_MethodHook activityCreateHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;
                String activityClassName = activity.getClass().getName();
                if (activityClassName != null && !PreferencesHelper.whiteListElements().contains(activityClassName) && (HookLoader.actViewList.contains(activityClassName) || PreferencesHelper.isAggressiveHookEnabled() && isAggressiveBlock(activityClassName))) {
                    activity.overridePendingTransition(0, 0);
                    activity.finish();
                    activity.overridePendingTransition(0, 0);
                    LogUtils.logRecord("Activity Block Success: " + lpparam.packageName + "/" + activityClassName);
                    NotificationUtils.setNotify(ContextUtils.getOwnContext());
                }
            }
        };

        XC_MethodHook activityStartHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] != null) {
                    ComponentName Component = ((Intent) param.args[0]).getComponent();
                    if (Component != null) {
                        String activityClassName = Component.getClassName();
                        if (activityClassName != null && !PreferencesHelper.whiteListElements().contains(activityClassName) && (HookLoader.actViewList.contains(activityClassName) || PreferencesHelper.isAggressiveHookEnabled() && isAggressiveBlock(activityClassName))) {
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
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                hideIfAdView((View) param.thisObject, lpparam.packageName);
            }
        };

        XC_MethodHook visibilityHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if ((Integer) param.args[0] != 8) {
                    hideIfAdView((View) param.thisObject, lpparam.packageName);
                }
            }
        };

        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, activityCreateHook);
        XposedHelpers.findAndHookMethod(Activity.class, "startActivity", Intent.class, activityStartHook);
        XposedHelpers.findAndHookMethod(ContextWrapper.class, "startActivity", Intent.class, activityStartHook);
        XposedHelpers.findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, int.class, activityStartHook);
        XposedHelpers.findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, int.class, Bundle.class, activityStartHook);
        XposedHelpers.findAndHookMethod(View.class, "setVisibility", int.class, visibilityHook);
        XposedBridge.hookAllConstructors(View.class, viewHook);
        XposedBridge.hookAllConstructors(ViewGroup.class, viewHook);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            XposedHelpers.findAndHookMethod(Activity.class, "startActivityAsUser", Intent.class, Bundle.class, UserHandle.class, activityStartHook);
            XposedHelpers.findAndHookMethod(Activity.class, "startActivityForResultAsUser", Intent.class, int.class, Bundle.class, UserHandle.class, activityStartHook);
        }
    }

    private boolean isAggressiveBlock(String string) {
        for (String listItem : HookLoader.actViewList_aggressive) {
            if (string.contains(listItem)) {
                return true;
            }
        }
        return false;
    }

    private void hideIfAdView(View paramView, String paramString) {
        String str = paramView.getClass().getName();
        if (str != null && !PreferencesHelper.whiteListElements().contains(str) && (HookLoader.actViewList.contains(str) || PreferencesHelper.isAggressiveHookEnabled() && isAggressiveBlock(str))) {
            paramView.clearAnimation();
            paramView.setVisibility(View.GONE);
            LogUtils.logRecord("View Block Success: " + paramString + "/" + str);
            NotificationUtils.setNotify(ContextUtils.getOwnContext());
        }
    }
}