package com.aviraxp.adblocker.continued.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.aviraxp.adblocker.continued.BuildConfig;
import com.aviraxp.adblocker.continued.R;

public class SettingsActivity extends PreferenceActivity {

    public final static boolean isActivated = false;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.pref_general);
        checkState();
    }

    private void checkState() {
        if (!isActivated) {
            showNotActive();
        }
    }

    private void showNotActive() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(R.string.hint_reboot_not_active)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        openXposed();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void openXposed() {
        Intent intent = new Intent("de.robv.android.xposed.installer.OPEN_SECTION");
        if (getPackageManager().queryIntentActivities(intent, 0).isEmpty()) {
            intent = getPackageManager().getLaunchIntentForPackage("de.robv.android.xposed.installer");
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("section", "modules").putExtra("fragment", 1).putExtra("module", BuildConfig.APPLICATION_ID);
        startActivity(intent);
    }
}
