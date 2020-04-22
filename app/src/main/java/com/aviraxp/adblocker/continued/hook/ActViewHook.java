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

        XC_MethodHook activityStartHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0] != null) {
                    ComponentName Component = ((Intent) param.args[0]).getComponent();
                    if (Component != null) {
                        String activityClassName = Component.getClassName();
                        if (activityClassName != null && (HookLoader.actViewList.contains(activityClassName) || PreferencesHelper.isAggressiveHookEnabled() && isAggressiveBlock(activityClassName)) && !PreferencesHelper.whiteListElements().contains(activityClassName)) {
                            param.setResult(null);
                            LogUtils.logRecord("Activity Block Success: " + lpparam.packageName + "/" + activityClassName);
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
        String viewName = paramView.getClass().getName();
        if (PreferencesHelper.whiteListElements().contains(viewName)) {
            return;
        }
        if (PreferencesHelper.isAggressiveHookEnabled() && isAggressiveBlock(viewName) || HookLoader.actViewList.contains(viewName)) {
            paramView.clearAnimation();
            paramView.setVisibility(View.GONE);
            LogUtils.logRecord("View Block Success: " + paramString + "/" + viewName);
        }
    }
}