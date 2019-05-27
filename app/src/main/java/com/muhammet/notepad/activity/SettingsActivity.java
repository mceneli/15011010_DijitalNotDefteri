package com.muhammet.notepad.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.muhammet.notepad.R;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener{
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_preferences);

        bindPreferenceSummaryToValue(findPreference("theme"));
        bindPreferenceSummaryToValue(findPreference("font_size"));
        bindPreferenceSummaryToValue(findPreference("sort_by"));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addPreferencesFromResource(R.xml.settings_preferences_md);
            SharedPreferences pref = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
            findPreference("direct_edit").setOnPreferenceChangeListener(this);
            findPreference("direct_edit").setEnabled(!pref.getBoolean("markdown", false));

            findPreference("markdown").setOnPreferenceChangeListener(this);
            findPreference("markdown").setEnabled(!pref.getBoolean("direct_edit", false));
        }
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();

        if(preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        } else {
            preference.setSummary(stringValue);
        }

        return true;
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference
        .setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(
                        preference.getContext()).getString(preference.getKey(),
                                ""));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        switch(preference.getKey()) {
            case "direct_edit":
                findPreference("markdown").setEnabled(!(Boolean) value);
                break;
            case "markdown":
                findPreference("direct_edit").setEnabled(!(Boolean) value);
                break;
        }

        return true;
    }
}
