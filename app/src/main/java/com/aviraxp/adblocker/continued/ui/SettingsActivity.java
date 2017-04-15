package com.aviraxp.adblocker.continued.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.aviraxp.adblocker.continued.BuildConfig;
import com.aviraxp.adblocker.continued.R;
import com.oasisfeng.condom.CondomContext;
import com.zhuge.analysis.stat.ZhugeSDK;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import moe.feng.alipay.zerosdk.AlipayZeroSdk;

@SuppressWarnings("deprecation")
@SuppressLint("WorldReadableFiles")
public class SettingsActivity extends PreferenceActivity {

    static boolean isActivated = false;
    private static boolean useSDK = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWorldReadable();
        addPreferencesFromResource(R.xml.pref_settings);
        checkState();
        showUpdateLog();
        checkSDKPermission();
        new AppPicker().execute();
        removePreference();
        uriListener();
        hideIconListener();
        licensesListener();
        analysisSDKInit();
    }

    private void checkSDKPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission("android.permission.READ_PHONE_STATE") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, 0);
            }
        }
        analysisSDKInit();
    }

    @SuppressLint("HardwareIds")
    private void analysisSDKInit() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission("android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_GRANTED) {
            ZhugeSDK.getInstance().init(CondomContext.wrap(getApplicationContext(), "ZhuGeIO"));
            JSONObject personObject = new JSONObject();
            ZhugeSDK.getInstance().identify(CondomContext.wrap(getApplicationContext(), "ZhuGeIO"), Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID), personObject);
            useSDK = true;
        }
    }

    private void showUpdateLog() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sp.getInt("VERSION", 0) != BuildConfig.VERSION_CODE) {
            new LicensesDialog(SettingsActivity.this, getLocalUpdateLog())
                    .setTitle(R.string.updatelog)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            sp.edit().putInt("VERSION", BuildConfig.VERSION_CODE)
                    .apply();
        }
    }

    private String getLocalUpdateLog() {
        if (Locale.getDefault().getLanguage().equals("zh")) {
            return "file:///android_asset/html/update_cn.html";
        } else {
            return "file:///android_asset/html/update_en.html";
        }
    }

    private void uriListener() {
        uriHelper("DONATE_PAYPAL", "https://paypal.me/wanghan1995315");
        uriHelper("GITHUB", "https://github.com/aviraxp/AdBlocker_Reborn");
        uriHelper("XDA", "https://forum.xda-developers.com/xposed/modules/xposed-adblocker-reborn-1-0-1-2017-02-11-t3554617");
    }

    private void uriHelper(String pref, final String uri) {
        findPreference(pref).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW)
                        .setData(Uri.parse(uri));
                startActivity(intent);
                return true;
            }
        });
    }

    private void removePreference() {
        try {
            PackageInfo info = getApplicationContext().getPackageManager().getPackageInfo("com.eg.android.AlipayGphone", 0);
            boolean isAvailable = info != null;
            if (!isAvailable) {
                PreferenceCategory displayOptions = (PreferenceCategory) findPreference("ABOUT");
                displayOptions.removePreference(findPreference("DONATE_ALIPAY"));
            } else {
                donateAlipay();
            }
        } catch (Throwable t) {
            PreferenceCategory displayOptions = (PreferenceCategory) findPreference("ABOUT");
            displayOptions.removePreference(findPreference("DONATE_ALIPAY"));
        }
    }

    private void licensesListener() {
        findPreference("LICENSES").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new LicensesDialog(SettingsActivity.this, "file:///android_asset/html/licenses.html")
                        .setTitle(R.string.licensedialog)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return true;
            }
        });
    }

    private void checkState() {
        if (!isActivated) {
            new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setMessage(R.string.hint_reboot_not_active)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            openXposed();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    private void openXposed() {
        Intent intent = new Intent("de.robv.android.xposed.installer.OPEN_SECTION");
        if (getPackageManager().queryIntentActivities(intent, 0).isEmpty()) {
            intent = getPackageManager().getLaunchIntentForPackage("de.robv.android.xposed.installer");
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("section", "modules")
                .putExtra("fragment", 1)
                .putExtra("module", BuildConfig.APPLICATION_ID);
        startActivity(intent);
    }

    private void donateAlipay() {
        findPreference("DONATE_ALIPAY").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlipayZeroSdk.startAlipayClient(SettingsActivity.this, "aex00388woilyb9ln32hlfe");
                return true;
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("SetWorldReadable")
    private void setWorldReadable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            File dataDir = new File(getApplicationInfo().dataDir);
            File prefsDir = new File(dataDir, "shared_prefs");
            File prefsFile = new File(prefsDir, getPreferenceManager().getSharedPreferencesName() + ".xml");
            if (prefsFile.exists()) {
                for (File file : new File[]{dataDir, prefsDir, prefsFile}) {
                    file.setReadable(true, false);
                    file.setExecutable(true, false);
                }
            }
        } else {
            getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
        }
    }

    private void hideIconListener() {
        findPreference("HIDEICON").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object obj) {
                PackageManager packageManager = SettingsActivity.this.getPackageManager();
                ComponentName aliasName = new ComponentName(SettingsActivity.this, BuildConfig.APPLICATION_ID + ".SettingsActivityLauncher");
                if ((boolean) obj) {
                    packageManager.setComponentEnabledSetting(aliasName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                } else {
                    packageManager.setComponentEnabledSetting(aliasName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                }
                return true;
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        if (useSDK) {
            ZhugeSDK.getInstance().flush(CondomContext.wrap(getApplicationContext(), "ZhuGeIO"));
        }
    }

    private class AppPicker extends AsyncTask<Void, Void, Void> {

        private final MultiSelectListPreference disabledApps = (MultiSelectListPreference) findPreference("DISABLED_APPS");
        private final List<CharSequence> appNames = new ArrayList<>();
        private final List<CharSequence> packageNames = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            disabledApps.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            final List<String[]> sortedApps = new ArrayList<>();
            final PackageManager pm = getApplicationContext().getPackageManager();
            final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo app : packages) {
                if (!app.packageName.startsWith("com.android")) {
                    sortedApps.add(new String[]{app.packageName, app.loadLabel(pm).toString()});
                }
            }

            Collections.sort(sortedApps, new Comparator<String[]>() {
                @Override
                public int compare(String[] entry1, String[] entry2) {
                    return entry1[1].compareToIgnoreCase(entry2[1]);
                }
            });

            for (int i = 0; i < sortedApps.size(); i++) {
                appNames.add(sortedApps.get(i)[1] + "\n" + "(" + sortedApps.get(i)[0] + ")");
                packageNames.add(sortedApps.get(i)[0]);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            final CharSequence[] appNamesList = appNames.toArray(new CharSequence[appNames.size()]);
            final CharSequence[] packageNamesList = packageNames.toArray(new CharSequence[packageNames.size()]);
            disabledApps.setEntries(appNamesList);
            disabledApps.setEntryValues(packageNamesList);
            disabledApps.setEnabled(true);
        }
    }
}
