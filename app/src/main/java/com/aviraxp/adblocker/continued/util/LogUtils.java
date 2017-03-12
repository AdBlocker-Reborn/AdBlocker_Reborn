package com.aviraxp.adblocker.continued.util;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;

import de.robv.android.xposed.XposedBridge;

public class LogUtils {
    public static void logRecord(String string, Boolean bool) {
        if ((!PreferencesHelper.isDebugModeEnabled() && !bool) || PreferencesHelper.isDebugModeEnabled()) {
                XposedBridge.log(string);
        }
    }
}
