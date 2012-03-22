package com.awesomeapplets.assignamo.preferences;

import com.awesomeapplets.assignamo.R;
import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.Values;
import com.awesomeapplets.assignamo.utils.DbUtils;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class CourseListActivity extends ListActivity {
	
	private DbAdapter courseDb;
	
	public CourseListActivity() {
		
	}
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	/** Called when the activity becomes visible. */
	public void onStart() {
		super.onStart();
		registerForContextMenu(getListView());
	}
	
	public void onResume() {
		super.onResume();
		openDatabase();
		fillData();
	}
	
	public void onPause() {
		super.onPause();
		closeDatabase();
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.course_list, container, false);
		return v;
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.course_menu, menu);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, CourseViewFragment.class);
		i.putExtra(Values.KEY_ROWID, id);
		startActivity(i);
	}
	
	public void openDatabase() {
		if (courseDb == null)
			courseDb = new DbAdapter(this, Values.DATABASE_NAME, Values.DATABASE_VERSION,
					Values.COURSE_TABLE, new String[0], Values.KEY_ROWID);
		courseDb.open();
	}
	
	public void closeDatabase() {
		if (courseDb != null)
			courseDb.close();
	}
	
    public void fillData() {
    	Cursor coursesCursor = courseDb.fetchAll(Values.COURSE_FETCH);
    	startManagingCursor(coursesCursor);
    	
    	// Create and array to specify the fields we want
    	String[] from = new String[] {Values.KEY_NAME, Values.COURSE_KEY_TEACHER};
    	
    	// and an array of the fields we want to bind in the view
    	int[] to = new int[]{R.id.course_list_name, R.id.course_list_teacher};
    	
    	// Now create a simple cursor adapter and set it to display
    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.course_list_item, coursesCursor, from, to);
    	adapter.setViewBinder(new ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				// TODO Auto-generated method stub
				if (columnIndex == 2) {
					String[] teachers = DbUtils.getTeachersAsArray(getApplicationContext());
					short value = cursor.getShort(columnIndex);
					TextView textView = (TextView)view;
					textView.setText(teachers[value]);
					return true;
				}
				return false;
			}
		});
    	setListAdapter(adapter);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.course_menu, menu);
    	return true;
    }
    
    // TODO
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case R.id.course_menu_add:
			Intent i = new Intent(this, CourseEditActivity.class);
			startActivity(i);
    		return true;
		}
    	return true;
    }
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.course_longpress, menu);
    }
    
    public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    	switch (item.getItemId()) {
    	case R.id.course_context_menu_delete:
    		// Delete the course
			courseDb.delete(info.id);
			fillData();
			return true;
    	case R.id.course_context_menu_edit:
    		Intent i = new Intent(this, CourseEditActivity.class);
    		i.putExtra(Values.KEY_ROWID, info.id);
    		startActivity(i);
    		return true;
		}
    	return true;
    }
}
