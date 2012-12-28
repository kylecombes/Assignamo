package com.acedit.assignamo.manage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;

import com.acedit.assignamo.FragmentAdapter;
import com.acedit.assignamo.R;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

public class ManageActivity extends FragmentActivity {

	short titleCount;
    short selectedPos;
    TitlePageAdapter adapter;
    ViewPager pager;
    TabPageIndicator indicator;
    private String[] titles;
    private static final String KEY_POSITION = "pos";
    
    private static final short COURSES = 0;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if (savedInstanceState != null)
    		selectedPos = savedInstanceState.getShort(KEY_POSITION);
        setContentView(R.layout.tab_pager);
        
        titles = new String[] { getString(R.string.manage_courses),
        		getString(R.string.manage_teachers) };
        titleCount = (short)titles.length;
    	adapter = new TitlePageAdapter(getSupportFragmentManager(),titles,this);
        
        pager = (ViewPager)findViewById(R.id.viewpager);
        pager.setAdapter(adapter);
        
        indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        indicator.setOnPageChangeListener(new OnPageChangeListener() {
			
			public void onPageSelected(int arg0) {
				setPosition(arg0);
			}
			
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			
			public void onPageScrollStateChanged(int arg0) {}
		});
    }

    @Override
    public void onResume() {
    	super.onResume();
        indicator.setCurrentItem(selectedPos);
        pager.setCurrentItem(selectedPos);
    }
    
    private void setPosition(int pos) {
    	selectedPos = (short)pos;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	outState.putShort(KEY_POSITION, selectedPos);
    }
    

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.clear();
    	switch (selectedPos) {
    	case COURSES:
    		menu.add(R.string.course_add).setIcon(android.R.drawable.ic_menu_add);
    		return true;
    	default:
    		menu.add(R.string.teacher_add).setIcon(android.R.drawable.ic_menu_add);
    		return true;
    	}
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i;
    	// (There is only one option, so there is no need to check the MenuItem id)
    	switch (selectedPos) {
    	case COURSES:
    		i = new Intent(getBaseContext(), CourseEditActivity.class);
    		break;
    	default:
    		i = new Intent(getBaseContext(), TeacherEditActivity.class);
    	}
		startActivity(i);
    	return true;
    }
    

    class TitlePageAdapter extends FragmentAdapter implements TitleProvider {
    	String[] titles;
    	Context context;
		public TitlePageAdapter(FragmentManager fm, String[] titles, Context context) {
			super(fm,titles);
			this.titles = titles;
			this.context = context;
		}
		
		@Override
		public Fragment getItem(int position) {
			setCurrentPosition(position);
			switch (position) {
			case COURSES:
				return new CourseListFragment();
			default:
				return new TeacherListFragment();
			}
		}
		
		@Override
		public int getCount() {
			return titles.length;
		}

		public String getTitle(int position) {
			return titles[position % titles.length].toUpperCase();
		}
	}

}
