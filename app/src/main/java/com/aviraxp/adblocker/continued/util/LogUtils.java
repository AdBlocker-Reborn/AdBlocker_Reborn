package com.aviraxp.adblocker.continued.util;

import com.aviraxp.adblocker.continued.BuildConfig;

import de.robv.android.xposed.XposedBridge;

public class LogUtils {
    public static void logRecord(Object object) {
        if (object instanceof Throwable) {
            XposedBridge.log((Throwable) object);
        } else if (BuildConfig.DEBUG && object instanceof String) {
            XposedBridge.log((String) object);
        }
    }
}
