package com.awesomeapplets.assignamo;

import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.DbUtils;
import com.awesomeapplets.assignamo.database.Values;
import com.awesomeapplets.assignamo.preferences.Preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
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
	
	// The course we are displaying assignments for. If all, -1.
	short course = -1;
	
	
	/**
	 * Create an assignment list that shows all assignments.
	 */
	public AssignmentListFragment() {
		
	}

	/**
	 * Create an assignment list that lists the assignments in a given course.
	 * 
	 * @param course
	 *            the course to display assignments of
	 */
	public AssignmentListFragment(short course) {
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

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.assignment_list, container, false);
		setHasOptionsMenu(true);
		return v;
	}

	public void openDatabase() {
		if (assignmentDb == null)
			assignmentDb = new DbAdapter(getActivity(),
					Values.DATABASE_NAME, Values.DATABASE_VERSION,
					Values.ASSIGNMENT_TABLE, new String[0], Values.KEY_ROWID);
		assignmentDb.open();
	}

	public void closeDatabase() {
		if (assignmentDb != null)
			assignmentDb.close();
	}

	public void fillData() {
		Cursor assignmentsCursor;
		boolean showingCompleted = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Values.ASSIGNMENT_KEY_SHOWING_COMPLETED, false);
		
		if (course < 0) // Showing assignments from all courses
			if (showingCompleted)
				assignmentsCursor = DbUtils.fetchAllAssignments(getActivity(), Values.ASSIGNMENT_LIST_FETCH, null, true);
			else
				assignmentsCursor = DbUtils.fetchIncompleteAssignments(getActivity(), Values.ASSIGNMENT_LIST_FETCH, null, true);
		else // Showing assignments from specified course
			if (showingCompleted)
				assignmentsCursor = DbUtils.fetchAllAssignments(getActivity(), Values.ASSIGNMENT_LIST_FETCH, course, true);
			else
				assignmentsCursor = DbUtils.fetchIncompleteAssignments(getActivity(), Values.ASSIGNMENT_LIST_FETCH, course, true);
		getActivity().startManagingCursor(assignmentsCursor);

		// Create and array to specify the fields we want
		String[] from = new String[] { Values.KEY_TITLE, Values.ASSIGNMENT_KEY_COURSE };

		// and an array of the fields we want to bind in the view
		int[] to = new int[] { R.id.assignment_list_title, R.id.assignment_list_course };

		// Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter reminders = new SimpleCursorAdapter(getActivity(),
				R.layout.assignment_list_item, assignmentsCursor, from, to);
		reminders.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				if (columnIndex == 2) {
					String[] courses = DbUtils.getCoursesAsArray(getActivity());
					short value = cursor.getShort(columnIndex);
					TextView textView = (TextView) view;
					textView.setText(courses[value]);
					return true;
				}
				return false;
			}
		});
		setListAdapter(reminders);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(getActivity(), AssignmentViewFragment.class);
		i.putExtra(Values.KEY_ROWID, id);
		startActivity(i);
	}

	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, 0, 0, R.string.assignment_add).setIcon(android.R.drawable.ic_menu_add);
		//menu.add(0, 1, 0, R.string.assignment_add_book).setIcon(android.R.drawable.ic_menu_add);
		if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Values.ASSIGNMENT_KEY_SHOWING_COMPLETED, false))
			menu.add(0, 2, 0, R.string.show_all_assignments).setIcon(android.R.drawable.button_onoff_indicator_on);
		else
			menu.add(0, 2, 0, R.string.show_all_assignments).setIcon(android.R.drawable.button_onoff_indicator_off);
		menu.add(0, 3, 0, R.string.preferences).setIcon(android.R.drawable.ic_menu_preferences);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			Intent iA = new Intent(getActivity(), AssignmentEditFragment.class);
			startActivity(iA);
			return true;
		case 2:
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			Editor prefEditor = prefs.edit();
			if (prefs.getBoolean(Values.ASSIGNMENT_KEY_SHOWING_COMPLETED, false))
				prefEditor.putBoolean(Values.ASSIGNMENT_KEY_SHOWING_COMPLETED, false);
			else
				prefEditor.putBoolean(Values.ASSIGNMENT_KEY_SHOWING_COMPLETED, true);
			prefEditor.commit();
			fillData();
			return true;
		case 3:
			Intent iP = new Intent(getActivity(), Preferences.class);
			startActivity(iP);
			return true;
		}
		return true;
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		if (DbUtils.isAssignmentCompleted(getActivity(), info.id))
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
			if (DbUtils.isAssignmentCompleted(getActivity(), info.id))
				DbUtils.setAssignmentState(getActivity(), info.id, false);
			else
				DbUtils.setAssignmentState(getActivity(), info.id, true);
			fillData();
			return true;
		case 1: // Edit the assignment
			Intent i = new Intent(getActivity(), AssignmentEditFragment.class);
			i.putExtra(Values.KEY_ROWID, info.id);
			startActivity(i);
			return true;
		case 2:
			// Delete the assignment
			assignmentDb.delete(info.id);
			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}
}
