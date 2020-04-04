package com.aviraxp.adblocker.continued.util;

import android.app.ActivityThread;
import android.content.Context;

public class ContextUtils {
    public static Context getSystemContext() {
        return ActivityThread.currentActivityThread().getSystemContext();
    }
}
