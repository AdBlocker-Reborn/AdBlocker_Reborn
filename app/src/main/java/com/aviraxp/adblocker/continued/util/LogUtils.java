package com.aviraxp.adblocker.continued.util;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;

import de.robv.android.xposed.XposedBridge;

public class LogUtils {
    public static void logRecord(String string) {
        if (PreferencesHelper.isDebugModeEnabled()) {
            XposedBridge.log(string);
        }
    }
}
