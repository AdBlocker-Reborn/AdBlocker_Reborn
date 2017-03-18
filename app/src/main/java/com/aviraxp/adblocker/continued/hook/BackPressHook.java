package com.aviraxp.adblocker.continued.hook;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.LogUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class BackPressHook {

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isBackPressHookEnabled()) {
            return;
        }

        XC_MethodHook backPressHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!(Boolean) param.args[0]) {
                    param.args[0] = true;
                    LogUtils.logRecord("BackPressHook Success: " + lpparam.packageName);
                }
            }
        };

        XposedBridge.hookAllMethods(android.app.Dialog.class, "setCancelable", backPressHook);
        XposedBridge.hookAllMethods(android.app.Dialog.class, "setCanceledOnTouchOutside", backPressHook);
        XposedBridge.hookAllMethods(android.app.AlertDialog.Builder.class, "setCancelable", backPressHook);
        XposedBridge.hookAllMethods(android.app.Activity.class, "setFinishOnTouchOutside", backPressHook);
    }
}