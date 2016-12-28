package com.aviraxp.adblocker.continued;

import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.view.View;
import android.view.ViewGroup;

import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class WebViewHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    public Resources res;
    private boolean adExist;
    private Set<String> hostsList;
    private Set<String> whiteList;
    private Set<String> regexList;

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

        if (whiteList.contains(lpparam.packageName)) {
            return;
        }

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

        String urlDecode = null;
        String dataDecode = null;

        if (!url.equals("")) {
            urlDecode = URLDecoder.decode(url, "UTF-8");
        }
        if (!data.equals("")) {
            dataDecode = URLDecoder.decode(data, "UTF-8");
        }

        for (String adUrl : hostsList) {
            if ((urlDecode != null && urlDecode.contains(adUrl)) || (dataDecode != null && dataDecode.contains(adUrl))) {
                try {
                    param.setResult(new Object());
                    removeAdView((View) param.thisObject);
                    return true;
                } catch (Exception e) {
                    XposedBridge.log(e);
                }
            }
        }

        for (String regexAdUrl : regexList) {
            if (urlDecode != null && urlDecode.contains("//")) {
                try {
                    String urlRegex = urlDecode.substring(urlDecode.indexOf("//") + 1);
                    Pattern regexPattern = Pattern.compile(regexAdUrl);
                    Matcher matcher = regexPattern.matcher(urlRegex);
                    if (matcher.matches()) {
                        param.setResult(new Object());
                        removeAdView((View) param.thisObject);
                        return true;
                    }
                } catch (Exception e) {
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
        byte[] array2 = XposedHelpers.assetAsByteArray(res, "whitelist/app");
        byte[] array3 = XposedHelpers.assetAsByteArray(res, "blocklist/regexurls");
        String decoded = new String(array);
        String decoded2 = new String(array2);
        String decoded3 = new String(array3);
        String[] sUrls = decoded.split("\n");
        String[] sUrls2 = decoded2.split("\n");
        String[] sUrls3 = decoded3.split("\n");
        hostsList = new HashSet<>();
        whiteList = new HashSet<>();
        regexList = new HashSet<>();
        Collections.addAll(hostsList, sUrls);
        Collections.addAll(whiteList, sUrls2);
        Collections.addAll(regexList, sUrls3);
    }
}