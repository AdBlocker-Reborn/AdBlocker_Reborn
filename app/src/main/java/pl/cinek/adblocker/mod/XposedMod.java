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

public class XposedMod implements IXposedHookLoadPackage {
    private static final List<String> blocked_activities_list = Arrays.asList(ADBlockList.blocked_activities);
    private static final List<String> blocked_views_list = Arrays.asList(ADBlockList.blocked_views);
    private static final List<String> blocked_views_on_packages_list = Arrays.asList(ADBlockList.blocked_views_on_packages);

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam paramLoadPackageParam)
            throws Throwable {
        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam paramAnonymousMethodHookParam)
                    throws Throwable {
                Activity activity = (Activity) paramAnonymousMethodHookParam.thisObject;
                String str = activity.getClass().getName();
                if ((str != null) && (!str.startsWith("android")) && (XposedMod.blocked_activities_list.contains(str))) {
                    activity.overridePendingTransition(0, 0);
                    activity.finish();
                    activity.overridePendingTransition(0, 0);
                }
            }
        });
        Object localObject = new XC_MethodHook() {
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam paramAnonymousMethodHookParam)
                    throws Throwable {
                ComponentName localComponentName = ((Intent) paramAnonymousMethodHookParam.args[0]).getComponent();
                String str = null;
                if (localComponentName != null) {
                    str = localComponentName.getClassName();
                }
                if ((str != null) && (!str.startsWith("android")) && (XposedMod.blocked_activities_list.contains(str))) {
                    paramAnonymousMethodHookParam.setResult(null);
                }
            }
        };
        XposedHelpers.findAndHookMethod(Activity.class, "startActivity", Intent.class, localObject);
        XposedHelpers.findAndHookMethod(ContextWrapper.class, "startActivity", Intent.class, localObject);
        XposedHelpers.findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, Integer.TYPE, localObject);
        XposedHelpers.findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, Integer.TYPE, Bundle.class, localObject);
        localObject = new XC_MethodHook() {
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam paramAnonymousMethodHookParam)
                    throws Throwable {
                hideIfAdView(paramAnonymousMethodHookParam.thisObject, paramLoadPackageParam.packageName);
            }
        };
        XposedBridge.hookAllConstructors(View.class, (XC_MethodHook) localObject);
        XposedBridge.hookAllConstructors(ViewGroup.class, (XC_MethodHook) localObject);
        XposedHelpers.findAndHookMethod(View.class, "setVisibility", Integer.TYPE, new XC_MethodHook() {
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam paramAnonymousMethodHookParam)
                    throws Throwable {
                if ((Integer) paramAnonymousMethodHookParam.args[0] != 8) {
                    hideIfAdView(paramAnonymousMethodHookParam.thisObject, paramLoadPackageParam.packageName);
                }
            }
        });
    }

    private void hideIfAdView(Object paramObject, String paramString) {
        String str = paramObject.getClass().getName();
        if ((str != null) && (!str.startsWith("android")) && ((blocked_views_list.contains(str)) || (blocked_views_on_packages_list.contains(paramString + "/" + str)))) {
            ((View) paramObject).setVisibility(View.GONE);
        }
    }
}