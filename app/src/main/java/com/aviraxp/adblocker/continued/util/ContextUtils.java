package com.aviraxp.adblocker.continued.util;

import android.content.Context;
import android.content.pm.PackageManager;

import de.robv.android.xposed.XposedHelpers;

public class ContextUtils {

    public static Context getSystemContext() {
        Object activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
        return (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
    }

    public static Context getOwnContext() {
        try {
            return getSystemContext().createPackageContext("com.aviraxp.adblocker.continued", Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }
    }
}
