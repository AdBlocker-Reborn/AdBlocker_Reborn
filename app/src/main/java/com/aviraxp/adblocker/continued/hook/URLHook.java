package com.aviraxp.adblocker.continued.hook;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.DecodeUtils;
import com.aviraxp.adblocker.continued.util.LogUtils;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class URLHook {

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isURLHookEnabled()) {
            return;
        }

        XC_MethodHook urlHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String url;
                if (param.args.length == 1) {
                    url = (String) param.args[0];
                } else {
                    url = (String) param.args[1];
                }
                if (url != null && !PreferencesHelper.whiteListElements().contains(url) && url.startsWith("http")) {
                    String urlCutting = url.substring(url.indexOf("://") + 3);
                    for (String host : HookLoader.hostsList) {
                        if (urlCutting.startsWith(host)) {
                            if (param.args.length == 1) {
                                param.args[0] = "http://localhost";
                            } else {
                                param.args[1] = "http://localhost";
                            }
                            LogUtils.logRecord("URL Block Success: " + lpparam.packageName + "/" + url);
                            return;
                        }
                    }
                    for (String adUrl : HookLoader.urlList) {
                        if (urlCutting.contains(adUrl)) {
                            if (param.args.length == 1) {
                                param.args[0] = "http://localhost";
                            } else {
                                param.args[1] = "http://localhost";
                            }
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
                    param.args[1] = "localhost";
                    LogUtils.logRecord("URL Block Success: " + lpparam.packageName + "/" + host);
                }
            }
        };

        XC_MethodHook urlConnectionHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws MalformedURLException {
                URL url = (URL) param.args[0];
                if (url != null) {
                    String host = url.getHost();
                    if (host != null && !PreferencesHelper.whiteListElements().contains(host) && HookLoader.hostsList.contains(host)) {
                        param.args[0] = new URL("http://localhost");
                        return;
                    }
                    String urlDecode = DecodeUtils.decode(url.toString(), null);
                    if (urlDecode != null) {
                        for (String adUrl : HookLoader.urlList) {
                            if (urlDecode.contains(adUrl)) {
                                param.args[0] = new URL("http://localhost");
                                return;
                            }
                        }
                    }
                }
            }
        };

        XposedHelpers.findAndHookConstructor(URL.class, String.class, urlHook);
        XposedHelpers.findAndHookConstructor(URL.class, URL.class, String.class, urlHook);
        XposedHelpers.findAndHookConstructor(URL.class, String.class, String.class, String.class, hostsHook);
        XposedHelpers.findAndHookConstructor(URL.class, String.class, String.class, int.class, String.class, hostsHook);
        XposedHelpers.findAndHookConstructor(URLConnection.class, URL.class, urlConnectionHook);
        XposedHelpers.findAndHookConstructor(HttpURLConnection.class, URL.class, urlConnectionHook);
    }
}
