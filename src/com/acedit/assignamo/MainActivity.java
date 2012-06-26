package com.acedit.assignamo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;

import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.preferences.Preferences;
import com.acedit.assignamo.utils.DbUtils;
import com.awesomeapplets.assignamo.R;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

public class MainActivity extends FragmentActivity {
	
	static enum ACTIVITY_STATE { ADD, EDIT }
    short titleCount;
    short selectedPos;
    TitlePageAdapter adapter;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	//TODO Add Welcome/Setup screen
    	if (savedInstanceState != null)
    		selectedPos = savedInstanceState.getShort(KEY_POSITION);
        setContentView(R.layout.tab_pager);
    	
    	checkForExistingDatabase();
    }
    
    /**
     * Checks to see if the database already exists. If not, it creates one.
     * @return true if the database already existed
     */
    private boolean checkForExistingDatabase() {
    	java.io.File dbFile = new java.io.File("/data/data/" + getApplicationContext().getPackageName() + "/databases/" + Values.DATABASE_NAME);
    	if (!dbFile.exists()) {
    		DbAdapter a = new DbAdapter(this, Values.DATABASE_NAME, Values.DATABASE_VERSION, "temp", Values.DATABASE_CREATE, Values.KEY_ROWID);
    		a.open();
    		a.close();
    		return false;
    	}
    	return true;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	
    	String[] courses = DbUtils.getCoursesAsArray(this);
    	String[] titles = new String[courses.length+1];
    	titleCount = (short)titles.length;
    	titles[0] = getString(R.string.all_assignments);
    	for (int i = 0; i < courses.length; i++)
    		titles[i+1] = courses[i];
        adapter = new TitlePageAdapter(getSupportFragmentManager(),titles,this);
        
        ViewPager pager = (ViewPager)findViewById(R.id.viewpager);
        pager.setAdapter(adapter);
        
        TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        indicator.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				setPosition(arg0);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {}
		});
        indicator.setCurrentItem(selectedPos);
        pager.setCurrentItem(selectedPos);
    }
    
    // Save the current tab position
    private static final String KEY_POSITION = "pos";
    
    private void setPosition(int pos) {
    	selectedPos = (short)pos;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	outState.putShort(KEY_POSITION, selectedPos);
    }
    
    /* ---------- Options Menu for All Assignment Lists ---------- */
    
    private final int OPTIONS_ASSIGNMENT_ADD = 0;
    private final int OPTIONS_MANAGE = 1;
    private final int OPTIONS_PREFERENCES = 2;
    
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, OPTIONS_ASSIGNMENT_ADD, 0, R.string.assignment_add).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, OPTIONS_MANAGE, 0, R.string.assignment_menu_manage).setIcon(android.R.drawable.ic_menu_manage);
		menu.add(0, OPTIONS_PREFERENCES, 0, R.string.preferences).setIcon(android.R.drawable.ic_menu_preferences);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case OPTIONS_ASSIGNMENT_ADD:
			Intent iA = new Intent(this, AssignmentEditFragment.class);
			iA.putExtra(Values.NEW_ASSIGNMENT_COURSE_KEY, (short)(selectedPos - 1));
			startActivity(iA);
			return true;
		case OPTIONS_MANAGE:
			//TODO
			break;
		case OPTIONS_PREFERENCES:
			Intent iP = new Intent(this, Preferences.class);
			startActivity(iP);
			return true;
		}
		return true;
	}
    
    class TitlePageAdapter extends FragmentAdapter implements TitleProvider {
    	String[] titles;
		public TitlePageAdapter(FragmentManager fm, String[] titles, Context context) {
			super(fm,titles);
			this.titles = titles;
			this.context = context;
		}

		@Override
		public int getCount() {
			return titles.length;
		}

		@Override
		public String getTitle(int position) {
			return titles[position % titles.length].toUpperCase();
		}
	}
}