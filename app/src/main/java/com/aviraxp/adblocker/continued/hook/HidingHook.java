package com.aviraxp.adblocker.continued.hook;

import android.app.ActivityManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.os.Build;

import com.aviraxp.adblocker.continued.BuildConfig;
import com.aviraxp.adblocker.continued.helper.PreferencesHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.aviraxp.adblocker.continued.hook.HookLoader.hideList;

class HidingHook {

    private static boolean isTarget(String name) {
        return name.equals(BuildConfig.APPLICATION_ID);
    }

    void init(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blacklist/hidingapp");
        String decoded = new String(array, "UTF-8");
        String[] sUrls = decoded.split("\n");
        hideList = new HashSet<>();
        Collections.addAll(hideList, sUrls);
    }

    @SuppressWarnings("unchecked")
    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (PreferencesHelper.isAndroidApp(lpparam.packageName) || !hideList.contains(lpparam.packageName) || !PreferencesHelper.isHidingHookEnabled() || PreferencesHelper.disabledApps().contains(lpparam.packageName)) {
            return;
        }

        XC_MethodHook getInstalledApplicationsHook = new XC_MethodHook() {
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
        };

        XC_MethodHook getInstalledPackagesHook = new XC_MethodHook() {
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
        };

        XC_MethodHook getPackageInfoHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String packageName = (String) param.args[0];
                if (isTarget(packageName)) {
                    param.args[0] = lpparam.packageName;
                }
            }
        };

        XC_MethodHook getApplicationInfoHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String packageName = (String) param.args[0];
                if (isTarget(packageName)) {
                    param.args[0] = lpparam.packageName;
                }
            }
        };

        XC_MethodHook getRunningTasksHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<ActivityManager.RunningTaskInfo> serviceInfoList = (List) param.getResult();
                List<ActivityManager.RunningTaskInfo> resultList = new ArrayList<>();
                for (ActivityManager.RunningTaskInfo runningTaskInfo : serviceInfoList) {
                    String taskName = runningTaskInfo.baseActivity.flattenToString();
                    if (!isTarget(taskName)) {
                        resultList.add(runningTaskInfo);
                    }
                }
                param.setResult(resultList);
            }
        };

        XC_MethodHook getRunningAppProcessesHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = (List) param.getResult();
                List<ActivityManager.RunningAppProcessInfo> resultList = new ArrayList<>();
                for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcessInfos) {
                    String processName = runningAppProcessInfo.processName;
                    if (!isTarget(processName)) {
                        resultList.add(runningAppProcessInfo);
                    }
                }
                param.setResult(resultList);
            }
        };

        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getInstalledApplications", int.class, getInstalledApplicationsHook);
        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getInstalledPackages", int.class, getInstalledPackagesHook);
        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getPackageInfo", String.class, int.class, getPackageInfoHook);
        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getApplicationInfo", String.class, int.class, getApplicationInfoHook);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            XposedHelpers.findAndHookMethod("android.app.ActivityManager", lpparam.classLoader, "getRunningTasks", int.class, getRunningTasksHook);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            XposedHelpers.findAndHookMethod("android.app.ActivityManager", lpparam.classLoader, "getRunningAppProcesses", getRunningAppProcessesHook);
        }
    }
}
