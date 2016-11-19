package pl.cinek.adblocker.mod;

import android.content.Context;

import org.json.JSONObject;

import java.util.HashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedSpecificHook implements IXposedHookLoadPackage {

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam paramLoadPackageParam)
            throws Throwable {

        //ifont
        if (paramLoadPackageParam.packageName.equals("com.kapp.ifont")) {
            Object CommonUtil = XposedHelpers.findClass("com.kapp.ifont.core.util.CommonUtil", paramLoadPackageParam.classLoader);
            XposedHelpers.findAndHookMethod((Class) CommonUtil, "isPremium", Context.class, XC_MethodReplacement.returnConstant(true));
            XposedHelpers.findAndHookMethod((Class) CommonUtil, "isShowRecomTab", Context.class, XC_MethodReplacement.returnConstant(false));
            XposedBridge.hookAllMethods((Class) CommonUtil, "isShowAdBanner", XC_MethodReplacement.returnConstant(false));
            XposedBridge.hookAllMethods((Class) CommonUtil, "isShowAdInterstitial", XC_MethodReplacement.returnConstant(false));
            XposedBridge.hookAllMethods((Class) CommonUtil, "showAdBanner", XC_MethodReplacement.returnConstant(null));
            XposedBridge.hookAllMethods((Class) CommonUtil, "showDownloadApp", XC_MethodReplacement.returnConstant(null));
            XposedBridge.hookAllMethods((Class) CommonUtil, "showInterstitialAd", XC_MethodReplacement.returnConstant(null));
            XposedBridge.hookAllMethods((Class) CommonUtil, "showInterstitialAdForce", XC_MethodReplacement.returnConstant(null));
            XposedBridge.hookAllMethods((Class) CommonUtil, "supportInterstitialAd", XC_MethodReplacement.returnConstant(null));
            XposedHelpers.findAndHookMethod("com.kapp.ifont.core.util.t", paramLoadPackageParam.classLoader, "e", Context.class, String.class, new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam paramAnonymousMethodHookParam)
                        throws Throwable {
                    if ("com.kapp.ifont.donate".equals(paramAnonymousMethodHookParam.args[1])) {
                        paramAnonymousMethodHookParam.setResult(true);
                    }
                }
            });
            XposedBridge.log("Application Specific Hook Success: " + paramLoadPackageParam.packageName);
        }

        //Youku
        if (paramLoadPackageParam.packageName.equals("com.youku.phone")) {
            Class<?> MediaPlayerConfiguration = XposedHelpers.findClass("com.youku.player.config.MediaPlayerConfiguration", paramLoadPackageParam.classLoader);
            Class<?> YoukuUtil = XposedHelpers.findClass("com.youku.util.YoukuUtil", paramLoadPackageParam.classLoader);
            Object AnalyticWrapper = XposedHelpers.findClass("com.youku.player.util.AnalyticWrapperWrapper", paramLoadPackageParam.classLoader);
            Object AnalyticBase = XposedHelpers.findClass("com.youku.AnalyticWrapper.AnalyticWrapperBase", paramLoadPackageParam.classLoader);
            Object GameCenterModel = XposedHelpers.findClass("com.youku.gamecenter.GameCenterModel", paramLoadPackageParam.classLoader);
            XposedHelpers.findAndHookMethod(MediaPlayerConfiguration, "showAdWebView", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(MediaPlayerConfiguration, "showOfflineAd", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(MediaPlayerConfiguration, "showPauseAd", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(MediaPlayerConfiguration, "showPreAd", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(MediaPlayerConfiguration, "trackAd", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(MediaPlayerConfiguration, "showSkipAdButton", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(YoukuUtil, "isGamecenterDisplay", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod((Class) AnalyticWrapper, "isAnalyticWrapperOpen", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod((Class) AnalyticWrapper, "trackExtendCustomEvent", Context.class, String.class, String.class, String.class, String.class, HashMap.class, XC_MethodReplacement.DO_NOTHING);
            XposedHelpers.findAndHookMethod((Class) AnalyticBase, "sendAppReport", Context.class, XC_MethodReplacement.DO_NOTHING);
            XposedHelpers.findAndHookMethod((Class) AnalyticBase, "sendExceedReport", Context.class, XC_MethodReplacement.DO_NOTHING);
            XposedHelpers.findAndHookMethod((Class) AnalyticBase, "saveToDisk", JSONObject.class, XC_MethodReplacement.DO_NOTHING);
            XposedHelpers.findAndHookMethod((Class) AnalyticBase, "saveData", Context.class, Boolean.TYPE, XC_MethodReplacement.DO_NOTHING);
            XposedHelpers.findAndHookMethod((Class) AnalyticBase, "write", Context.class, Long.TYPE, XC_MethodReplacement.DO_NOTHING);
            XposedHelpers.findAndHookMethod((Class) GameCenterModel, "loadLocalPackages", Context.class, XC_MethodReplacement.DO_NOTHING);
            XposedHelpers.findAndHookMethod((Class) GameCenterModel, "registerReceiver", Context.class, XC_MethodReplacement.DO_NOTHING);
            XposedHelpers.findAndHookMethod((Class) GameCenterModel, "unRegisterReceiver", Context.class, XC_MethodReplacement.DO_NOTHING);
            XposedBridge.log("Application Specific Hook Success: " + paramLoadPackageParam.packageName);
        }
    }
}
