package com.aviraxp.adblocker.continued.hook;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.ContextUtils;
import com.aviraxp.adblocker.continued.util.LogUtils;
import com.aviraxp.adblocker.continued.util.NotificationUtils;

import java.net.MalformedURLException;
import java.net.URL;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class URLHook {

    private static String lengthTweaker(XC_MethodHook.MethodHookParam param) {
        String host;
        if (param.args.length == 1) {
            host = (String) param.args[0];
        } else if (param.args.length == 2) {
            URL urlOrigin = (URL) param.args[0];
            host = urlOrigin.toString();
        } else {
            host = (String) param.args[1];
        }
        return host;
    }

    private static String urlLengthTweaker(XC_MethodHook.MethodHookParam param) {
        String url;
        if (param.args.length == 2) {
            url = (String) param.args[1];
        } else if (param.args.length == 3) {
            url = (String) param.args[2];
        } else {
            url = (String) param.args[3];
        }
        return url;
    }

    private static void lengthSetter(XC_MethodHook.MethodHookParam param) throws MalformedURLException {
        if (param.args.length == 1) {
            param.args[0] = "127.0.0.1";
        } else if (param.args.length == 2) {
            URL url = new URL("127.0.0.1");
            param.args[0] = url;
        } else {
            param.args[1] = "127.0.0.1";
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
                        if (url.contains(adUrl) && !PreferencesHelper.whiteListElements().contains(url)) {
                            lengthSetter(param);
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
            protected void beforeHookedMethod(MethodHookParam param) throws MalformedURLException {
                String url = lengthTweaker(param);
                if (url != null && url.startsWith("http")) {
                    String urlCutting = url.substring(url.indexOf("://") + 3);
                    for (String adUrl : HookLoader.hostsList) {
                        if (urlCutting.startsWith(adUrl) && !PreferencesHelper.whiteListElements().contains(adUrl)) {
                            lengthSetter(param);
                            LogUtils.logRecord("URL Block Success: " + lpparam.packageName + "/" + urlCutting);
                            NotificationUtils.setNotify(ContextUtils.getOwnContext());
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
