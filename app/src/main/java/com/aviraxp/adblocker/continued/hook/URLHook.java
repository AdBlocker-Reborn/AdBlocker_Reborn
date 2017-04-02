package com.aviraxp.adblocker.continued.hook;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.ContextUtils;
import com.aviraxp.adblocker.continued.util.LogUtils;
import com.aviraxp.adblocker.continued.util.NotificationUtils;

import java.net.URL;

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
                if (url != null && url.startsWith("http")) {
                    String urlCutting = url.substring(url.indexOf("://") + 3);
                    for (String host : HookLoader.hostsList) {
                        if (urlCutting.startsWith(host) && !PreferencesHelper.whiteListElements().contains(host)) {
                            if (param.args.length == 1) {
                                determineHttps(param, 0, url);
                            } else {
                                determineHttps(param, 1, url);
                            }
                            LogUtils.logRecord("URL Block Success: " + lpparam.packageName + "/" + host);
                            NotificationUtils.setNotify(ContextUtils.getOwnContext());
                            return;
                        }
                    }
                    for (String adUrl : HookLoader.urlList) {
                        if (urlCutting.contains(adUrl) && !PreferencesHelper.whiteListElements().contains(url)) {
                            if (param.args.length == 1) {
                                determineHttps(param, 0, url);
                            } else {
                                determineHttps(param, 1, url);
                            }
                            LogUtils.logRecord("URL Block Success: " + lpparam.packageName + "/" + url);
                            NotificationUtils.setNotify(ContextUtils.getOwnContext());
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
                    NotificationUtils.setNotify(ContextUtils.getOwnContext());
                }
            }
        };

        XposedHelpers.findAndHookConstructor(URL.class, String.class, urlHook);
        XposedHelpers.findAndHookConstructor(URL.class, URL.class, String.class, urlHook);
        XposedHelpers.findAndHookConstructor(URL.class, String.class, String.class, String.class, hostsHook);
        XposedHelpers.findAndHookConstructor(URL.class, String.class, String.class, int.class, String.class, hostsHook);
    }

    private void determineHttps(XC_MethodHook.MethodHookParam param, int i, String string) {
        if (string.startsWith("https")) {
            param.args[i] = "https://localhost";
        } else {
            param.args[i] = "http://localhost";
        }
    }
}
