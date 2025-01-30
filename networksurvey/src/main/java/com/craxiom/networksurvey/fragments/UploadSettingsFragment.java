package com.craxiom.networksurvey.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.craxiom.networksurvey.R;

public class UploadSettingsFragment extends PreferenceFragmentCompat
{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.upload_preferences, rootKey);
    }
}
