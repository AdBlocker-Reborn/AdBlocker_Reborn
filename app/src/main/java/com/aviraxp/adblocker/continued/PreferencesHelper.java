package com.aviraxp.adblocker.continued;

import java.lang.ref.WeakReference;

import de.robv.android.xposed.XSharedPreferences;

class PreferencesHelper {

    private static WeakReference<XSharedPreferences> xSharedPreferences = new WeakReference<>(null);

    private static XSharedPreferences getModuleSharedPreferences() {
        XSharedPreferences preferences = xSharedPreferences.get();
        if (preferences == null) {
            preferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
            preferences.makeWorldReadable();
            xSharedPreferences = new WeakReference<>(preferences);
        } else {
            preferences.reload();
        }
        return preferences;
    }

    static boolean isActViewHookEnabled() {
        return getModuleSharedPreferences().getBoolean("ACTVIEW_HOOK", true);
    }

    static boolean isHostsHookEnabled() {
        return getModuleSharedPreferences().getBoolean("HOSTS_HOOK", true);
    }

    static boolean isWebViewHookEnabled() {
        return getModuleSharedPreferences().getBoolean("WEBVIEW_HOOK", true);
    }

    static boolean isServicesHookEnabled() {
        return getModuleSharedPreferences().getBoolean("SERVICES_HOOK", true);
    }

    static boolean isReceiversHookEnabled() {
        return getModuleSharedPreferences().getBoolean("RECEIVERS_HOOK", true);
    }

    static boolean isHidingHookEnabled() {
        return getModuleSharedPreferences().getBoolean("HIDING_HOOK", true);
    }
}
