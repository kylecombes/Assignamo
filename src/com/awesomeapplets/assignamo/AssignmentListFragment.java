package com.awesomeapplets.assignamo;

import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.DbUtils;
import com.awesomeapplets.assignamo.database.Values;
import com.awesomeapplets.assignamo.preferences.Preferences;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class AssignmentListFragment extends ListFragment {

	public static DbAdapter assignmentDb;
	
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
		if (savedInstanceState != null)
			course = savedInstanceState.getShort(Values.ASSIGNMENT_KEY_COURSE);
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
					MainActivity.DATABASE_NAME, MainActivity.DATABASE_VERSION,
					Values.ASSIGNMENT_TABLE, new String[0], MainActivity.KEY_ROWID);
		assignmentDb.open();
	}

	public void closeDatabase() {
		if (assignmentDb != null)
			assignmentDb.close();
	}

	public void fillData() {
		Cursor assignmentsCursor;
		if (course < 0)
			assignmentsCursor = assignmentDb.fetchAll(Values.ASSIGNMENT_FETCH);
		else
			// Fetch all the assignments for the set course
			assignmentsCursor = assignmentDb.fetchAllWhere(Values.ASSIGNMENT_FETCH, Values.ASSIGNMENT_KEY_COURSE + "=" + course);
		getActivity().startManagingCursor(assignmentsCursor);

		// Create and array to specify the fields we want
		String[] from = new String[] { Values.KEY_TITLE, Values.ASSIGNMENT_KEY_COURSE };

		// and an array of the fields we want to bind in the view
		int[] to = new int[] { R.id.assignment_list_title,
				R.id.assignment_list_course };

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
		i.putExtra(MainActivity.KEY_ROWID, id);
		startActivity(i);
		// startActivityForResult(i, ACTIVITY_EDIT);
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.assignment_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.assignment_menu_add:
			// addAssignment("Test", "My class", "A test assignment",
			// "Some day", -1);
			Intent iA = new Intent(getActivity(), AssignmentEditFragment.class);
			startActivity(iA);
			return true;
		case R.id.menu_settings:
			Intent iP = new Intent(getActivity(), Preferences.class);
			startActivity(iP);
			return true;
		}
		return true;
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = getActivity().getMenuInflater();
		mi.inflate(R.menu.assignment_longpress, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.assignment_context_menu_delete:
			// Delete the task
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			assignmentDb.delete(info.id);
			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}
}
