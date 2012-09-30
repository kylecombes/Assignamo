package com.acedit.assignamo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.ui.ColorStrip;
import com.acedit.assignamo.utils.DateUtils;
import com.acedit.assignamo.utils.DbUtils;

public class AssignmentListFragment extends ListFragment {
	
	CustomCursorAdapter adapter;
	Cursor assignmentsCursor;
	private Context context;
	
	private final String DATE_FORMAT = "c, MMMMM dd";
	
	// The course we are displaying assignments for. If all, -1.
	short course = -1;
	
	// Needed for recreating the fragment
	public AssignmentListFragment() {
		Bundle args = getArguments();
		if (args != null)
			course = args.getShort("courseId");
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
		
		if (context == null)
			context = getActivity();
		
		// Need to give assignmentsCursor a value -- null will make it not work
		updateAdapter();
		
		adapter = new CustomCursorAdapter(context, assignmentsCursor, 0);
		setListAdapter(adapter);
	}

	/** Called when the activity becomes visible. */
	public void onStart() {
		super.onStart();
		registerForContextMenu(getListView());
	}

	public void onResume() {
		super.onResume();
		
		if (context == null)
			context = getActivity();
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
				assignmentsCursor = fetchAllAssignments(null);
			else
				assignmentsCursor = fetchIncompleteAssignments(null);
		else // Showing assignments from specified course
			if (showingCompleted)
				assignmentsCursor = fetchAllAssignments(course);
			else
				assignmentsCursor = fetchIncompleteAssignments(course);
		
		// adapter is null when updateAdapter() is called in onCreate()
		if (adapter != null)
			adapter.changeCursor(assignmentsCursor);
	}

	private void refresh() {
		updateAdapter();
		adapter.notifyDataSetChanged();
	}
	
	private void broadcastRefresh() {
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
		long rowId = ((AdapterContextMenuInfo) menuInfo).id;
		if (DbUtils.isAssignmentCompleted(context, rowId))
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
			broadcastRefresh();
			return true;
		case 1: // Edit the assignment
			Intent i = new Intent(context, AssignmentEditFragment.class);
			i.putExtra(Values.KEY_ROWID, info.id);
			startActivity(i);
			return true;
		case 2:
			// Delete the assignment
			DbUtils.deleteAssignment(context, info.id);
			broadcastRefresh();
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	//TODO Finish implementing prompt when deleting assignment
	/*---------- Assignment-Delete Prompt ----------*
	
	public static class DeleteDialogFragment extends DialogFragment {
		
		static DeleteDialogFragment newInstance(int arg) {
			return new DeleteDialogFragment();
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.assignment_confirm_delete_title)
				.setMessage(R.string.assignment_confirm_delete_message)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						((AssignmentListFragment)getTargetFragment()).doPositiveClick();
					}
				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				})
				.create();
		}
	}
	
	public void doPositiveClick() {
		// Delete the assignment
		DbUtils.deleteAssignment(context, info.id);
		broadcastRefresh();
	}
	
	/*---------- Fetching Assignments from the Database ----------*/
	
	public Cursor fetchAllAssignments(Short course) {
		Cursor c;
		DbAdapter adapter = new DbAdapter(context, Values.DATABASE_NAME, Values.DATABASE_VERSION,
				Values.ASSIGNMENT_TABLE, Values.DATABASE_CREATE, Values.KEY_ROWID);
		adapter.open();
		
		if (course == null)
			c = adapter.fetchAllOrdered(Values.ASSIGNMENT_LIST_FETCH, null, Values.ASSIGNMENT_KEY_DUE_DATE);
		else
			c = adapter.fetchAllOrdered(Values.ASSIGNMENT_LIST_FETCH, Values.ASSIGNMENT_KEY_COURSE + "=" + course, Values.ASSIGNMENT_KEY_DUE_DATE);
		
		adapter.close();
		if (c != null)
			c.moveToFirst();
		return c;
	}
	
	public Cursor fetchIncompleteAssignments(Short course) {
		DbAdapter adapter = new DbAdapter(context,
				Values.DATABASE_NAME,
				Values.DATABASE_VERSION,
				Values.ASSIGNMENT_TABLE,
				Values.DATABASE_CREATE,
				Values.KEY_ROWID);
		adapter.open();
		
		Cursor r;
		if (course == null) // Fetching from all courses
			r = adapter.fetchAllWhere(Values.ASSIGNMENT_LIST_FETCH, Values.ASSIGNMENT_KEY_STATUS + "=" + 0, Values.ASSIGNMENT_KEY_DUE_DATE);
		else
			r = adapter.fetchAllWhere(Values.ASSIGNMENT_LIST_FETCH, Values.ASSIGNMENT_KEY_COURSE + "=" + course
				+ " AND " + Values.ASSIGNMENT_KEY_STATUS + "=" + 0, Values.ASSIGNMENT_KEY_DUE_DATE);
		adapter.close();
		return r;
	}
	
	/*---------- ListView Adapter and Related Subroutines ----------*/
	
	private class CustomCursorAdapter extends CursorAdapter {
		
		LayoutInflater mInflater;
		ViewHolder holder;
		int[] stripColors;
		int[] stripColorsLight;
		
		public CustomCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			this.mContext = context;
			
			// Load the strip colors
			//TODO Thread this?
			Resources res = getResources();
			TypedArray colors = res.obtainTypedArray(R.array.strip_colors);
			stripColors = new int[colors.length()];
			for (short i = 0; i < colors.length(); i++)
				stripColors[i] = colors.getColor(i, res.getColor(R.color.default_strip));
			colors = res.obtainTypedArray(R.array.strip_colors_light);
			stripColorsLight = new int[colors.length()];
			for (short i = 0; i < colors.length(); i++)
				stripColorsLight[i] = colors.getColor(i, res.getColor(R.color.default_strip));
			
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@SuppressWarnings("static-access")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.assignment_list_item, parent, false);
				holder = new ViewHolder();
				holder.colorStrip = (ColorStrip) convertView.findViewById(R.id.assignment_list_color_strip);
				holder.titleLabel = (TextView) convertView.findViewById(R.id.list_title);
				holder.descriptionLabel = (TextView) convertView.findViewById(R.id.list_description);
				holder.dueLabel = (TextView) convertView.findViewById(R.id.list_due);
				
				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();
			
			assignmentsCursor.moveToPosition(position);
			String title = assignmentsCursor.getString(1);
			short course = assignmentsCursor.getShort(2);
			String desc = assignmentsCursor.getString(3);
			long due = assignmentsCursor.getLong(4);
			
			holder.titleLabel.setText(title);
			//holder.colorStrip.setColor(stripColors[course]);
			//convertView.setBackgroundColor(stripColorsLight[course]);
			String dueString = getDateString(due);
			holder.dueLabel.setText(dueString);
			holder.descriptionLabel.setText(desc);
			
			return convertView;
		}

		@Override
		public void bindView(View arg0, Context arg1, Cursor arg2) {}

		@Override
		public View newView(Context arg0, Cursor arg1, ViewGroup arg2) { return null; }
		
	}
	
	private String getDateString(long minutes) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(DateUtils.convertMinutesToMills(minutes));
		
		return (new SimpleDateFormat(DATE_FORMAT)).format(calendar.getTime());
	}

	private static class ViewHolder {
		private static ColorStrip colorStrip;
		private static TextView titleLabel;
		private static TextView descriptionLabel;
		private static TextView dueLabel;
	}
	
}
