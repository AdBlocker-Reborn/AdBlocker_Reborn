package com.aviraxp.adblocker.continued.hook;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.content.res.XModuleResources;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HidingHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private Set<String> whiteList;

    @SuppressWarnings("unchecked")
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isHidingHookEnabled()) {
            return;
        }

        if (whiteList.contains(lpparam.packageName)) {
            return;
        }

        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getInstalledApplications", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<ApplicationInfo> applicationList = (List) param.getResult();
                List<ApplicationInfo> resultapplicationList = new ArrayList<>();
                for (ApplicationInfo applicationInfo : applicationList) {
                    String packageName = applicationInfo.packageName;
                    if (!isTarget(packageName)) {
                        resultapplicationList.add(applicationInfo);
                    }
                }
                param.setResult(resultapplicationList);
            }
        });

        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getInstalledPackages", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<PackageInfo> packageInfoList = (List) param.getResult();
                List<PackageInfo> resultpackageInfoList = new ArrayList<>();

                for (PackageInfo packageInfo : packageInfoList) {
                    String packageName = packageInfo.packageName;
                    if (!isTarget(packageName)) {
                        resultpackageInfoList.add(packageInfo);
                    }
                }
                param.setResult(resultpackageInfoList);
            }
        });

        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getPackageInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String packageName = (String) param.args[0];
                if (isTarget(packageName)) {
                    param.args[0] = lpparam.packageName;
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getApplicationInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String packageName = (String) param.args[0];
                if (isTarget(packageName)) {
                    param.args[0] = lpparam.packageName;
                }
            }
        });
    }

    private boolean isTarget(String name) {
        return name.equals("com.aviraxp.adblocker.continued");
    }

    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "whitelist/hidingapp");
        String decoded = new String(array, "UTF-8");
        String[] sUrls = decoded.split("\n");
        whiteList = new HashSet<>();
        Collections.addAll(whiteList, sUrls);
    }
}
