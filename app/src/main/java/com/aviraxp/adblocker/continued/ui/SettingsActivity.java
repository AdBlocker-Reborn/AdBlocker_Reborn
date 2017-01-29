package com.aviraxp.adblocker.continued.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.aviraxp.adblocker.continued.BuildConfig;
import com.aviraxp.adblocker.continued.R;

import de.psdev.licensesdialog.LicensesDialog;
import moe.feng.alipay.zerosdk.AlipayZeroSdk;

public class SettingsActivity extends PreferenceActivity {

    static boolean isActivated = false;

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.pref_settings);
        checkState();
        donateAlipay();
        donateWechat();
        openGithub();
        hideIconListener();
        licensesListener();
    }

    @SuppressWarnings("deprecation")
    private void licensesListener() {
        findPreference("LICENSES").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new LicensesDialog.Builder(SettingsActivity.this)
                        .setNotices(R.raw.licenses)
                        .setIncludeOwnLicense(false)
                        .build()
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

    @SuppressWarnings("deprecation")
    private void donateAlipay() {
        findPreference("DONATE_ALIPAY").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (AlipayZeroSdk.hasInstalledAlipayClient(getApplicationContext())) {
                    AlipayZeroSdk.startAlipayClient(SettingsActivity.this, "aex00388woilyb9ln32hlfe");
                } else {
                    Toast.makeText(getApplicationContext(), R.string.donate_alipay_failed, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void donateWechat() {
        findPreference("DONATE_WECHAT").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
            	try {
                    Intent intent = new Intent();
                    intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
                            .putExtra("wechat_donate", true);
                    startActivity(intent);
                } catch (Throwable t) {
                	Toast.makeText(getApplicationContext(), R.string.donate_wechat_failed, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
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
}