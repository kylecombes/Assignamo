package com.acedit.assignamo.manage;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.acedit.assignamo.R;
import com.acedit.assignamo.database.Values;

public class CourseListFragment extends BaseListFragment {
	
	public CourseListFragment() {
		
		
	}
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = getDbAdapter(Values.COURSE_TABLE);
		setListFrom( new String[] { Values.KEY_NAME, Values.COURSE_KEY_TEACHER,
				Values.COURSE_KEY_TIMES_OF_DAY } );
		setListTo( new int[] { R.id.list_title, R.id.list_teacher, R.id.list_days } );
		setListItem(R.layout.course_list_item);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.course_list, container, false);
		return v;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(context, CourseViewFragment.class);
		i.putExtra(Values.KEY_ROWID, id);
		startActivity(i);
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(R.string.course_add);
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// (The only choice is to add a course)
		Intent i = new Intent(context, CourseEditActivity.class);
		startActivity(i);
    	return true;
    }

    private static final int CONTEXT_EDIT = 2;
    private static final int CONTEXT_DELETE = 3;
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(0, CONTEXT_EDIT, 0, getString(R.string.course_edit));
		menu.add(0, CONTEXT_DELETE, 0, getString(R.string.course_delete));
    }
    
    public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    	switch (item.getItemId()) {
    	case CONTEXT_EDIT:
    		Intent i = new Intent(context, CourseEditActivity.class);
    		i.putExtra(Values.KEY_ROWID, info.id);
    		startActivity(i);
    		return true;
    	case CONTEXT_DELETE:
			delete(info.id);
			fillData();
			return true;
		}
    	return false;
    }
}
