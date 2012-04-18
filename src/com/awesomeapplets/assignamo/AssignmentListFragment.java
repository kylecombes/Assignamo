package com.awesomeapplets.assignamo;

import java.util.Calendar;

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

import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.Values;
import com.awesomeapplets.assignamo.utils.DateUtils;
import com.awesomeapplets.assignamo.utils.DbUtils;

public class AssignmentListFragment extends ListFragment {
	
	SimpleCursorAdapter adapter;
	Cursor assignmentsCursor;
	private Context context;
	
	private final String DATE_FORMAT = "c, MMMMM dd";
	
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
	 * @param course the course to display assignments for
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
		
		// Create and array to specify the fields we want
		String[] from = new String[] { Values.KEY_TITLE, Values.ASSIGNMENT_KEY_COURSE, Values.KEY_DESCRIPTION, Values.ASSIGNMENT_KEY_DUE_DATE };

		// and an array of the fields we want to bind in the view
		int[] to = new int[] { R.id.assignment_list_title, R.id.assignment_list_course, R.id.assignment_list_description, R.id.assignment_list_due };

		// Need to give assignmentsCursor a value -- null will make it not work
		updateAdapter();
		
		adapter = new SimpleCursorAdapter(context, R.layout.assignment_list_item, assignmentsCursor, from, to);
		adapter.setViewBinder(new CustomViewBinder());
		setListAdapter(adapter);
	}

	/** Called when the activity becomes visible. */
	public void onStart() {
		super.onStart();
		registerForContextMenu(getListView());
	}

	public void onResume() {
		super.onResume();
		updateAdapter();
		refresh();
		
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
						refresh();
				} else // Message sent to refresh all lists
					refresh();
			}
		}, filter);
	}

	public void onPause() {
		super.onPause();
		if (assignmentsCursor != null) {
			assignmentsCursor.close();
			assignmentsCursor = null;
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.assignment_list, container, false);
		setHasOptionsMenu(true);
		
		return v;
	}

	/**
	 * Updates the content in the adapter.
	 */
	public void updateAdapter() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean showingCompleted = sharedPrefs.getBoolean(Values.ASSIGNMENT_KEY_SHOWING_COMPLETED, false);

		if (course < 0) // Showing assignments from all courses
			if (showingCompleted)
				assignmentsCursor = fetchAllAssignments(null, true);
			else
				assignmentsCursor = fetchIncompleteAssignments(null, true);
		else // Showing assignments from specified course
			if (showingCompleted)
				assignmentsCursor = fetchAllAssignments(course, true);
			else
				assignmentsCursor = fetchIncompleteAssignments(course, true);
		
		// adapter is null when updateAdapter() is called in onCreate()
		if (adapter != null)
			adapter.changeCursor(assignmentsCursor);
	}

	private void refresh() {
		updateAdapter();
		adapter.notifyDataSetChanged();
	}
	
	private void broadcastRepaint() {
		Intent i = new Intent();
		i.setAction(Values.INTENT_REFRESH_ACTION);
		i.putExtra(Values.INTENT_REFRESH_COURSE_KEY, course);
		
		LocalBroadcastManager.getInstance(context).sendBroadcast(i);
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
			DbUtils.deleteAssignment(context, info.id);
			broadcastRepaint();
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	/**
	 * Get all assignments.
	 * @param query the SQL query syntax.
	 * @param order whether or not to sort by due date.
	 * @return Contains all the items in the table.
	 */
	public Cursor fetchAllAssignments(Short course, boolean order) {
		Cursor c;
		DbAdapter adapter = new DbAdapter(context, Values.DATABASE_NAME, Values.DATABASE_VERSION,
				Values.ASSIGNMENT_TABLE, Values.DATABASE_CREATE, Values.KEY_ROWID);
		adapter.open();
		
		if (order)
			if (course == null)
				c = adapter.fetchAll(Values.ASSIGNMENT_LIST_FETCH);
			else
				c = adapter.fetchAllWhere(Values.ASSIGNMENT_LIST_FETCH, Values.ASSIGNMENT_KEY_COURSE + "=" + course);
		else
			if (course == null)
				c = adapter.fetchAllOrdered(Values.ASSIGNMENT_LIST_FETCH, null, Values.ASSIGNMENT_KEY_DUE_DATE);
			else
				c = adapter.fetchAllOrdered(Values.ASSIGNMENT_LIST_FETCH, Values.ASSIGNMENT_KEY_COURSE + "=" + course, Values.ASSIGNMENT_KEY_DUE_DATE);
		
		adapter.close();
		if (c != null)
			c.moveToFirst();
		return c;
	}
	
	/**
	 * Get all the incomplete assignments for a certain course.
	 * @param context a copy of the application context.
	 * @param query the SQL query syntax.
	 * @param course the course to fetch the assignments from. Pass null
	 * to return assignments from all courses.
	 * @param order whether or not to sort the result by due date.
	 * @return all the incomplete assignments for the specified course.
	 */
	public Cursor fetchIncompleteAssignments(Short course, boolean order) {
		DbAdapter adapter = new DbAdapter(context,
				Values.DATABASE_NAME,
				Values.DATABASE_VERSION,
				Values.ASSIGNMENT_TABLE,
				Values.DATABASE_CREATE,
				Values.KEY_ROWID);
		adapter.open();
		
		Cursor r;
		if (course != null) // Fetching from all courses
			r = adapter.fetchAllWhere(Values.ASSIGNMENT_LIST_FETCH, Values.ASSIGNMENT_KEY_COURSE + "=" + course
				+ " AND " + Values.ASSIGNMENT_KEY_STATUS + "=" + 0);
		else
			r = adapter.fetchAllWhere(Values.ASSIGNMENT_LIST_FETCH, Values.ASSIGNMENT_KEY_STATUS + "=" + 0);
		adapter.close();
		return r;
	}
	
	private class CustomViewBinder implements ViewBinder {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			switch (columnIndex) {
			case 2: // Course label
				String[] courses = DbUtils.getCoursesAsArray(context);
				short value = cursor.getShort(columnIndex);
				TextView textView = (TextView) view;
				textView.setText(courses[value]);
				return true;
			/*case 3: // Description label
				String desc = assignmentsCursor.getString(columnIndex);
				String */
			case 4: // Due date label
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(DateUtils.convertMinutesToMills(cursor.getLong(columnIndex)));
				
				String str = DateUtils.formatAsString(cal, DATE_FORMAT);
				TextView txt = (TextView)view;
				txt.setText(str);
				return true;
			default:
				return false;
			}
		}
	}

	/*
	private class CustomAdapter extends SimpleCursorAdapter {
		
		LayoutInflater mInflater;
		
		public CustomAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout, c, from, to);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.assignment_list_item, parent, false);
				holder = new ViewHolder();
				holder.titleLabel = (TextView) convertView.findViewById(R.id.assignment_list_title);
				holder.descriptionLabel = (TextView) convertView.findViewById(R.id.assignment_list_description);
				holder.courseLabel = (TextView) convertView.findViewById(R.id.assignment_list_course);
				holder.dueLabel = (TextView) convertView.findViewById(R.id.assignment_list_course);
				
				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();
			
			
			holder.titleLabel.setText(assignmentsCursor.getString(position));
			holder.descriptionLabel.setText(text);
			holder.courseLabel.setText(text);
			holder.dueLabel.setText(text);
			
			return convertView;
		}
		
	}

	private static class ViewHolder {
		private static TextView titleLabel;
		private static TextView courseLabel;
		private static TextView descriptionLabel;
		private static TextView dueLabel;
	}
*/	
}
