package com.awesomeapplets.assignamo;

import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.Values;
import com.awesomeapplets.assignamo.utils.DbUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class AssignmentListFragment extends ListFragment {

	public static DbAdapter assignmentDb;
	private Context context;
	
	// The course we are displaying assignments for. If all, -1.
	short course = -1;
	
	// Needed for recreating the fragment
	public AssignmentListFragment() {}
	
	/**
	 * Create an assignment list that shows all assignments.
	 */
	public AssignmentListFragment(Context context) {
		this.context = context;
	}

	/**
	 * Create an assignment list that lists the assignments in a given course.
	 * 
	 * @param course
	 *            the course to display assignments of
	 */
	public AssignmentListFragment(Context context, short course) {
		this.context = context;
		this.course = course;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putShort(Values.ASSIGNMENT_KEY_COURSE, course);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			course = savedInstanceState.getShort(Values.ASSIGNMENT_KEY_COURSE);
		}
		setRetainInstance(true);
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
		
		// Register refresh Intent listener
		IntentFilter filter = new IntentFilter();
		filter.addAction(Values.INTENT_REFRESH_ACTION);
		
		LocalBroadcastManager.getInstance(context)
		.registerReceiver(new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle extras = intent.getExtras();
				if (extras != null && extras.containsKey(Values.INTENT_REFRESH_COURSE_KEY)) {
					// Message sent to refresh only lists displaying a certain course
					if (course == -1 || intent.getExtras().getShort(Values.INTENT_REFRESH_COURSE_KEY) == course)
						fillData();
				} else // Message sent to refresh all lists
					fillData();	
			}
		}, filter);
	}

	public void onPause() {
		super.onPause();
		closeDatabase();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.assignment_list, container, false);
		setHasOptionsMenu(true);
		return v;
	}

	public void openDatabase() {
		if (assignmentDb == null)
			assignmentDb = new DbAdapter(context,
					Values.DATABASE_NAME, Values.DATABASE_VERSION,
					Values.ASSIGNMENT_TABLE, new String[0], Values.KEY_ROWID);
		assignmentDb.open();
	}

	public void closeDatabase() {
		if (assignmentDb != null)
			assignmentDb.close();
	}
	
	private void broadcastRepaint() {
		Intent i = new Intent();
		i.setAction(Values.INTENT_REFRESH_ACTION);
		i.putExtra(Values.INTENT_REFRESH_COURSE_KEY, course);
		
		LocalBroadcastManager.getInstance(context).sendBroadcast(i);
	}
	
	public void fillData() {
		long startTime = System.currentTimeMillis();
		Cursor assignmentsCursor;
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean showingCompleted = sharedPrefs.getBoolean(Values.ASSIGNMENT_KEY_SHOWING_COMPLETED, false);
		if (course < 0) // Showing assignments from all courses
			if (showingCompleted)
				assignmentsCursor = DbUtils.fetchAllAssignments(context, Values.ASSIGNMENT_LIST_FETCH, null, true);
			else
				assignmentsCursor = DbUtils.fetchIncompleteAssignments(context, Values.ASSIGNMENT_LIST_FETCH, null, true);
		else // Showing assignments from specified course
			if (showingCompleted)
				assignmentsCursor = DbUtils.fetchAllAssignments(context, Values.ASSIGNMENT_LIST_FETCH, course, true);
			else
				assignmentsCursor = DbUtils.fetchIncompleteAssignments(context, Values.ASSIGNMENT_LIST_FETCH, course, true);
		getActivity().startManagingCursor(assignmentsCursor);
		// Create and array to specify the fields we want
		String[] from = new String[] { Values.KEY_TITLE, Values.ASSIGNMENT_KEY_COURSE };

		// and an array of the fields we want to bind in the view
		int[] to = new int[] { R.id.assignment_list_title, R.id.assignment_list_course };

		// Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter reminders = new SimpleCursorAdapter(context,
				R.layout.assignment_list_item, assignmentsCursor, from, to);
		reminders.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				if (columnIndex == 2) {
					String[] courses = DbUtils.getCoursesAsArray(context);
					short value = cursor.getShort(columnIndex);
					TextView textView = (TextView) view;
					textView.setText(courses[value]);
					return true;
				}
				return false;
			}
		});
		setListAdapter(reminders);
		Log.d("time", "It took " + (System.currentTimeMillis() - startTime) + " milliseconds.");
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(context, AssignmentViewFragment.class);
		i.putExtra(Values.KEY_ROWID, id);
		startActivity(i);
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		if (DbUtils.isAssignmentCompleted(context, info.id))
			menu.add(0, 0, 0, R.string.assignment_menu_mark_as_incomplete);
		else
			menu.add(0, 0, 0, R.string.assignment_menu_mark_as_completed);
		menu.add(0, 1, 0, R.string.assignment_edit);
		menu.add(0, 2, 0, R.string.assignment_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch (item.getItemId()) {
		case 0:
			if (DbUtils.isAssignmentCompleted(context, info.id))
				DbUtils.setAssignmentState(context, info.id, false);
			else
				DbUtils.setAssignmentState(context, info.id, true);
			broadcastRepaint();
			return true;
		case 1: // Edit the assignment
			Intent i = new Intent(context, AssignmentEditFragment.class);
			i.putExtra(Values.KEY_ROWID, info.id);
			startActivity(i);
			return true;
		case 2:
			// Delete the assignment
			assignmentDb.delete(info.id);
			broadcastRepaint();
			return true;
		}
		return super.onContextItemSelected(item);
	}
}
