package com.aviraxp.adblocker.continued.util;

import de.robv.android.xposed.XC_MethodHook;

public class SecureSocketUtils {
    public static void determineHttps(XC_MethodHook.MethodHookParam param, int i, String string) {
        if (string.startsWith("https")) {
            param.args[i] = "https://localhost";
        } else {
            param.args[i] = "http://localhost";
        }
    }
}
