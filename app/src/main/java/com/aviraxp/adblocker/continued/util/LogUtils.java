package com.aviraxp.adblocker.continued.util;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;

import de.robv.android.xposed.XposedBridge;

public class LogUtils {
    public static void logRecord(Object object) {
        if (object instanceof Throwable) {
            XposedBridge.log((Throwable) object);
        } else if (PreferencesHelper.isDebugModeEnabled() && object instanceof String) {
            XposedBridge.log((String) object);
        }
    }
}
