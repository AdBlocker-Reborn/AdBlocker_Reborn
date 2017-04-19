package com.aviraxp.adblocker.continued.hook;

import android.view.View;
import android.webkit.WebView;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.ContextUtils;
import com.aviraxp.adblocker.continued.util.LogUtils;
import com.aviraxp.adblocker.continued.util.NotificationUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class WebViewHook {

    private static boolean adExist = false;

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isWebViewHookEnabled()) {
            return;
        }

        XC_MethodHook urlHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String url = (String) param.args[0];
                if (url != null) {
                    adExist = urlFiltering(url, null, param);
                    if (adExist) {
                        LogUtils.logRecord("WebView Block Success: " + lpparam.packageName + "/" + url);
                        NotificationUtils.setNotify(ContextUtils.getOwnContext());
                    }
                }
            }
        };

        XC_MethodHook loadDataHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String data = (String) param.args[0];
                String encodingType = (String) param.args[2];
                if (data != null) {
                    adExist = urlFiltering(null, data, param);
                    if (adExist) {
                        LogUtils.logRecord("WebView Block Success: " + lpparam.packageName + "/" + data);
                        NotificationUtils.setNotify(ContextUtils.getOwnContext());
                    }
                }
            }
        };

        XC_MethodHook loadDataWithBaseURL = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String url = (String) param.args[0];
                String data = (String) param.args[1];
                String encodingType = (String) param.args[3];
                if (url != null || data != null) {
                    adExist = urlFiltering(url, data, param);
                    if (adExist) {
                        LogUtils.logRecord("WebView Block Success: " + lpparam.packageName + "/" + url + " & " + data);
                        NotificationUtils.setNotify(ContextUtils.getOwnContext());
                    }
                }
            }
        };

        XposedBridge.hookAllMethods(WebView.class, "postUrl", urlHook);
        XposedBridge.hookAllMethods(WebView.class, "loadUrl", urlHook);
        XposedBridge.hookAllMethods(WebView.class, "loadData", loadDataHook);
        XposedBridge.hookAllMethods(WebView.class, "loadDataWithBaseURL", loadDataWithBaseURL);
    }

    private boolean urlFiltering(String url, String data, XC_MethodHook.MethodHookParam param) {
        return hostsBlock(url, param) || hostsBlock(data, param) || urlBlock(url, param) || urlBlock(data, param);
    }

    private boolean hostsBlock(String string, XC_MethodHook.MethodHookParam param) {
        if (string != null && string.startsWith("http")) {
            try {
                for (String adUrl : HookLoader.hostsList) {
                    if (string.substring(string.indexOf("://") + 3).startsWith(adUrl) && !PreferencesHelper.whiteListElements().contains(string)) {
                        param.setResult(null);
                        ((View) param.thisObject).clearAnimation();
                        ((View) param.thisObject).setVisibility(View.GONE);
                        return true;
                    }
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return false;
    }

    private boolean urlBlock(String string, XC_MethodHook.MethodHookParam param) {
        if (string != null && string.startsWith("http")) {
            try {
                for (String adUrl : HookLoader.urlList) {
                    if (string.contains(adUrl) && !PreferencesHelper.whiteListElements().contains(string)) {
                        param.setResult(null);
                        ((View) param.thisObject).clearAnimation();
                        ((View) param.thisObject).setVisibility(View.GONE);
                        return true;
                    }
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return false;
    }
}