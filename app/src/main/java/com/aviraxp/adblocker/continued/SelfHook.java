package com.aviraxp.adblocker.continued;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SelfHook implements IXposedHookLoadPackage {
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.aviraxp.adblocker.continued")) {
            Class<?> SettingsActivity = XposedHelpers.findClass("com.aviraxp.adblocker.continued.ui.SettingsActivity", lpparam.classLoader);
            XposedHelpers.setStaticBooleanField(SettingsActivity, "isActivated", true);
        }
    }
}
