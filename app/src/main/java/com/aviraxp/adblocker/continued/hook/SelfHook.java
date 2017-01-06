package com.aviraxp.adblocker.continued.hook;

import com.aviraxp.adblocker.continued.BuildConfig;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SelfHook implements IXposedHookLoadPackage {
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) {
            Class<?> SettingsActivity = XposedHelpers.findClass(BuildConfig.APPLICATION_ID + ".ui.SettingsActivity", lpparam.classLoader);
            XposedHelpers.setStaticBooleanField(SettingsActivity, "isActivated", true);
        }
    }
}
