package com.acedit.assignamo.preferences;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.acedit.assignamo.R;
	
@SuppressWarnings("deprecation")
public class Preferences extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		addPreferencesFromResource(R.xml.preferences);
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Preference aboutPref = (Preference)findPreference("pref_app_version_key");
		String version = "";
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {}
		aboutPref.setSummary(version + "");
	}
	
}
