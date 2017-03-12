package com.aviraxp.adblocker.continued.helper;

import android.os.SystemProperties;

import com.aviraxp.adblocker.continued.BuildConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XSharedPreferences;

public class PreferencesHelper {

    private static XSharedPreferences preferences = null;
    private static boolean isNoReloadPreferences = false;

    private static XSharedPreferences getModuleSharedPreferences() {
        if (preferences == null) {
            preferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
            preferences.makeWorldReadable();
            isNoReloadPreferences = preferences.getBoolean("NORELOAD", false);
        } else if (!isNoReloadPreferences) {
            preferences.reload();
        }
        return preferences;
    }

    public static boolean isActViewHookEnabled() {
        return getModuleSharedPreferences().getBoolean("ACTVIEW_HOOK", true);
    }

    public static boolean isHostsHookEnabled() {
        return getModuleSharedPreferences().getBoolean("HOSTS_HOOK", true);
    }

    public static boolean isWebViewHookEnabled() {
        return getModuleSharedPreferences().getBoolean("WEBVIEW_HOOK", true);
    }

    public static boolean isServicesHookEnabled() {
        return getModuleSharedPreferences().getBoolean("SERVICES_HOOK", true);
    }

    public static boolean isReceiversHookEnabled() {
        return getModuleSharedPreferences().getBoolean("RECEIVERS_HOOK", true);
    }

    public static boolean isBackPressHookEnabled() {
        return getModuleSharedPreferences().getBoolean("BACKPRESS_HOOK", false);
    }

    public static boolean isAggressiveHookEnabled() {
        return getModuleSharedPreferences().getBoolean("AGGRESSIVE_HOOK", false);
    }

    public static boolean isShortcutHookEnabled() {
        return getModuleSharedPreferences().getBoolean("SHORTCUT_HOOK", false);
    }

    public static boolean isDisableSystemApps() {
        return getModuleSharedPreferences().getBoolean("SYSTEMAPPS", false);
    }

    public static boolean isDebugModeEnabled() {
        return getModuleSharedPreferences().getBoolean("DEBUG", false);
    }

    public static boolean isAndroidApp(String string) {
        return string.startsWith("com.android") && !string.equals("com.android.webview");
    }

    public static boolean isMIUI() {
        return !SystemProperties.get("ro.miui.ui.version.name", "").equals("");
    }

    public static Set<String> disabledApps() {
        return getModuleSharedPreferences().getStringSet("DISABLED_APPS", new HashSet<String>());
    }

    public static List<String> whiteListElements() {
        return Arrays.asList(getModuleSharedPreferences().getString("DISABLED_ELEMENTS", "").split("\n"));
    }

}
