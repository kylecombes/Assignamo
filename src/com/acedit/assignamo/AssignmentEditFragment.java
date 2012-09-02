package com.acedit.assignamo;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.manage.CourseEditActivity;
import com.acedit.assignamo.utils.DateUtils;
import com.acedit.assignamo.utils.DbUtils;

public class AssignmentEditFragment extends Activity {
	
	private Calendar calendar;
	static final String DATE_FORMAT = "MM/dd/yyyy";
	static final String TIME_FORMAT = "hh:mm a";
	private static final short DATE_PICKER_DIALOG = 0;
	private static final short TIME_PICKER_DIALOG = 1;
	private Long rowId;
	private boolean userSetDateTime;
	
	private Spinner courseSpinner;
	private TextView titleField;
	private Button dueDateButton;
	private Button timeDueButton;
	private TextView pointsField;
	private TextView descriptionField;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (DbUtils.getCourseCount(getApplicationContext()) == 0) {
			AlertDialog.Builder d = new AlertDialog.Builder(this);
			d.setTitle(R.string.assignment_edit_course_required_title);
			d.setMessage(R.string.assignment_edit_course_required_message);
			d.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(getBaseContext(), CourseEditActivity.class);
					startActivity(i);
				}
			});
			d.show();
		}
		
		setContentView(R.layout.assignment_edit);
		// Set the title of the Activity
		if (rowId == null)
			setTitle(R.string.assignment_add);
		else
			setTitle(R.string.assignment_edit);
		
		initializeViews();
		loadDataFromIntent();
		populateFields();
		
		updateButtons(0);
		//titleField.requestFocus();
	}
	
	private void initializeViews() {
		// Initialize the Spinner
		courseSpinner = (Spinner)findViewById(R.id.assignment_add_course_select);
		Cursor courses = DbUtils.getCoursesAsCursor(this);
		String[] from = new String[]{Values.KEY_NAME};
		int[] to = new int[]{android.R.id.text1};
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
				courses, from, to, 0);
		adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		courseSpinner.setAdapter(adapter);
		courseSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (!userSetDateTime) {
					setDueDateToNextClassTime();
					updateButtons(0);
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		titleField = (TextView)findViewById(R.id.assignment_add_title_field);
		descriptionField = (TextView)findViewById(R.id.assignment_add_description_field);
		pointsField = (TextView)findViewById(R.id.assignment_add_points);
		
		dueDateButton = (Button)findViewById(R.id.assignment_add_date_due);
		dueDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DATE_PICKER_DIALOG);
			}
		});
		timeDueButton = (Button)findViewById(R.id.assignment_add_time_due);
		timeDueButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(TIME_PICKER_DIALOG);
			}
		});
		((Button)findViewById(R.id.assignment_add_save)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData();
				finish();
			}
		});
		((Button)findViewById(R.id.assignment_add_cancel)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void populateFields() {
		
		if (rowId != null) {
			DbAdapter dbAdapter = new DbAdapter(this, Values.DATABASE_NAME, Values.DATABASE_VERSION,
					Values.ASSIGNMENT_TABLE, Values.DATABASE_CREATE, Values.KEY_ROWID);
			dbAdapter.open();
			Cursor data = dbAdapter.fetch(rowId, Values.ASSIGNMENT_FETCH);
			courseSpinner.setSelection(data.getShort(data.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_COURSE)));
			titleField.setText(data.getString(data.getColumnIndexOrThrow(Values.KEY_TITLE)));
			descriptionField.setText(data.getString(data.getColumnIndexOrThrow(Values.KEY_DESCRIPTION)));
			
			long points = data.getLong(data.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_POINTS));
			if (points >= 0)
				pointsField.setText("" + points);
			
			long time = data.getLong(data.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_DUE_DATE));
			time = DateUtils.convertMinutesToMills(time);
			calendar.setTimeInMillis(time);
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
		for (int i = curDayOfWeek + 1; i != curDayOfWeek; i++, dayDiff++) {
			if (i > 7)
				i = 0;
			if (days[i] == true) {
				startTime = courseStartTimes[i];
				break;
			}
		}
		calendar.add(Calendar.DATE, dayDiff);
		// Set the calendar time to the class start time
		calendar.set(Calendar.HOUR_OF_DAY, startTime / 60);
		calendar.set(Calendar.MINUTE, startTime % 60);
	}
	
	private short[] getClassStartTimes(int course) {
		DbAdapter dbAdapter = new DbAdapter(this, Values.DATABASE_NAME, Values.DATABASE_VERSION, Values.COURSE_TABLE, Values.DATABASE_CREATE, Values.KEY_ROWID);
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
	
	public void onSaveInstanceState(Bundle state) {
		state.putShort(Values.ASSIGNMENT_KEY_COURSE, (short)courseSpinner.getSelectedItemPosition()); // Selected course
		state.putString(Values.KEY_TITLE, titleField.getText().toString());
		state.putString(Values.KEY_DESCRIPTION, descriptionField.getText().toString());
		short pointsEntered = -1;
		String pointsTxt = pointsField.getText().toString();
		if (pointsTxt.length() > 0)
			try {
				pointsEntered = Short.parseShort(pointsTxt);
			} catch (NumberFormatException e) {}
		state.putShort(Values.ASSIGNMENT_KEY_POINTS, pointsEntered);
		state.putLong(CALENDAR_TIME_KEY, calendar.getTimeInMillis());
	}
	
	public void onRestoreInstanceState(Bundle oldState) {
		if (oldState != null) {
			try {
				courseSpinner.setSelection(oldState.getShort(Values.ASSIGNMENT_KEY_COURSE));
				titleField.setText(oldState.getString(Values.KEY_TITLE));
				descriptionField.setText(oldState.getString(Values.KEY_DESCRIPTION));
				calendar.setTimeInMillis(oldState.getLong(CALENDAR_TIME_KEY));
				updateButtons(0);
				pointsField.setText(oldState.getShort(Values.ASSIGNMENT_KEY_POINTS) + "");
			} catch (NullPointerException e) {}
		}
	}
	
	private void loadDataFromIntent() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (rowId == null && extras.containsKey(Values.KEY_ROWID)) {
				// We are editing an existing assignment
				rowId = extras.getLong(Values.KEY_ROWID);
			}
			short passedCourse = extras.getShort(Values.NEW_ASSIGNMENT_COURSE_KEY);
			if (passedCourse > 0)
				// The user entered Add Assignment from viewing a course
					// other than the first course, so we should change to
					// that course. The course id was passed in the Bundle.
				courseSpinner.setSelection(passedCourse);
		}
	}
	
	private void saveData() {
				
		// Get the assignment's point value
		long points = -1;
		if (pointsField.getText().length() > 0) {
			try {
				points = Long.parseLong(pointsField.getText().toString());
			} catch (NumberFormatException e) {
				Toast.makeText(this, R.string.assignment_edit_points_out_of_range_message, Toast.LENGTH_LONG).show();
			}
		}
		
		addAssignment(titleField.getText().toString(),
				(short)courseSpinner.getSelectedItemPosition(),
				descriptionField.getText().toString(),
				DateUtils.convertMillsToMinutes(calendar.getTimeInMillis()),
				points, rowId);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_PICKER_DIALOG:
			return showDatePicker();
		case TIME_PICKER_DIALOG:
			return showTimePicker();
		}
		return super.onCreateDialog(id);
	}
	
	private DatePickerDialog showDatePicker() {
		DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				calendar.set(Calendar.YEAR, year);
				calendar.set(Calendar.MONTH, monthOfYear);
				calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				updateButtons(1);
				if (!userSetDateTime)
					userSetDateTime = true;
			}
			
		}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		return datePicker;
	}

	private TimePickerDialog showTimePicker() {
		return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				calendar.set(Calendar.MINUTE, minute);
				updateButtons(2);
				if (!userSetDateTime)
					userSetDateTime = true;
			}
		}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
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
	
	/**
	 * Adds or updates an assignment in the database.
	 * @param title the title of the assignment
	 * @param course the course the assignment is from
	 * @param description a description for the assignment
	 * @param dueDate the assignment's due date and time
	 * @param points the amount of points the assignment is worth
	 * @param rowId the position in the database to update, or <b>null</b> to add a new assignment
	 */
    private void addAssignment(String title, short course, String description, long dueDate, long points, Long rowId) {
    	ContentValues values = new ContentValues();
    	values.put(Values.KEY_TITLE, title);
    	values.put(Values.ASSIGNMENT_KEY_COURSE, course);
    	values.put(Values.KEY_DESCRIPTION, description);
    	values.put(Values.ASSIGNMENT_KEY_DUE_DATE, dueDate);
    	values.put(Values.ASSIGNMENT_KEY_POINTS, points);

		DbAdapter dbAdapter = new DbAdapter(this, Values.DATABASE_NAME, Values.DATABASE_VERSION,
				Values.ASSIGNMENT_TABLE, Values.DATABASE_CREATE, Values.KEY_ROWID);
		dbAdapter.open();
		// Check to see if we are adding or updating an assignment
    	if (rowId == null)
    		dbAdapter.add(values);
    	else
    		dbAdapter.update(rowId, values);
    	dbAdapter.close();
    }
	
}
