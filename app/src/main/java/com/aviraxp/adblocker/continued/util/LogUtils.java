package com.aviraxp.adblocker.continued.util;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;

import de.robv.android.xposed.XposedBridge;

public class LogUtils {
    public static void logRecord(Object obj, Boolean bool) {
        if ((!PreferencesHelper.isDebugModeEnabled() && !bool) || PreferencesHelper.isDebugModeEnabled()) {
            if (obj instanceof String) {
                XposedBridge.log((String) obj);
            } else if (obj instanceof Throwable) {
                XposedBridge.log((Throwable) obj);
            }
        }
    }
}
