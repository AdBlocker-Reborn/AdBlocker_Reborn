package com.aviraxp.adblocker.continued.hook;

import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.ContextUtils;
import com.aviraxp.adblocker.continued.util.DecodeUtils;
import com.aviraxp.adblocker.continued.util.LogUtils;
import com.aviraxp.adblocker.continued.util.NotificationUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class WebViewHook {

    private static boolean adExist;

    void init(IXposedHookZygoteInit.StartupParam startupParam) throws IOException {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/urls");
        String decoded = new String(array, "UTF-8");
        String[] sUrls = decoded.split("\n");
        HookLoader.urlList = new HashSet<>();
        Collections.addAll(HookLoader.urlList, sUrls);
    }

    private void removeAdView(final View view) {

        view.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if (params == null) {
                    params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                } else {
                    params.height = 0;
                    params.width = 0;
                }
                view.setLayoutParams(params);
            }
        });
    }

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isWebViewHookEnabled()) {
            return;
        }

        XC_MethodHook urlHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String url = (String) param.args[0];
                if (url != null) {
                    adExist = urlFiltering(url, null, null, param);
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
                    adExist = urlFiltering(null, data, encodingType, param);
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
                    adExist = urlFiltering(url, data, encodingType, param);
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

    private boolean urlFiltering(String url, String data, String encodingType, XC_MethodHook.MethodHookParam param) {
        String urlDecode = DecodeUtils.decode(url, encodingType);
        String dataDecode = DecodeUtils.decode(data, encodingType);
        return hostsBlock(urlDecode, HookLoader.hostsList, param) || hostsBlock(dataDecode, HookLoader.hostsList, param) || urlBlock(urlDecode, HookLoader.urlList, param) || urlBlock(dataDecode, HookLoader.urlList, param);
    }

    private boolean hostsBlock(String string, HashSet<String> hashSet, XC_MethodHook.MethodHookParam param) {
        if (string != null && !PreferencesHelper.whiteListElements().contains(string) && string.startsWith("http")) {
            try {
                for (String adUrl : hashSet) {
                    if (string.substring(string.indexOf("://") + 3).startsWith(adUrl)) {
                        param.setResult(null);
                        removeAdView((View) param.thisObject);
                        return true;
                    }
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return false;
    }

    private boolean urlBlock(String string, HashSet<String> hashSet, XC_MethodHook.MethodHookParam param) {
        if (string != null && !PreferencesHelper.whiteListElements().contains(string) && string.startsWith("http")) {
            try {
                for (String adUrl : hashSet) {
                    if (string.contains(adUrl)) {
                        param.setResult(null);
                        removeAdView((View) param.thisObject);
                        return true;
                    }
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return false;
    }
}