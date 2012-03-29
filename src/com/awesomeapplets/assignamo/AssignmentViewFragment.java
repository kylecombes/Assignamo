package com.awesomeapplets.assignamo;

import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.Values;
import com.awesomeapplets.assignamo.preferences.ViewFragment;
import com.awesomeapplets.assignamo.utils.DbUtils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class AssignmentViewFragment extends ViewFragment {
	
	private TextView courseLabel;
	private TextView titleLabel;
	private TextView dueDateLabel;
	private TextView pointsLabel;
	private TextView descriptionLabel;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new DbAdapter(getBaseContext(), Values.DATABASE_NAME, Values.DATABASE_VERSION,
				Values.ASSIGNMENT_TABLE, Values.DATABASE_CREATE, Values.KEY_ROWID);
		
		setContentView(R.layout.assignment_view_phone);
		courseLabel = (TextView)findViewById(R.id.assignment_view_course);
		titleLabel = (TextView)findViewById(R.id.assignment_view_title);
		dueDateLabel = (TextView)findViewById(R.id.assignment_view_date);
		pointsLabel = (TextView)findViewById(R.id.assignment_view_points);
		descriptionLabel = (TextView)findViewById(R.id.assignment_view_description);
		
		if (savedInstanceState != null)
			rowId = savedInstanceState.getLong(Values.KEY_ROWID);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		dbAdapter.close();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		dbAdapter.open();
		setRowIdFromIntent();
		populateFields();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(Values.KEY_ROWID, rowId);
	}
	
	
	protected void populateFields() {
		dbAdapter.open();
		Cursor cursor = dbAdapter.fetch(rowId, Values.ASSIGNMENT_FETCH);
		dbAdapter.close();
		startManagingCursor(cursor);
		
		// Set course label
		short courseId = (short)cursor.getInt(cursor.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_COURSE));
		String[] courses = DbUtils.getCoursesAsArray(getApplicationContext());
		courseLabel.setText(courses[courseId]);
		
		// Set title label
		titleLabel.setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_TITLE)));
		
		// Set due date label
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean withTime = prefs.getBoolean("pref_appearance_show_time", true);
		dueDateLabel.setText(getDateString(	cursor.getLong(cursor.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_DUE_DATE)),
				withTime),
				BufferType.SPANNABLE);
		
		// Set points label
		long points = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_POINTS)));
		if (points > 0)
			if (points == 1)
				pointsLabel.setText(points + " " + getString(R.string.assignment_point));
			else
				pointsLabel.setText(points + " " + getString(R.string.assignment_points));
		else
			pointsLabel.setText(getItalicizedString(R.string.assignment_no_points));
		
		// Set description label
		descriptionLabel.setText(getDescription(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_DESCRIPTION))));
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (DbUtils.isAssignmentCompleted(getApplicationContext(), rowId))
			menu.add(0, 0, 0, R.string.assignment_menu_mark_as_incomplete)
			.setIcon(R.drawable.checkmark);
		else
			menu.add(0, 0, 0, R.string.assignment_menu_mark_as_completed)
			.setIcon(R.drawable.checkmark);
		menu.add(0, 1, 0, R.string.edit).setIcon(android.R.drawable.ic_menu_edit);
		menu.add(0, 2, 0, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			if (DbUtils.isAssignmentCompleted(getApplicationContext(), rowId))
				DbUtils.setAssignmentState(getApplicationContext(), rowId, false);
			else
				DbUtils.setAssignmentState(getApplicationContext(), rowId, true);
			break;
		case 1:
			Intent i = new Intent(getApplicationContext(), AssignmentEditFragment.class);
			i.putExtra(Values.KEY_ROWID, rowId);
			startActivity(i);
			break;
		case 2:
			deleteAssignment(rowId);
			finish();
		}
		return true;
	}
	
	private boolean deleteAssignment(long rowId) {
		dbAdapter.open();
		boolean b = dbAdapter.delete(rowId);
		dbAdapter.close();
		return b;
	}
}
