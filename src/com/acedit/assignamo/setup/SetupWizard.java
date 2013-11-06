package com.acedit.assignamo.setup;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;

import com.acedit.assignamo.MainActivity;
import com.acedit.assignamo.R;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.setup.AddFragment.DisplayState;
import com.acedit.assignamo.setup.AddFragment.ItemExistenceMonitor;

public class SetupWizard extends FragmentActivity implements ItemExistenceMonitor {
	
	private static enum Page { WELCOME, TEACHERS, COURSES, TIPS };
	
	private static Page displayedPage;
	private static Button backButton, nextButton;
	private static AddFragment addFragment = new AddFragment();
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup_wizard);
		setFragmentView(new WelcomeFragment());
		addFragment = new AddFragment();
		displayedPage = Page.WELCOME;
		backButton = (Button) findViewById(R.id.setup_wizard_back_button);
		nextButton = (Button) findViewById(R.id.setup_wizard_next_button);
	}
	
	public void backClicked(View v) {
		switch (displayedPage) {
		case WELCOME:
			finish();
		case TEACHERS:
			setFragmentView(new WelcomeFragment());
			displayedPage = Page.WELCOME;
			backButton.setText(getString(android.R.string.cancel));
			nextButton.setEnabled(true);
			break;
		case COURSES:
			addFragment.setDisplayState(DisplayState.TEACHERS);
			displayedPage = Page.TEACHERS;
			break;
		case TIPS:
			setFragmentView(addFragment);
			nextButton.setText(getString(R.string.next));
			displayedPage = Page.COURSES;
		}
	}
	
	public void nextClicked(View v) {
		switch (displayedPage) {
		case WELCOME:
			setFragmentView(addFragment);
			displayedPage = Page.TEACHERS;
			backButton.setText(getString(R.string.back));
			break;
		case TEACHERS:
			addFragment.setDisplayState(DisplayState.COURSES);
			displayedPage = Page.COURSES;
			break;
		case COURSES:
			setFragmentView(new TipsFragment());
			displayedPage = Page.TIPS;
			nextButton.setText(getString(R.string.finish));
			break;
		case TIPS:
			startActivity(new Intent(this, MainActivity.class));
			PreferenceManager.getDefaultSharedPreferences(this)
			.edit().putBoolean(Values.PREFS_DB_INITIALIZED, true).commit();
			finish();
		}
	}
	
	private void setFragmentView(Fragment newFragment) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragTrans = fragmentManager.beginTransaction();
		fragTrans.replace(R.id.setup_wizard_fragment_container, newFragment);
		fragTrans.commit();
	}

	public void itemsExist(boolean itemsDoExist) {
		nextButton.setEnabled(itemsDoExist);
	}
	
}
