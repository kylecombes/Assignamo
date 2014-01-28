package com.acedit.assignamo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
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
import com.acedit.assignamo.objects.Assignment;
import com.acedit.assignamo.objects.Course;
import com.acedit.assignamo.ui.ColorStrip;
import com.acedit.assignamo.utils.DbUtils;
import com.acedit.assignamo.utils.UiUtils;

public class AssignmentListFragment extends ListFragment {
	
	CustomCursorAdapter cursorAdapter;
	DbAdapter dbAdapter;
	Cursor assignmentsCursor;
	private Context context;
	private long selectedItem;
	private boolean activityJustCreated;
	
	private static final String DATE_FORMAT = "c, MMM dd";
	
	// The course we are displaying assignments for. If all, -1.
	short course = -1;
	
	
	public AssignmentListFragment() {}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null)
			course = args.getShort("courseId");
		
		if (savedInstanceState != null) {
			course = savedInstanceState.getShort(Assignment.KEY_COURSE);
		}
		setRetainInstance(true);
		
		if (context == null) context = getActivity();
		
		// Need to give assignmentsCursor a value -- null will make it not work
		updateAdapter();
		
		cursorAdapter = new CustomCursorAdapter(context, assignmentsCursor, 0);
		setListAdapter(cursorAdapter);
		activityJustCreated = true; // Use to prevent calling updateAdapter() twice on first load
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.assignment_list, container, false);
	}

	/** Called when the activity becomes visible. */
	public void onStart() {
		super.onStart();
		registerForContextMenu(getListView());
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putShort(Assignment.KEY_COURSE, course);
	}
	
	public void onResume() {
		super.onResume();
		
		if (context == null) context = getActivity();
		updateCourseColors();
		if (activityJustCreated)
			activityJustCreated = false;
		else
			refresh();
		
		registerIntentListener();
	}
	
	public void registerIntentListener() {
		
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

	/**
	 * Updates the content in the adapter.
	 */
	public void updateAdapter() {
		new LoadDataTask().execute((Void)null);
	}
	

	private class LoadDataTask extends AsyncTask<Void, Void, Cursor> {

		@Override
		protected Cursor doInBackground(Void... params) {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
			boolean showingCompleted = sharedPrefs.getBoolean(Assignment.KEY_SHOWING_COMPLETED, false);
			
			if (course < 0) // Showing assignments from all courses
				if (showingCompleted)
					return DbUtils.fetchAllAssignments(context, null);
				else
					return DbUtils.fetchIncompleteAssignments(context, null);
			else // Showing assignments from specified course
				if (showingCompleted)
					return DbUtils.fetchAllAssignments(context, course);
				else
					return DbUtils.fetchIncompleteAssignments(context, course);
		}
		
		@Override
		protected void onPostExecute(Cursor result) {
			assignmentsCursor = result;
			// adapter is null when updateAdapter() is called in onCreate()
			if (cursorAdapter != null)
				cursorAdapter.changeCursor(assignmentsCursor);
		}
		
	}

	private void refresh() {
		updateAdapter();
		cursorAdapter.notifyDataSetChanged();
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
		startActivity( new Intent(context, AssignmentViewFragment.class).putExtra(Values.KEY_ROWID, id) );
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
		long id = ((AdapterContextMenuInfo)item.getMenuInfo()).id;
		
		switch (item.getItemId()) {
		case 0:
			if (DbUtils.isAssignmentCompleted(context, id))
				DbUtils.setAssignmentCompletionStatus(context, id, false);
			else
				DbUtils.setAssignmentCompletionStatus(context, id, true);
			broadcastRefresh();
			return true;
		case 1: // Edit the assignment
			startActivity( new Intent(context, AssignmentEditFragment.class).putExtra(Values.KEY_ROWID, id));
			return true;
		case 2:
			// Delete the assignment
			selectedItem = id;
			DeleteDialogFragment frag = new DeleteDialogFragment();
			frag.setTargetFragment(this, 0);
			frag.show(getFragmentManager(), "confirmDelete");
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	/*---------- Assignment Delete Prompt ----------*/
	
	public static class DeleteDialogFragment extends DialogFragment {
		
		static DeleteDialogFragment newInstance(int arg) {
			return new DeleteDialogFragment();
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
				.setIcon(R.drawable.ic_action_warning)
				.setTitle(R.string.confirm_delete)
				.setMessage(R.string.assignment_confirm_delete_message)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((AssignmentListFragment)getTargetFragment()).deleteAssignment();
					}
				})
				.setNegativeButton(R.string.no, null)
				.create();
		}
	}
	
	public void deleteAssignment() {
		// Delete the assignment
		DbUtils.deleteItem(context, Assignment.TABLE_NAME, selectedItem);
		broadcastRefresh();
	}
	
	
	/*---------- ListView Adapter and Related Subroutines ----------*/
	
	private Map<Short,Integer> colors = new HashMap<Short,Integer>();
	private Map<Short,Integer> colorsLight = new HashMap<Short,Integer>();
	
	private class CustomCursorAdapter extends CursorAdapter {
		
		LayoutInflater mInflater;
		private final int mOverdueColor = getResources().getColor(R.color.assignment_list_overdue);
		
		public CustomCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			this.mContext = context;
			
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		//@SuppressWarnings("static-access")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			//ViewHolder holder;
			
			//if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item, parent, false);
				//holder = new ViewHolder();
				ColorStrip colorStrip = (ColorStrip) convertView.findViewById(R.id.list_item_color_strip);
				TextView titleLabel = (TextView) convertView.findViewById(R.id.list_item_title);
				TextView descriptionLabel = (TextView) convertView.findViewById(R.id.list_item_bottomLeft);
				TextView dueLabel = (TextView) convertView.findViewById(R.id.list_item_bottomRight);
				
				//convertView.setTag(holder);
			//} else
				//holder = (ViewHolder) convertView.getTag();
			
			assignmentsCursor.moveToPosition(position);
			String title = assignmentsCursor.getString(1);
			short course = assignmentsCursor.getShort(2);
			String desc = assignmentsCursor.getString(3);
			long due = assignmentsCursor.getLong(4);
			
			titleLabel.setText(/*"Pos: " + position + " | " + */title);
			colorStrip.setColor(colors.get(course));
			convertView.setBackgroundColor(colorsLight.get(course));
			
			dueLabel.setText(getDateString(due));
			// Make the text red if it's overdue
			if (due < System.currentTimeMillis() / 60000)
				dueLabel.setTextColor(mOverdueColor);
			
			descriptionLabel.setText(desc);
			
			return convertView;
		}

		@Override
		public void bindView(View arg0, Context arg1, Cursor arg2) {}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) { return null; }
		
		@Override
		public boolean hasStableIds() {	return true; }
		
		//TODO Might not need
		/*@Override
		public int getViewTypeCount() { return 2; }*/
	}
	
	private String getDateString(long minutes) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(minutes * 60000);
		
		return (new SimpleDateFormat(DATE_FORMAT)).format(calendar.getTime());
	}
	
	public final static short ALPHA = 30;

	private void updateCourseColors() {
		DbAdapter adapter = new DbAdapter(context, null, Course.TABLE_NAME);
		adapter.open();
		Cursor c = adapter.fetchAll(new String[] { Values.KEY_ROWID, Course.KEY_COLOR } );
		c.moveToFirst();
		for (short i = 0; i < c.getCount(); i++) {
			short id = c.getShort(c.getColumnIndexOrThrow(Values.KEY_ROWID));
			int color = c.getInt(c.getColumnIndexOrThrow(Course.KEY_COLOR));
			colors.put(id, color);
			colorsLight.put(id,UiUtils.changeAlpha(color, ALPHA));
			c.moveToNext();
		}
		c.close();
	}
	/*
	private static class ViewHolder {
		private static ColorStrip colorStrip;
		private static TextView titleLabel;
		private static TextView descriptionLabel;
		private static TextView dueLabel;
		
		public ViewHolder() {}
	}
	*/
}
