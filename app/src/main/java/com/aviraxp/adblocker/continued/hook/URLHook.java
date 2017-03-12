package com.aviraxp.adblocker.continued.hook;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.LogUtils;

import java.net.MalformedURLException;
import java.net.URL;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class URLHook {

    private static final String BLOCK_MESSAGE = "Blocked by AdBlocker Reborn: ";
    private String url = null;

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        XC_MethodHook urlHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args.length == 1) {
                    url = (String) param.args[0];
                } else {
                    url = (String) param.args[1];
                }
                if (url != null && url.startsWith("http")) {
                    String urlCutting = url.substring(url.indexOf("://") + 3);
                    for (String host : HookLoader.hostsList) {
                        if (urlCutting.startsWith(host)) {
                            param.setResult(null);
                            param.setThrowable(new MalformedURLException(BLOCK_MESSAGE + url));
                            LogUtils.logRecord("URL Block Success: " + lpparam.packageName + "/" + url);
                            return;
                        }
                    }
                    for (String adUrl : HookLoader.urlList) {
                        if (urlCutting.contains(adUrl)) {
                            param.setResult(null);
                            param.setThrowable(new MalformedURLException(BLOCK_MESSAGE + url));
                            LogUtils.logRecord("URL Block Success: " + lpparam.packageName + "/" + url);
                            return;
                        }
                    }
                }
            }
        };

        XC_MethodHook hostsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String host = (String) param.args[1];
                if (host != null && !PreferencesHelper.whiteListElements().contains(host) && HookLoader.hostsList.contains(host)) {
                    param.setResult(null);
                    param.setThrowable(new MalformedURLException(BLOCK_MESSAGE + host));
                    LogUtils.logRecord("URL Block Success: " + lpparam.packageName + "/" + host);
                }
            }
        };
        XposedHelpers.findAndHookConstructor(URL.class, String.class, urlHook);
        XposedHelpers.findAndHookConstructor(URL.class, URL.class, String.class, urlHook);
        XposedHelpers.findAndHookConstructor(URL.class, String.class, String.class, int.class, String.class, hostsHook);
        XposedHelpers.findAndHookConstructor(URL.class, String.class, String.class, String.class, hostsHook);
    }
}
