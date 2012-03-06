package com.awesomeapplets.assignamo.preferences;


import com.awesomeapplets.assignamo.R;
import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.*;

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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class TeacherListActivity extends ListActivity {
	
	private DbAdapter teacherDb;
	
	public TeacherListActivity() {
		
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
		View v = inflater.inflate(R.layout.teacher_list, null);
		//setHasOptionsMenu(true);
		return v;
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.teacher_menu, menu);
	}
	
	public void openDatabase() {
		if (teacherDb == null)
			teacherDb = new DbAdapter(this, Values.DATABASE_NAME, Values.DATABASE_VERSION, Values.BOOK_TABLE, new String[0], Values.KEY_ROWID);
		teacherDb.open();
	}
	
	public void closeDatabase() {
		if (teacherDb != null)
			teacherDb.close();
	}
	
    public void fillData() {
    	Cursor teachersCursor = teacherDb.fetchAll(Values.BOOK_FETCH);
    	startManagingCursor(teachersCursor);
    	
    	// Create and array to specify the fields we want (only the TITLE)
    	String[] from = new String[] {Values.KEY_NAME, Values.TEACHER_KEY_SUBJECT};
    	
    	// and an array of the fields we want to bind in the view
    	int[] to = new int[]{R.id.course_list_name, R.id.course_list_teacher};
    	
    	// Now create a simple cursor adapter and set it to display
    	SimpleCursorAdapter reminders = new SimpleCursorAdapter(this, R.layout.course_list_item, teachersCursor, from, to);
    	setListAdapter(reminders);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, TeacherViewFragment.class);
		i.putExtra(Values.KEY_ROWID, id);
		startActivity(i);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.teacher_menu, menu);
    	return true;
    }
    
    // TODO
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case R.id.teacher_menu_add:
			Intent i = new Intent(getApplication(), TeacherEditActivity.class);
			startActivity(i);
			return true;
		}
    	return true;
    }
    
    // TODO
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	//super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.teacher_context, menu);
    }
    
    public boolean onContextItemSelected(MenuItem item) {
    	
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		
    	switch (item.getItemId()) {
    	case R.id.teacher_context_menu_delete:
    		teacherDb.delete(info.id);
			fillData();
			return true;
    	case R.id.teacher_context_menu_edit:
    		Intent i = new Intent(getApplicationContext(), TeacherEditActivity.class);
    		i.putExtra(Values.KEY_ROWID, info.id);
    		startActivity(i);
		}
    	return true;
    }
}
