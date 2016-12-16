package com.aviraxp.adblocker.continued;

import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.view.View;
import android.view.ViewGroup;

import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class WebViewHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    public Resources res;
    private boolean adExist;
    private Set<String> patterns;

    private void removeAdView(final View view) throws Throwable {
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

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        try {
            Class<?> webView = XposedHelpers.findClass("android.webkit.WebView", lpparam.classLoader);

            XposedBridge.hookAllMethods(webView, "loadUrl", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    String url = (String) param.args[0];
                    if (url != null) {
                        adExist = urlFiltering(url, "", param);
                        if (adExist) {
                            param.setResult(new Object());
                            if (BuildConfig.DEBUG) {
                                XposedBridge.log("WebView Block Success: " + lpparam.packageName + "/" + url);
                            }
                        }
                    }
                }
            });

            XposedBridge.hookAllMethods(webView, "loadData", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    String data = (String) param.args[0];
                    if (data != null) {
                        adExist = urlFiltering("", data, param);
                        if (adExist) {
                            param.setResult(new Object());
                            if (BuildConfig.DEBUG) {
                                XposedBridge.log("WebView Block Success: " + lpparam.packageName + "/" + data);
                            }
                        }
                    }
                }
            });

            XposedBridge.hookAllMethods(webView, "loadDataWithBaseURL", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    String url = (String) param.args[0];
                    String data = (String) param.args[1];
                    if (url != null && data != null) {
                        adExist = urlFiltering(url, data, param);
                        if (adExist) {
                            param.setResult(new Object());
                            if (BuildConfig.DEBUG) {
                                XposedBridge.log("WebView Block Success: " + lpparam.packageName + "/" + url + " & " + data);
                            }
                        }
                    }
                }
            });
        } catch (XposedHelpers.ClassNotFoundError ignored) {
        }
    }

    private boolean urlFiltering(String url, String data, XC_MethodHook.MethodHookParam param) throws Throwable {

        if (!url.equals("")) {
            try {
                String urldecode = URLDecoder.decode(url, "UTF-8");
                for (String adUrl : patterns) {
                    if (urldecode.contains(adUrl)) {
                        param.setResult(new Object());
                        removeAdView((View) param.thisObject);
                        return true;
                    }
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    XposedBridge.log(e);
                }
            }
        }

        if (!data.equals("")) {
            try {
                String datadecode = URLDecoder.decode(data, "UTF-8");
                for (String adUrl : patterns) {
                    if (datadecode.contains(adUrl)) {
                        param.setResult(new Object());
                        removeAdView((View) param.thisObject);
                        return true;
                    }
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    XposedBridge.log(e);
                }
            }
        }

        return false;
    }

    public void initZygote(StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/hosts");
        String decoded = new String(array);
        String[] sUrls = decoded.split("\n");
        patterns = new HashSet<>();
        Collections.addAll(patterns, sUrls);
    }
}