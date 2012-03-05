package com.awesomeapplets.assignamo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.DbUtils;
import com.awesomeapplets.assignamo.database.Values;
import com.viewpagerindicator.*;

public class MainActivity extends FragmentActivity {
	
    public static final String DATABASE_NAME = "data.db";
    public static final short DATABASE_VERSION = 1;
    static enum ACTIVITY_STATE { ADD, EDIT }
    short titleCount;
    short selectedPos;
    TitlePageAdapter adapter;

	public static final String KEY_ROWID = "_id";
	public static final String[] DATABASE_CREATE = {Values.ASSIGNMENT_DATABASE_CREATE,
		Values.COURSE_DATABASE_CREATE, Values.BOOK_DATABASE_CREATE, Values.TEACHER_DATABASE_CREATE };
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	if (savedInstanceState != null)
    		selectedPos = savedInstanceState.getShort(KEY_POSITION);
        setContentView(R.layout.main);
    	
    	checkForExistingDatabase();
    }
    
    /**
     * Checks to see if the database already exists. If not, it creates one.
     * @return true if the database already existed
     */
    private boolean checkForExistingDatabase() {
    	java.io.File dbFile = new java.io.File("/data/data/" + getApplicationContext().getPackageName() + "/databases/" + MainActivity.DATABASE_NAME);
    	android.util.Log.d("onCreate", "File name: " + dbFile.toString());
    	if (!dbFile.exists()) {
    		DbAdapter a = new DbAdapter(this, DATABASE_NAME, DATABASE_VERSION, "temp", DATABASE_CREATE, KEY_ROWID);
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
				// TODO Auto-generated method stub
				setPosition(arg0);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
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