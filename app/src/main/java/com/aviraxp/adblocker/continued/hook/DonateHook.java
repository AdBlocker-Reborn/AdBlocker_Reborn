package com.aviraxp.adblocker.continued.hook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class DonateHook {

    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {

        XC_MethodHook paymentIntentHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;
                if (activity != null) {
                    Intent intent = activity.getIntent();
                    if (intent != null) {
                        String className = intent.getComponent().getClassName();
                        if (intent.hasExtra("wxid_90m10eigpruz21") && !TextUtils.isEmpty(className) && className.equals("com.tencent.mm.ui.LauncherUI")) {
                            Intent donateIntent = new Intent();
                            donateIntent.setClassName(activity, "com.tencent.mm.plugin.remittance.ui.RemittanceUI")
                                    .putExtra("scene", 1)
                                    .putExtra("pay_scene", 32)
                                    .putExtra("pay_channel", 13)
                                    .putExtra("receiver_name", "wxid_90m10eigpruz21")
                                    .removeExtra("wxid_90m10eigpruz21");
                            activity.startActivity(donateIntent);
                            activity.finish();
                        }
                    }
                }
            }
        };

        if (lpparam.packageName.equals("com.tencent.mm")) {
            XposedHelpers.findAndHookMethod("com.tencent.mm.ui.LauncherUI", lpparam.classLoader, "onCreate", Bundle.class, paymentIntentHook);
        }
    }
}
