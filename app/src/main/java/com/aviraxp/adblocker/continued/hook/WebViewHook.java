package com.aviraxp.adblocker.continued.hook;

import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.view.View;
import android.view.ViewGroup;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.LogUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class WebViewHook {

    private static boolean adExist;

    void init(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/urls");
        String decoded = new String(array, "UTF-8");
        String[] sUrls = decoded.split("\n");
        HookLoader.urlList = new HashSet<>();
        Collections.addAll(HookLoader.urlList, sUrls);
    }

    private void removeAdView(final View view) {

        ViewGroup.LayoutParams params = view.getLayoutParams();

        if (params == null) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        } else {
            params.height = 0;
        }
        view.setLayoutParams(params);

        view.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if (params == null) {
                    params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                } else {
                    params.height = 0;
                }
                view.setLayoutParams(params);
            }
        });
    }

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isWebViewHookEnabled()) {
            return;
        }

        try {
            Class<?> webView = XposedHelpers.findClass("android.webkit.WebView", lpparam.classLoader);

            XC_MethodHook loadUrlHook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                    String url = (String) param.args[0];
                    if (url != null) {
                        adExist = urlFiltering(url, null, null, param);
                        if (adExist) {
                            LogUtils.logRecord("WebView Block Success: " + lpparam.packageName + "/" + url);
                        }
                    }
                }
            };

            XC_MethodHook loadDataHook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                    String data = (String) param.args[0];
                    String encodingType = (String) param.args[2];
                    if (data != null) {
                        adExist = urlFiltering(null, data, encodingType, param);
                        if (adExist) {
                            LogUtils.logRecord("WebView Block Success: " + lpparam.packageName + "/" + data);
                        }
                    }
                }
            };

            XC_MethodHook loadDataWithBaseURL = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                    String url = (String) param.args[0];
                    String data = (String) param.args[1];
                    String encodingType = (String) param.args[3];
                    if (url != null || data != null) {
                        adExist = urlFiltering(url, data, encodingType, param);
                        if (adExist) {
                            LogUtils.logRecord("WebView Block Success: " + lpparam.packageName + "/" + url + " & " + data);
                        }
                    }
                }
            };

            XposedBridge.hookAllMethods(webView, "loadUrl", loadUrlHook);
            XposedBridge.hookAllMethods(webView, "loadData", loadDataHook);
            XposedBridge.hookAllMethods(webView, "loadDataWithBaseURL", loadDataWithBaseURL);
        } catch (XposedHelpers.ClassNotFoundError ignored) {
        }
    }

    private boolean urlFiltering(String url, String data, String encodingType, XC_MethodHook.MethodHookParam param) {
        String urlDecode = decode(url, encodingType);
        String dataDecode = decode(data, encodingType);
        return hostsBlock(urlDecode, HookLoader.hostsList, param) || hostsBlock(dataDecode, HookLoader.hostsList, param) || urlBlock(urlDecode, HookLoader.urlList, param) || urlBlock(dataDecode, HookLoader.urlList, param);
    }

    private boolean hostsBlock(String string, HashSet<String> hashSet, XC_MethodHook.MethodHookParam param) {
        if ((string != null && !PreferencesHelper.whiteListElements().contains(string) && string.startsWith("http"))) {
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

    private String decode(String string, String encodingType) {
        if (string != null) {
            try {
                if (encodingType != null) {
                    return URLDecoder.decode(string, encodingType);
                } else {
                    return URLDecoder.decode(string, "UTF-8");
                }
            } catch (UnsupportedEncodingException | IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    private boolean urlBlock(String string, HashSet<String> hashSet, XC_MethodHook.MethodHookParam param) {
        if ((string != null && !PreferencesHelper.whiteListElements().contains(string) && string.startsWith("http"))) {
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