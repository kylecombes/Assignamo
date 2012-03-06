package com.awesomeapplets.assignamo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.awesomeapplets.assignamo.database.DateAdapter;
import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.Values;
import com.awesomeapplets.assignamo.database.DbUtils;
import com.awesomeapplets.assignamo.preferences.ViewFragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class AssignmentViewFragment extends ViewFragment {
	
	private static String DATE_FORMAT = "c, MMMMM dd, yyyy";
	private static String TIME_FORMAT = "hh:mm a";
	
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
		//startManagingCursor(cursor);
		
		// Set course label
		short courseId = (short)cursor.getInt(cursor.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_COURSE));
		String[] courses = DbUtils.getCoursesAsArray(getApplicationContext());
		courseLabel.setText(courses[courseId]);
		
		// Set title label
		titleLabel.setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_TITLE)));
		
		// Set due date label
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean withTime = prefs.getBoolean("pref_appearance_show_time", true);
		dueDateLabel.setText(getDateString(cursor.getLong(cursor.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_DUE_DATE)),
				withTime),
				BufferType.SPANNABLE);
		long points = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_POINTS)));
		if (points > 0)
			pointsLabel.setText(points + " " + getString(R.string.assignment_points));
		else
			pointsLabel.setText(getString(R.string.assignment_no_points));
		descriptionLabel.setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_DESCRIPTION)));
	}
	
	private SpannableString getDateString(long time, boolean withTime) {
		
		final float RELATIVE_SIZE = 1.0f;
		
		Calendar calendar = Calendar.getInstance();
		//if (rowId != null)
		calendar.setTimeInMillis(DateAdapter.convertMinutesToMills(time));
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
		
		SpannableString returnString;
		
		// Either way, we need to have the date
		String dueString = "";// getString(R.string.due) + " ";
		String dateString = dateFormat.format(calendar.getTime());
		
		if (!withTime) {
			// We only want the date to be returned (no time)
			returnString = new SpannableString( dueString + dateString );
		} else {
			// If we are going to show the time, get it here
			String atString = " " + getString(R.string.at) + " ";
			String timeString = timeFormat.format(calendar.getTime());
			
			// We want the time as well as the date to be returned
			returnString = new SpannableString( dueString
					+ dateString + atString + timeString);
			
			returnString.setSpan(new StyleSpan(Typeface.BOLD),
					dueString.length() + dateString.length() + atString.length(),
					dueString.length() + dateString.length() + atString.length() + timeString.length(),
					0);
			returnString.setSpan(new RelativeSizeSpan(RELATIVE_SIZE),
					dueString.length() + dateString.length() + atString.length(),
					dueString.length() + dateString.length() + atString.length() + timeString.length(),
					0);
		}
		// This could really be up right after defining returnString, but
			// then I would have to put it in twice, as it is needed in
			// both cases. So instead I put in in here, seemingly out
			// of order.
		returnString.setSpan(new StyleSpan(Typeface.BOLD),
				dueString.length(),
				dueString.length() + dateString.length(),
				0);
		returnString.setSpan(new RelativeSizeSpan(RELATIVE_SIZE),
				dueString.length(),
				dueString.length() + dateString.length(),
				0);
		return returnString;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.view_edit:
			Intent i = new Intent(getApplicationContext(), AssignmentEditFragment.class);
			i.putExtra(Values.KEY_ROWID, rowId);
			startActivity(i);
			break;
		case R.id.view_delete:
			dbAdapter.delete(rowId);
			finish();
		}
		return true;
	}
}
