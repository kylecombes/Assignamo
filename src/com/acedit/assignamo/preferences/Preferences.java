package com.acedit.assignamo.preferences;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.acedit.assignamo.R;

public class Preferences extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		addPreferencesFromResource(R.xml.preferences);
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		/*
		 * SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			Editor prefEditor = prefs.edit();
			if (prefs.getBoolean(Values.ASSIGNMENT_KEY_SHOWING_COMPLETED, false))
				prefEditor.putBoolean(Values.ASSIGNMENT_KEY_SHOWING_COMPLETED, false);
			else
				prefEditor.putBoolean(Values.ASSIGNMENT_KEY_SHOWING_COMPLETED, true);
			prefEditor.commit();
		 */
		
		((Preference)findPreference("pref_contact_key"))
		.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				Intent i = new Intent(android.content.Intent.ACTION_SEND);
				i.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { getString(R.string.contact_email) });
				i.setType("text/css");
				startActivity(Intent.createChooser(i, getString(R.string.pref_email_chooser_title)));
				return false;
			}
		});
		
		((Preference)findPreference("pref_visit_key"))
		.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				Intent i = new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.website)));
				startActivity(i);
				return false;
			}
		});
		
		Preference aboutPref = (Preference)findPreference("pref_app_version_key");
		String version = "";
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {}
		String versionTitle;
		if (version.length() > 0)
			versionTitle = "" + version;
		else
			versionTitle = getString(R.string.pref_unknown_version);
		aboutPref.setSummary(versionTitle);
	}
	
}
