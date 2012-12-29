package com.acedit.assignamo;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.setup.SetupWizard;

public class LoadingActivity extends Activity {
	
	private static final short MIN_DISPLAY_TIME = 1000;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading_screen);
		
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				proceedToNextActivity();
			}
		};
		
		new Timer().schedule(task, MIN_DISPLAY_TIME);
	}
	
	private void proceedToNextActivity() {
		if (isDatabaseInitialized())
			startActivity(new Intent(this, MainActivity.class));
		else
			startActivity(new Intent(this, SetupWizard.class));
		finish();
	}
	
	private boolean isDatabaseInitialized() {
    	java.io.File dbFile = new java.io.File("/data/data/" + getApplicationContext().getPackageName() + "/databases/" + Values.DATABASE_NAME);
    	if (!dbFile.exists())
    		return false;
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	if (!prefs.getBoolean(Values.PREFS_DB_INITIALIZED, false))
    		return false;
    	return true;
    }
	
}
