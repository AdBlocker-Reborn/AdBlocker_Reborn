package com.aviraxp.adblocker.continued.util;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageManager;

public class ContextUtils {

    public static Context getSystemContext() {
        return ActivityThread.currentActivityThread().getSystemContext();
    }

    public static Context getOwnContext() {
        try {
            return getSystemContext().createPackageContext("com.aviraxp.adblocker.continued", Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
