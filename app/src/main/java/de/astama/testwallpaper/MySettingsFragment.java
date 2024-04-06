package de.astama.testwallpaper;

import android.os.Bundle;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Objects;

public class MySettingsFragment extends PreferenceFragmentCompat {
    public static int size = 3;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        {
            EditTextPreference sizeStore = findPreference("size");
            if (sizeStore != null) {
                String tmp = sizeStore.getText();
                if (tmp == null){
                    sizeStore.setText("3");
                }else {
                    sizeStore.setTitle(tmp);
                    size = Integer.parseInt(tmp);
                }
            }
            PreferenceCategory list = findPreference("colours");
            if (list != null) {
                fixListAmount(list);
            }
        }
        Preference tmp2 = findPreference("increase");
        if (tmp2 != null) {
            tmp2.setOnPreferenceClickListener((preference) -> {
                size++;
                EditTextPreference sizeStore = findPreference("size");
                if (sizeStore != null) {
                    sizeStore.setTitle("" + size);
                    sizeStore.setText("" + size);
                }
                PreferenceCategory list = findPreference("colours");
                if (list != null) {
                    fixListAmount(list);
                }
                return true;
            });
        }
        Preference tmp3 = findPreference("decrease");
        if (tmp3 != null) {
            tmp3.setOnPreferenceClickListener((preference) -> {
                if (size > 2) {
                    size--;
                    EditTextPreference sizeStore = findPreference("size");
                    if (sizeStore != null) {
                        sizeStore.setTitle("" + size);
                        sizeStore.setText("" + size);
                    }
                    PreferenceCategory list = findPreference("colours");
                    if (list != null) {
                        fixListAmount(list);
                    }
                }
                return true;

            });
        }
    }
    private void fixListAmount(PreferenceCategory lst) {
        lst.removeAll();
        for(int i = 0; i < size; i++){
            EditTextPreference tmp = new EditTextPreference(this.requireContext());
            tmp.setKey("" + i);
            tmp.setTitle("" + i);
            tmp.setText("#ffffff");
            tmp.setPersistent(true);
            lst.addPreference(tmp);
        }
    }

}

