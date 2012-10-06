package com.acedit.assignamo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.utils.DbUtils;

public class AssignmentViewFragment extends ViewFragment {
	
	private TextView courseLabel;
	private TextView titleLabel;
	private TextView dueDateLabel;
	private TextView pointsLabel;
	private TextView descriptionLabel;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.assignment_view);
		courseLabel = (TextView)findViewById(R.id.assignment_view_course);
		titleLabel = (TextView)findViewById(R.id.assignment_view_title);
		dueDateLabel = (TextView)findViewById(R.id.assignment_view_date);
		pointsLabel = (TextView)findViewById(R.id.assignment_view_points);
		descriptionLabel = (TextView)findViewById(R.id.assignment_view_description);
		
		if (savedInstanceState != null)
			rowId = savedInstanceState.getLong(Values.KEY_ROWID);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		populateFields();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(Values.KEY_ROWID, rowId);
	}
	
	
	protected void populateFields() {
		
		// Set course label
		String courseText = DbUtils.getCourseNameFromId(context,
				cursor.getShort(cursor.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_COURSE)));
		courseLabel.setText(courseText);
		
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
			DbUtils.deleteAssignment(context,rowId);
			finish();
		}
		return true;
	}
	
	@Override
	protected void reloadData() {
		DbAdapter dbAdapter = new DbAdapter(getBaseContext(), null, Values.ASSIGNMENT_TABLE);
		dbAdapter.open();
		cursor = dbAdapter.fetch(rowId, Values.ASSIGNMENT_FETCH);
		dbAdapter.close();
		
	}
}
