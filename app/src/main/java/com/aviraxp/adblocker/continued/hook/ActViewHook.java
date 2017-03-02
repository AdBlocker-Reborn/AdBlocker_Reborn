package com.aviraxp.adblocker.continued.hook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.LogUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class ActViewHook {

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isActViewHookEnabled()) {
            return;
        }

        XC_MethodHook activityCreateHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;
                String activityClassName = activity.getClass().getName();
                if (activityClassName != null && !PreferencesHelper.whiteListElements().contains(activityClassName) && (HookLoader.actViewList.contains(activityClassName) || (PreferencesHelper.isAggressiveHookEnabled() && isAggressiveBlock(activityClassName)))) {
                    activity.overridePendingTransition(0, 0);
                    activity.finish();
                    activity.overridePendingTransition(0, 0);
                    LogUtils.logRecord("Activity Block Success: " + lpparam.packageName + "/" + activityClassName, true);
                }
            }
        };

        XC_MethodHook activityStartHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (param.args[0] != null) {
                    ComponentName Component = ((Intent) param.args[0]).getComponent();
                    if (Component != null) {
                        String activityClassName = Component.getClassName();
                        if (activityClassName != null && !PreferencesHelper.whiteListElements().contains(activityClassName) && (HookLoader.actViewList.contains(activityClassName) || (PreferencesHelper.isAggressiveHookEnabled() && isAggressiveBlock(activityClassName)))) {
                            param.setResult(null);
                            LogUtils.logRecord("Activity Block Success: " + lpparam.packageName + "/" + activityClassName, true);
                        }
                    }
                }
            }
        };

        XC_MethodHook viewHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                hideIfAdView((View) param.thisObject, lpparam.packageName);
            }
        };

        XC_MethodHook visibilityHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
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
        if (str != null && !PreferencesHelper.whiteListElements().contains(str) && (HookLoader.actViewList.contains(str) || (PreferencesHelper.isAggressiveHookEnabled() && isAggressiveBlock(str)))) {
            paramView.clearAnimation();
            paramView.setVisibility(View.GONE);
            LogUtils.logRecord("View Block Success: " + paramString + "/" + str, true);
        }
    }
}