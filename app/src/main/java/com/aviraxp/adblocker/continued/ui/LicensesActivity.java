package com.aviraxp.adblocker.continued.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.aviraxp.adblocker.continued.R;

public class LicensesActivity extends PreferenceActivity {

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perf_licenses);
    }
}
