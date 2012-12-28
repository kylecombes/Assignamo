package com.acedit.assignamo;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.utils.DateUtils;
import com.acedit.assignamo.utils.DbUtils;

public class AssignmentEditFragment extends FragmentActivity {
	
	private Calendar calendar;
	static final String DATE_FORMAT = "E, MM/dd/yyyy";
	static final String TIME_FORMAT = "hh:mm a";
	private Long rowId;
	private boolean userSetDateTime;
	private Cursor courseCursor;
	// If we just restored the state, we don't need to reload the next class time
	private boolean justRestoredState;
	
	private Spinner courseSpinner;
	private TextView titleField;
	private Button dueDateButton;
	private Button timeDueButton;
	private TextView descriptionField;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//TODO Needs something to check if a course exists
		
		setContentView(R.layout.assignment_edit);
		// Set the title of the Activity
		if (rowId == null)
			setTitle(R.string.assignment_add);
		else
			setTitle(R.string.assignment_edit);
		
		mapViews();
		populateSpinner();
		loadDataFromIntent();
		populateFields();
		
		updateButtons(0);
	}
	
	private void mapViews() {
		courseSpinner = (Spinner)findViewById(R.id.assignment_add_course_select);
		
		titleField = (TextView)findViewById(R.id.assignment_add_title_field);
		descriptionField = (TextView)findViewById(R.id.assignment_add_description_field);
		
		dueDateButton = (Button)findViewById(R.id.assignment_add_date_due);
		timeDueButton = (Button)findViewById(R.id.assignment_add_time_due);
	}

	private void populateSpinner() {
		courseCursor = DbUtils.getCoursesAsCursor(this);
		String[] from = new String[]{Values.KEY_NAME};
		int[] to = new int[]{android.R.id.text1};
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
				courseCursor, from, to, 0);
		adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		courseSpinner.setAdapter(adapter);
		courseSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (!justRestoredState)
					if (!userSetDateTime) {
						setDueDateToNextClassTime();
						updateButtons(0);
					}
				else
					justRestoredState = false;
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}
	
	private void populateFields() {
		
		if (rowId != null) {
			DbAdapter dbAdapter = new DbAdapter(this, null, Values.ASSIGNMENT_TABLE);
			dbAdapter.open();
			Cursor data = dbAdapter.fetch(rowId, Values.ASSIGNMENT_FETCH);
			courseSpinner.setSelection(DbUtils.getPositionFromRowID(courseCursor, data.getShort(data.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_COURSE))));
			titleField.setText(data.getString(data.getColumnIndexOrThrow(Values.KEY_TITLE)));
			descriptionField.setText(data.getString(data.getColumnIndexOrThrow(Values.KEY_DESCRIPTION)));
			
			long time = data.getLong(data.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_DUE_DATE));
			data.close();
			dbAdapter.close();
			time = DateUtils.convertMinutesToMills(time);
			calendar = Calendar.getInstance();
			calendar.setTimeInMillis(time);
			userSetDateTime = true;
		} else {
			setDueDateToNextClassTime();
		}
	}

	private void setDueDateToNextClassTime() {
		calendar = Calendar.getInstance();
		// Set the calendar based on the next class time
		int course = courseSpinner.getSelectedItemPosition();
		short[] courseStartTimes = getClassStartTimes(course);
		// Determine how many days from now the next class is
		boolean[] days = getCourseDays(courseStartTimes);
		short curDayOfWeek = (short) (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1);
		short dayDiff = 1;
		// Used to keep track of the class start time. Defaults to noon.
		short startTime = 720;
		for (int day = curDayOfWeek + 1; dayDiff < 8; day++, dayDiff++) {
			if (day == 7)
				day = 0;
			if (days[day] == true) {
				startTime = courseStartTimes[day];
				break;
			}
		}
		// dayDiff is only 8 if there are no days entered for the course,
			// so reset it to one day from now.
		if (dayDiff > 7) dayDiff = 1;
		calendar.add(Calendar.DATE, dayDiff);
		// Set the calendar time to the class start time
		calendar.set(Calendar.HOUR_OF_DAY, startTime / 60);
		calendar.set(Calendar.MINUTE, startTime % 60);
	}
	
	private short[] getClassStartTimes(int course) {
		DbAdapter dbAdapter = new DbAdapter(this, null, Values.COURSE_TABLE);
		dbAdapter.open();
		Cursor courseIds = dbAdapter.fetchAll(new String[] { Values.KEY_ROWID } );
		courseIds.moveToPosition(course);
		int courseId = courseIds.getInt(0);
		Cursor times = dbAdapter.fetch(courseId, new String[] {Values.COURSE_KEY_TIMES_OF_DAY} );
		short[] startTimes = new short[7];
		try {
			JSONArray array = new JSONArray(times.getString(times.getColumnIndexOrThrow(Values.COURSE_KEY_TIMES_OF_DAY)));
			for (short i = 0; i < 14; i += 2) {
				startTimes[i/2] = (short) array.getInt(i);
			}
		} catch (JSONException e) {}
		courseIds.close();
		dbAdapter.close();
		return startTimes;
	}
	
	private boolean[] getCourseDays(short[] times) {
		boolean[] days = new boolean[7];
		for (short i = 0; i < 7; i++)
			if (times[i] != 0)
				days[i] = true;
		return days;
	}
	
	private static final String CALENDAR_TIME_KEY = "calendar_time";
	private static final String USER_SET_DUE_DATE = "user_set_due_date";
	
	public void onSaveInstanceState(Bundle state) {
		state.putShort(Values.ASSIGNMENT_KEY_COURSE, (short)courseSpinner.getSelectedItemPosition()); // Selected course
		state.putString(Values.KEY_TITLE, titleField.getText().toString());
		state.putString(Values.KEY_DESCRIPTION, descriptionField.getText().toString());
		state.putBoolean(USER_SET_DUE_DATE, userSetDateTime);
		long time = calendar.getTimeInMillis();
		state.putLong(CALENDAR_TIME_KEY, time);
	}
	
	public void onRestoreInstanceState(Bundle oldState) {
		if (oldState != null) {
			try {
				courseSpinner.setSelection(oldState.getShort(Values.ASSIGNMENT_KEY_COURSE));
				titleField.setText(oldState.getString(Values.KEY_TITLE));
				descriptionField.setText(oldState.getString(Values.KEY_DESCRIPTION));
				long time = oldState.getLong(CALENDAR_TIME_KEY);
				calendar.setTimeInMillis(time);
				updateButtons(0);
				userSetDateTime = oldState.getBoolean(USER_SET_DUE_DATE);
			} catch (NullPointerException e) {}
			justRestoredState = true;
		}
	}
	
	private void loadDataFromIntent() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (rowId == null && extras.containsKey(Values.KEY_ROWID)) {
				// We are editing an existing assignment
				rowId = extras.getLong(Values.KEY_ROWID);
			}
			short passedCourse = extras.getShort(Values.ASSIGNMENT_KEY_COURSE);
			if (passedCourse > 0)
				// The user entered Add Assignment from viewing a course
					// other than the first course, so we should change to
					// that course. The course id was passed in the Bundle.
				courseSpinner.setSelection(passedCourse);
		}
	}
	
	private static final String CALENDAR_KEY = "calendar";
	
	public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
		
		private Calendar mCalendar;
		
		@Override
		public void setArguments(Bundle args) {
			mCalendar = (Calendar)args.getSerializable(CALENDAR_KEY);
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Context context = getActivity();
			return new TimePickerDialog(context, this, mCalendar.get(Calendar.HOUR_OF_DAY),
					mCalendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(context));
		}
		
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			((AssignmentEditFragment)getActivity()).setTime(hourOfDay, minute);
		}
	}
	
	protected void setTime(int hourOfDay, int minute) {
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		updateButtons(2);
	}
	
	public void showTimePickerDialog(View v) {
		Bundle args = new Bundle();
		args.putSerializable("calendar", calendar);
		
		TimePickerFragment frag = new TimePickerFragment();
		frag.setArguments(args);
		frag.show(getSupportFragmentManager(), "timePicker");
	}
	
	
	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
		
		private Calendar mCalendar;
		
		@Override
		public void setArguments(Bundle args) {
			mCalendar = (Calendar)args.getSerializable(CALENDAR_KEY);
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new DatePickerDialog(getActivity(), this, mCalendar.get(Calendar.YEAR),
					mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DATE));
		}
		
		public void onDateSet(DatePicker view, int year, int month, int day) {
			//TODO Not sure I'm doing this right...
			((AssignmentEditFragment)getActivity()).setDate(year, month, day);
		}
	}
	
	protected void setDate(int year, int month, int day) {
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		updateButtons(1);
	}
	
	public void showDatePickerDialog(View v) {
		Bundle args = new Bundle();
		args.putSerializable(CALENDAR_KEY, calendar);
		DatePickerFragment frag = new DatePickerFragment();
		frag.setArguments(args);
		frag.show(getSupportFragmentManager(), "timePicker");
	}
	
	/**
	 * Update the date and/or time buttons.
	 * @param which which button to update: <b>1</b> is date, <b>2</b> is time, <b>0</b> is both
	 */
	private void updateButtons(int which) {
		if (which == 0 || which == 1)
			dueDateButton.setText(DateUtils.formatAsString(calendar, DATE_FORMAT));
		if (which == 0 || which == 2)
			timeDueButton.setText(DateUtils.formatAsString(calendar, TIME_FORMAT));
	}
	
	public void cancelPressed(View v) {
		finish();
	}
	
	public void savePressed(View v) {
		saveData();
		finish();
	}
	
	private void saveData() {
		
		addAssignment(titleField.getText().toString(),
				(short) courseSpinner.getSelectedItemId(),
				descriptionField.getText().toString(),
				DateUtils.convertMillsToMinutes(calendar.getTimeInMillis()),
				rowId);
	}
	
	/**
	 * Adds or updates an assignment in the database.
	 * @param title the title of the assignment
	 * @param course the course the assignment is from
	 * @param description a description for the assignment
	 * @param dueDate the assignment's due date and time
	 * @param points the amount of points the assignment is worth
	 * @param rowId the position in the database to update, or <b>null</b> to add a new assignment
	 */
    private void addAssignment(String title, short course, String description, long dueDate, Long rowId) {
    	ContentValues values = new ContentValues();
    	values.put(Values.KEY_TITLE, title);
    	values.put(Values.ASSIGNMENT_KEY_COURSE, course);
    	values.put(Values.KEY_DESCRIPTION, description);
    	values.put(Values.ASSIGNMENT_KEY_DUE_DATE, dueDate);

		DbAdapter dbAdapter = new DbAdapter(this, null, Values.ASSIGNMENT_TABLE);
		dbAdapter.open();
		// Check to see if we are adding or updating an assignment
    	if (rowId == null)
    		dbAdapter.add(values);
    	else
    		dbAdapter.update(rowId, values);
    	dbAdapter.close();
    }
	
}
