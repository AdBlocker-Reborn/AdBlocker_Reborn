package com.aviraxp.adblocker.continued.hook;

import android.content.Intent;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class ShortcutHook {
    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {

        XC_MethodHook shortcutHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                Intent intent = (Intent) param.args[1];
                if (intent != null && "com.android.launcher.action.INSTALL_SHORTCUT".equals(intent.getAction())) {
                    param.setResult(0);
                }
            }
        };

        if (PreferencesHelper.isShortcutHookEnabled() && "android".equals(lpparam.packageName)) {
            Class<?> clazz = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", lpparam.classLoader);
            XposedBridge.hookAllMethods(clazz, "broadcastIntent", shortcutHook);
        }
    }
}