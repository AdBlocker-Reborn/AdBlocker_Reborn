package com.aviraxp.adblocker.continued.util;

import android.content.Context;
import android.content.pm.PackageManager;

import de.robv.android.xposed.XposedHelpers;

public class ContextUtils {
    public static Context getOwnContext() {
        try {
            Object activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
            Context systemContext = (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
            return systemContext.createPackageContext("com.aviraxp.adblocker.continued", Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }
    }
}
