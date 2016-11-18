package pl.cinek.adblocker.mod;

import android.content.Context;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedSpecificHook implements IXposedHookLoadPackage {

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam paramLoadPackageParam)
            throws Throwable {
        if (paramLoadPackageParam.packageName.equals("com.kapp.ifont")) {
            Object localObject = XposedHelpers.findClass("com.kapp.ifont.core.util.CommonUtil", paramLoadPackageParam.classLoader);
            XposedHelpers.findAndHookMethod((Class) localObject, "isPremium", Context.class, XC_MethodReplacement.returnConstant(true));
            XposedHelpers.findAndHookMethod((Class) localObject, "isShowRecomTab", Context.class, XC_MethodReplacement.returnConstant(false));
            XposedBridge.hookAllMethods((Class) localObject, "isShowAdBanner", XC_MethodReplacement.returnConstant(false));
            XposedBridge.hookAllMethods((Class) localObject, "isShowAdInterstitial", XC_MethodReplacement.returnConstant(false));
            XposedBridge.hookAllMethods((Class) localObject, "showAdBanner", XC_MethodReplacement.returnConstant(null));
            XposedBridge.hookAllMethods((Class) localObject, "showDownloadApp", XC_MethodReplacement.returnConstant(null));
            XposedBridge.hookAllMethods((Class) localObject, "showInterstitialAd", XC_MethodReplacement.returnConstant(null));
            XposedBridge.hookAllMethods((Class) localObject, "showInterstitialAdForce", XC_MethodReplacement.returnConstant(null));
            XposedBridge.hookAllMethods((Class) localObject, "supportInterstitialAd", XC_MethodReplacement.returnConstant(null));
            XposedHelpers.findAndHookMethod("com.kapp.ifont.core.util.t", paramLoadPackageParam.classLoader, "e", Context.class, String.class, new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam paramAnonymousMethodHookParam)
                        throws Throwable {
                    if ("com.kapp.ifont.donate".equals(paramAnonymousMethodHookParam.args[1])) {
                        paramAnonymousMethodHookParam.setResult(true);
                    }
                }
            });
        }

    }
}
