package com.aviraxp.adblocker.continued.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import com.aviraxp.adblocker.continued.BuildConfig;
import com.aviraxp.adblocker.continued.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import moe.feng.alipay.zerosdk.AlipayZeroSdk;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {

    static boolean isActivated = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
        }
        addPreferencesFromResource(R.xml.pref_settings);
        new AppPicker().execute();
        checkState();
        prepareDonationStatus();
        paypalDonate();
        openGithub();
        openXDA();
        hideIconListener();
        licensesListener();
    }

    private void openXDA() {
        findPreference("XDA").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW)
                        .setData(Uri.parse("https://forum.xda-developers.com/xposed/modules/xposed-adblocker-reborn-1-0-1-2017-02-11-t3554617"));
                startActivity(intent);
                return true;
            }
        });
    }

    private void paypalDonate() {
        findPreference("DONATE_PAYPAL").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW)
                        .setData(Uri.parse("https://paypal.me/wanghan1995315"));
                startActivity(intent);
                return true;
            }
        });
    }

    private void prepareDonationStatus() {
        removePreference("com.tencent.mm", "DONATE_WECHAT");
        removePreference("com.eg.android.AlipayGphone", "DONATE_ALIPAY");
    }

    private void removePreference(String packageName, String perfName) {
        try {
            PackageInfo info = getApplicationContext().getPackageManager().getPackageInfo(packageName, 0);
            boolean isAvailable = (info != null);
            if (!isAvailable) {
                PreferenceCategory displayOptions = (PreferenceCategory) findPreference("ABOUT");
                displayOptions.removePreference(findPreference(perfName));
            } else if (packageName.equals("com.tencent.mm")) {
                donateWechat();
            } else if (packageName.equals("com.eg.android.AlipayGphone")) {
                donateAlipay();
            }
        } catch (Throwable t) {
            PreferenceCategory displayOptions = (PreferenceCategory) findPreference("ABOUT");
            displayOptions.removePreference(findPreference(perfName));
        }
    }

    private void licensesListener() {
        findPreference("LICENSES").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new LicensesDialog(SettingsActivity.this)
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
        try {
            Intent intent = new Intent("de.robv.android.xposed.installer.OPEN_SECTION");
            if (getPackageManager().queryIntentActivities(intent, 0).isEmpty()) {
                intent = getPackageManager().getLaunchIntentForPackage("de.robv.android.xposed.installer");
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("section", "modules")
                    .putExtra("fragment", 1)
                    .putExtra("module", BuildConfig.APPLICATION_ID);
            startActivity(intent);
        } catch (Throwable t) {
            Toast.makeText(getApplicationContext(), R.string.hint_reboot_not_active_failed, Toast.LENGTH_SHORT).show();
        }
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

    private void donateWechat() {
        findPreference("DONATE_WECHAT").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
                        .putExtra("wxid_90m10eigpruz21", true);
                startActivity(intent);
                return true;
            }
        });
    }

    private void openGithub() {
        findPreference("GITHUB").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW)
                        .setData(Uri.parse("https://github.com/aviraxp/AdBlocker_Reborn"));
                startActivity(intent);
                return true;
            }
        });
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

    public class AppPicker extends AsyncTask<Void, Void, Void> {

        final MultiSelectListPreference disabledApps = (MultiSelectListPreference) findPreference("DISABLED_APPS");
        final List<CharSequence> appNames = new ArrayList<>();
        final List<CharSequence> packageNames = new ArrayList<>();

        protected void onPreExecute() {
            disabledApps.setEnabled(false);
        }

        protected Void doInBackground(Void... arg0) {

            final List<String[]> sortedApps = new ArrayList<>();
            final PackageManager pm = getApplicationContext().getPackageManager();
            final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo app : packages) {
                sortedApps.add(new String[]{app.packageName, app.loadLabel(pm).toString()});
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
            CharSequence[] appNamesList = appNames.toArray(new CharSequence[appNames.size()]);
            CharSequence[] packageNamesList = packageNames.toArray(new CharSequence[packageNames.size()]);
            disabledApps.setEntries(appNamesList);
            disabledApps.setEntryValues(packageNamesList);
            disabledApps.setEnabled(true);
        }
    }
}
