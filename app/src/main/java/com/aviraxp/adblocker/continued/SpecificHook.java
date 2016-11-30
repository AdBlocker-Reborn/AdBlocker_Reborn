package com.aviraxp.adblocker.continued;

import android.app.Notification;
import android.content.Context;

import java.util.HashMap;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SpecificHook implements IXposedHookLoadPackage {

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        //iFont
        if (lpparam.packageName.equals("com.kapp.ifont")) {
            Object CommonUtil = XposedHelpers.findClass("com.kapp.ifont.core.util.CommonUtil", lpparam.classLoader);
            XposedHelpers.findAndHookMethod((Class) CommonUtil, "isPremium", Context.class, XC_MethodReplacement.returnConstant(true));
            XposedHelpers.findAndHookMethod((Class) CommonUtil, "isShowRecomTab", Context.class, XC_MethodReplacement.returnConstant(false));
            XposedBridge.hookAllMethods((Class) CommonUtil, "isShowAdBanner", XC_MethodReplacement.returnConstant(false));
            XposedBridge.hookAllMethods((Class) CommonUtil, "isShowAdInterstitial", XC_MethodReplacement.returnConstant(false));
            XposedBridge.hookAllMethods((Class) CommonUtil, "showAdBanner", XC_MethodReplacement.returnConstant(null));
            XposedBridge.hookAllMethods((Class) CommonUtil, "showDownloadApp", XC_MethodReplacement.returnConstant(null));
            XposedBridge.hookAllMethods((Class) CommonUtil, "showInterstitialAd", XC_MethodReplacement.returnConstant(null));
            XposedBridge.hookAllMethods((Class) CommonUtil, "showInterstitialAdForce", XC_MethodReplacement.returnConstant(null));
            XposedBridge.hookAllMethods((Class) CommonUtil, "supportInterstitialAd", XC_MethodReplacement.returnConstant(null));
            XposedHelpers.findAndHookMethod("com.kapp.ifont.core.util.t", lpparam.classLoader, "e", Context.class, String.class, new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    if ("com.kapp.ifont.donate".equals(param.args[1])) {
                        param.setResult(true);
                    }
                }
            });
            if (BuildConfig.DEBUG) {
                XposedBridge.log("Application Specific Hook Success: " + lpparam.packageName);
            }
        }

        //Youku
        if (lpparam.packageName.equals("com.youku.phone")) {
            Class<?> MediaPlayerConfiguration = XposedHelpers.findClass("com.youku.player.config.MediaPlayerConfiguration", lpparam.classLoader);
            Class<?> YoukuUtil = XposedHelpers.findClass("com.youku.util.YoukuUtil", lpparam.classLoader);
            Class<?> AnalyticWrapper = XposedHelpers.findClass("com.youku.player.util.AnalyticsWrapper", lpparam.classLoader);
            Class<?> GameCenterModel = XposedHelpers.findClass("com.youku.gamecenter.GameCenterModel", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(MediaPlayerConfiguration, "showAdWebView", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(MediaPlayerConfiguration, "showOfflineAd", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(MediaPlayerConfiguration, "showPauseAd", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(MediaPlayerConfiguration, "showPreAd", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(MediaPlayerConfiguration, "trackAd", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(MediaPlayerConfiguration, "showSkipAdButton", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(YoukuUtil, "isGamecenterDisplay", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(AnalyticWrapper, "isAnalyticWrapperOpen", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(AnalyticWrapper, "trackExtendCustomEvent", Context.class, String.class, String.class, String.class, String.class, HashMap.class, XC_MethodReplacement.DO_NOTHING);
            XposedHelpers.findAndHookMethod(GameCenterModel, "loadLocalPackages", Context.class, XC_MethodReplacement.DO_NOTHING);
            XposedHelpers.findAndHookMethod(GameCenterModel, "registerReceiver", Context.class, XC_MethodReplacement.DO_NOTHING);
            XposedHelpers.findAndHookMethod(GameCenterModel, "unRegisterReceiver", Context.class, XC_MethodReplacement.DO_NOTHING);
            if (BuildConfig.DEBUG) {
                XposedBridge.log("Application Specific Hook Success: " + lpparam.packageName);
            }
        }

        //Tumblr
        if (lpparam.packageName.equals("com.tumblr")) {
            XposedHelpers.findAndHookMethod("com.tumblr.ui.widget.timelineadapter.SimpleTimelineAdapter", lpparam.classLoader, "applyItems", List.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    List<?> timeline = (List<?>) param.args[0];
                    int adCount = 0;
                    int postCount = timeline.size();
                    for (int i = postCount - 1; i >= 0; i--) {
                        Object timelineObject = timeline.get(i);
                        Object objectData = XposedHelpers.callMethod(timelineObject, "getObjectData");
                        Enum<?> typeEnum = (Enum<?>) XposedHelpers.callMethod(objectData, "getTimelineObjectType");
                        String typeStr = typeEnum.name();
                        boolean isSponsored = (Boolean) XposedHelpers.callMethod(timelineObject, "isSponsored");
                        if ((((typeStr.equals("BANNER") || typeStr.equals("CAROUSEL") || typeStr.equals("RICH_BANNER") || typeStr.equals("GEMINI_AD")) && !typeStr.equals("BLOG_CARD") && !typeStr.equals("POST"))) || isSponsored) {
                            timeline.remove(i);
                            adCount++;
                        }
                    }
                }

            });
            XposedHelpers.findAndHookMethod("com.tumblr.model.PostAttribution", lpparam.classLoader, "shouldShowNewAppAttribution", XC_MethodReplacement.returnConstant(false));
            if (BuildConfig.DEBUG) {
                XposedBridge.log("Application Specific Hook Success: " + lpparam.packageName);
            }
        }

        //SoundCloud
        if (lpparam.packageName.equals("com.soundcloud.android")) {
            XposedHelpers.findAndHookMethod("com.soundcloud.android.ads.AdsOperations", lpparam.classLoader, "insertAudioAd", "com.soundcloud.android.playback.TrackQueueItem", "com.soundcloud.android.ads.ApiAudioAd", XC_MethodReplacement.returnConstant(null));
            XposedHelpers.findAndHookMethod("android.support.v4.app.NotificationManagerCompat", lpparam.classLoader, "notify", String.class, int.class, Notification.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    if ("appboy_notification".equals(param.args[0])) {
                        param.setResult(null);
                    }
                }
            });
            if (BuildConfig.DEBUG) {
                XposedBridge.log("Application Specific Hook Success: " + lpparam.packageName);
            }
        }

        //Kernel Adiutor
        if (lpparam.packageName.equals("com.grarak.kerneladiutor")) {
            Class KABuildConfig = XposedHelpers.findClass("com.grarak.kerneladiutor.BuildConfig", lpparam.classLoader);
            Class Utils = XposedHelpers.findClass("com.grarak.kerneladiutor.utils.Utils", lpparam.classLoader);
            XposedHelpers.setStaticBooleanField(KABuildConfig, "DEBUG", true);
            XposedHelpers.setStaticBooleanField(Utils, "DONATED", true);
            XposedHelpers.setStaticObjectField(KABuildConfig, "BUILD_TYPE", "DEBUG");
            if (BuildConfig.DEBUG) {
                XposedBridge.log("Application Specific Hook Success: " + lpparam.packageName);
            }
        }
    }
}
