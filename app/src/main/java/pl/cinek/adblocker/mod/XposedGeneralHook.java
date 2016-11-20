package pl.cinek.adblocker.mod;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedGeneralHook implements IXposedHookLoadPackage {
    private static final List<String> blocked_activities_list = Arrays.asList(BlockList.blocked_activities);
    private static final List<String> blocked_views_list = Arrays.asList(BlockList.blocked_views);
    private static final List<String> blocked_views_on_packages_list = Arrays.asList(BlockList.blocked_views_on_packages);
    private static final List<String> blocked_specific_apps_list = Arrays.asList(BlockList.blocked_specific_apps);

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam paramLoadPackageParam)
            throws Throwable {
        if (!blocked_specific_apps_list.contains(paramLoadPackageParam.packageName)) {
            XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam paramAnonymousMethodHookParam)
                        throws Throwable {
                    Activity activity = (Activity) paramAnonymousMethodHookParam.thisObject;
                    String activityClassName = activity.getClass().getName();
                    if ((activityClassName != null) && (!activityClassName.startsWith("android")) && (blocked_activities_list.contains(activityClassName))) {
                        activity.overridePendingTransition(0, 0);
                        activity.finish();
                        activity.overridePendingTransition(0, 0);
                        XposedBridge.log("Activity Block Success: " + paramLoadPackageParam.packageName + "/" + activityClassName);
                    }
                }
            });
            Object activityObject = new XC_MethodHook() {
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam paramAnonymousMethodHookParam)
                        throws Throwable {
                    ComponentName Component = ((Intent) paramAnonymousMethodHookParam.args[0]).getComponent();
                    String activityClassName = null;
                    if (Component != null) {
                        activityClassName = Component.getClassName();
                    }
                    if ((activityClassName != null) && (!activityClassName.startsWith("android")) && (blocked_activities_list.contains(activityClassName))) {
                        paramAnonymousMethodHookParam.setResult(null);
                        XposedBridge.log("Activity Block Success: " + paramLoadPackageParam.packageName + "/" + activityClassName);
                    }
                }
            };
            XposedHelpers.findAndHookMethod(Activity.class, "startActivity", Intent.class, activityObject);
            XposedHelpers.findAndHookMethod(ContextWrapper.class, "startActivity", Intent.class, activityObject);
            XposedHelpers.findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, Integer.TYPE, activityObject);
            XposedHelpers.findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, Integer.TYPE, Bundle.class, activityObject);
            Object viewObject = new XC_MethodHook() {
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam paramAnonymousMethodHookParam)
                        throws Throwable {
                    hideIfAdView(paramAnonymousMethodHookParam.thisObject, paramLoadPackageParam.packageName);
                }
            };
            XposedBridge.hookAllConstructors(View.class, (XC_MethodHook) viewObject);
            XposedBridge.hookAllConstructors(ViewGroup.class, (XC_MethodHook) viewObject);
            XposedHelpers.findAndHookMethod(View.class, "setVisibility", Integer.TYPE, new XC_MethodHook() {
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam paramAnonymousMethodHookParam)
                        throws Throwable {
                    if ((Integer) paramAnonymousMethodHookParam.args[0] != 8) {
                        hideIfAdView(paramAnonymousMethodHookParam.thisObject, paramLoadPackageParam.packageName);
                    }
                }
            });
        }
    }

    private void hideIfAdView(Object paramObject, String paramString) {
        String str = paramObject.getClass().getName();
        if ((str != null) && (!str.startsWith("android")) && ((blocked_views_list.contains(str)) || (blocked_views_on_packages_list.contains(paramString + "/" + str)))) {
            ((View) paramObject).setVisibility(View.GONE);
            XposedBridge.log("View Block Success: " + paramString + "/" + str);
        }
    }
}