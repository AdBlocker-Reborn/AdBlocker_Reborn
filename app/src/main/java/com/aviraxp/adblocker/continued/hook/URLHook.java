package com.aviraxp.adblocker.continued.hook;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.LogUtils;

import java.net.MalformedURLException;
import java.net.URL;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class URLHook {

    private static String httpsTweaker(String string) {
        if (string.startsWith("https")) {
            return "https://127.0.0.1";
        } else {
            return "http://127.0.0.1";
        }
    }

    private static String lengthTweaker(XC_MethodHook.MethodHookParam param) {
        String host;
        if (param.args.length == 1) {
            host = (String) param.args[0];
        } else if (param.args.length == 2) {
            URL urlOrigin = (URL) param.args[0];
            if (urlOrigin != null) {
                host = urlOrigin.toString();
            } else {
                host = null;
            }
        } else {
            host = (String) param.args[1];
        }
        return host;
    }

    private static String urlLengthTweaker(XC_MethodHook.MethodHookParam param) {
        String url;
        if (param.args.length == 2) {
            if (lengthTweaker(param) != null) {
                url = lengthTweaker(param) + param.args[1];
            } else {
                url = (String) param.args[1];
            }
        } else if (param.args.length == 3) {
            url = (String) param.args[2];
        } else {
            url = (String) param.args[3];
        }
        return url;
    }

    private static void lengthSetter(XC_MethodHook.MethodHookParam param, String string) throws MalformedURLException {
        if (param.args.length == 1) {
            param.args[0] = httpsTweaker(string);
        } else if (param.args.length == 2) {
            URL url = new URL(httpsTweaker(string));
            param.args[0] = url;
        } else {
            param.args[1] = httpsTweaker(string);
        }
    }

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isURLHookEnabled()) {
            return;
        }

        XC_MethodHook urlHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws MalformedURLException {
                String url = urlLengthTweaker(param);
                if (url != null) {
                    for (String adUrl : HookLoader.urlList) {
                        if (lengthTweaker(param) != null && url.contains(adUrl) && !PreferencesHelper.whiteListElements().contains(url)) {
                            lengthSetter(param, lengthTweaker(param));
                            LogUtils.logRecord("URL Block Success: " + lpparam.packageName + "/" + url);
                            return;
                        }
                    }
                }
            }
        };

        XC_MethodHook hostsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws MalformedURLException {
                String url = lengthTweaker(param);
                if (url != null && url.startsWith("http")) {
                    String urlCutting = url.substring(url.indexOf("://") + 3);
                    for (String adUrl : HookLoader.hostsList) {
                        if (urlCutting.startsWith(adUrl) && !PreferencesHelper.whiteListElements().contains(url)) {
                            lengthSetter(param, url);
                            LogUtils.logRecord("URL Block Success: " + lpparam.packageName + "/" + url);
                            return;
                        }
                    }
                }
            }
        };

        XposedHelpers.findAndHookConstructor(URL.class, String.class, hostsHook);
        XposedHelpers.findAndHookConstructor(URL.class, URL.class, String.class, hostsHook);
        XposedHelpers.findAndHookConstructor(URL.class, URL.class, String.class, urlHook);
        XposedHelpers.findAndHookConstructor(URL.class, String.class, String.class, String.class, urlHook);
        XposedHelpers.findAndHookConstructor(URL.class, String.class, String.class, String.class, hostsHook);
        XposedHelpers.findAndHookConstructor(URL.class, String.class, String.class, int.class, String.class, hostsHook);
        XposedHelpers.findAndHookConstructor(URL.class, String.class, String.class, int.class, String.class, urlHook);
    }
}
