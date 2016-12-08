package com.aviraxp.adblocker.continued;

import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class WebViewHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    public static Resources res;
    private static boolean adExist = false;
    private Set<String> patterns;

    private static void removeAdView(final View view, final boolean first, final float heightLimit) {

        float adHeight = convertPixelsToDp(view.getHeight());

        if (first || (adHeight > 0 && adHeight <= heightLimit)) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params == null) {
                params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            } else {
                params.height = 0;
            }
            view.setLayoutParams(params);
        }

        view.post(new Runnable() {
            @Override
            public void run() {
                float adHeight = convertPixelsToDp(view.getHeight());
                if (first || (adHeight > 0 && adHeight <= heightLimit)) {
                    ViewGroup.LayoutParams params = view.getLayoutParams();
                    if (params == null) {
                        params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                    } else {
                        params.height = 0;
                    }
                    view.setLayoutParams(params);
                }
            }
        });
        if (view.getParent() != null && view.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view.getParent();
            removeAdView(parent, false, heightLimit);
        }
    }

    private static float convertPixelsToDp(float px) {
        DisplayMetrics metrics = res.getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            Class<?> adView = XposedHelpers.findClass("android.webkit.WebView", lpparam.classLoader);
            XposedBridge.hookAllMethods(adView, "loadUrl", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String url = (String) param.args[0];
                    adExist = urlFiltering(url, "", param);
                    if (adExist) {
                        param.setResult(new Object());
                    }
                }
            });

            XposedBridge.hookAllMethods(adView, "loadData", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                    String data = (String) param.args[0];
                    adExist = urlFiltering("", data, param);
                    if (adExist) {
                        param.setResult(new Object());
                    }
                }
            });

            XposedBridge.hookAllMethods(adView, "loadDataWithBaseURL", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String url = (String) param.args[0];
                    String data = (String) param.args[1];
                    adExist = urlFiltering(url, data, param);
                    if (adExist) {
                        param.setResult(new Object());
                    }
                }
            });
        } catch (XposedHelpers.ClassNotFoundError ignored) {
        }
    }

    private boolean urlFiltering(String url, String data, MethodHookParam param) {
        if (url == null) {
            url = "";
        }

        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        for (String adUrl : patterns) {
            if (url.contains(adUrl)) {
                param.setResult(new Object());
                removeAdView((View) param.thisObject, true, 51);
                return true;
            }
        }

        try {
            data = URLDecoder.decode(data, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String adUrl : patterns) {
            if (data.contains(adUrl)) {
                param.setResult(new Object());
                removeAdView((View) param.thisObject, true, 51);
                return true;
            }
        }
        return false;
    }

    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/hosts");
        String decoded = new String(array);
        String[] sUrls = decoded.split("\n");
        patterns = new HashSet<>();
        Collections.addAll(patterns, sUrls);
    }
}
