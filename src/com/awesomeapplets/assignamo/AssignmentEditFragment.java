package com.awesomeapplets.assignamo;

import java.util.Calendar;

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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.Values;
import com.awesomeapplets.assignamo.preferences.CourseEditActivity;
import com.awesomeapplets.assignamo.utils.DateUtils;
import com.awesomeapplets.assignamo.utils.DbUtils;

public class AssignmentEditFragment extends Activity {
	
	private DbAdapter dbAdapter;
	private Calendar calendar;
	static final String DATE_FORMAT = "MM/dd/yyyy";
	static final String TIME_FORMAT = "hh:mm a";
	private static final short DATE_PICKER_DIALOG = 0;
	private static final short TIME_PICKER_DIALOG = 1;
	private Long rowId;
	private short selectedCourse;
	
	private Spinner courseSpinner;
	private TextView titleField;
	private Button dueDateButton;
	private Button timeDueButton;
	private TextView pointsField;
	private TextView descriptionField;
	private Button saveButton;
	private Button cancelButton;
	boolean newAssignment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new DbAdapter(this, Values.DATABASE_NAME, Values.DATABASE_VERSION,
				Values.ASSIGNMENT_TABLE, Values.DATABASE_CREATE, Values.KEY_ROWID);
		calendar = Calendar.getInstance();
		
		if (!courseExists()) {
			AlertDialog.Builder d = new AlertDialog.Builder(this);
			d.setTitle(R.string.assignment_edit_course_required_title);
			d.setMessage(R.string.assignment_edit_course_required_message);
			d.setPositiveButton(R.string.ok, new AlertDialog.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(getBaseContext(), CourseEditActivity.class);
					startActivity(i);
				}
			});
			d.show();
		}
		
		setContentView(R.layout.assignment_edit);
		
		initializeFields();
		initializeButtons();
		
		updateDateButtonText();
		updateTimeButtonText();
		titleField.requestFocus();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		dbAdapter.open();
		loadDataFromIntent();
		setTitle();
		populateFields();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		dbAdapter.close();
	}
	
	private static final String SELECTED_COURSE_KEY = "selected_course";
	private static final String TITLE_KEY = "title";
	private static final String DESCRIPTION_KEY = "desc";
	private static final String POINTS_KEY = "pts";
	private static final String CALENDAR_TIME_KEY = "calendar_time";
	
	public void onSaveInstanceState(Bundle state) {
		state.putShort(SELECTED_COURSE_KEY, (short)courseSpinner.getSelectedItemPosition());
		state.putString(TITLE_KEY, titleField.getText().toString());
		state.putString(DESCRIPTION_KEY, descriptionField.getText().toString());
		long curTime = System.currentTimeMillis();
		short pointsEntered = -1;
		try {
			pointsEntered = Short.parseShort(pointsField.getText().toString());
			Log.d("pointsEntered", "Took " + (System.currentTimeMillis() - curTime) + "ms to resolve.");
		} catch (NumberFormatException e) {
			Log.d("pointEntered", "Crashed. Took " + (System.currentTimeMillis() - curTime) + "ms to resolve.");
		}
		state.putShort(POINTS_KEY, pointsEntered);
		state.putLong(CALENDAR_TIME_KEY, calendar.getTimeInMillis());
	}
	
	public void onRestoreInstanceState(Bundle oldState) {
		if (oldState != null) {
			try {
				selectedCourse = oldState.getShort(SELECTED_COURSE_KEY);
				titleField.setText(oldState.getString(TITLE_KEY));
				descriptionField.setText(oldState.getString(DESCRIPTION_KEY));
				calendar.setTimeInMillis(oldState.getLong(CALENDAR_TIME_KEY));
				updateButtons();
				pointsField.setText(oldState.getShort(POINTS_KEY) + "");
			} catch (NullPointerException e) {
				Toast.makeText(this, "onRestoreInstanceState error", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	/**
	 * Links courseSpinner and TextViews to their UI elements.
	 */
	private void initializeFields() {
		courseSpinner = (Spinner)findViewById(R.id.assignment_add_course_select);
		titleField = (TextView)findViewById(R.id.assignment_add_title_field);
		descriptionField = (TextView)findViewById(R.id.assignment_add_description_field);
		
		pointsField = (TextView)findViewById(R.id.assignment_add_points);
		
	}
	
	/**
	 * Links the buttons to their UI elements and sets up button listeners.
	 */
	private void initializeButtons() {
		
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
		saveButton = (Button)findViewById(R.id.assignment_add_save);
		cancelButton = (Button)findViewById(R.id.assignment_add_cancel);
		 
	    saveButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveData();
				finish();
			}
		});
	    cancelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void populateFields() {

		Cursor courseCursor = DbUtils.getCoursesAsCursor(this);
		
		// create an array to specify which fields we want to display
		String[] from = new String[]{Values.KEY_NAME};
		// create an array of the display item we want to bind our data to
		int[] to = new int[]{android.R.id.text1};
		// create simple cursor adapter
		SimpleCursorAdapter adapter =
				new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, courseCursor, from, to, 0);
		adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		courseSpinner.setAdapter(adapter);
		courseSpinner.setSelection((short)selectedCourse);
	    
		if (rowId != null) {
			dbAdapter.open();
			Cursor assignmentData = dbAdapter.fetch(rowId, Values.ASSIGNMENT_FETCH);
			courseSpinner.setSelection(assignmentData.getShort(assignmentData.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_COURSE)));
			titleField.setText(assignmentData.getString(assignmentData.getColumnIndexOrThrow(Values.KEY_TITLE)));
			descriptionField.setText(assignmentData.getString(assignmentData.getColumnIndexOrThrow(Values.KEY_DESCRIPTION)));
			
			long points = assignmentData.getLong(assignmentData.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_POINTS));
			if (points >= 0)
				pointsField.setText("" + points);
			
			long time = assignmentData.getLong(assignmentData.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_DUE_DATE));
			time = DateUtils.convertMinutesToMills(time);
			calendar.setTimeInMillis(time);
		}
	}
	
	/**
	 * Checks to see if this is a new assignment by checking the rowId.
	 * It also checks to see if there is a passed course value to use.
	 */
	private void loadDataFromIntent() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (rowId == null && extras.containsKey(Values.KEY_ROWID)) {
				rowId = extras.getLong(Values.KEY_ROWID);
				newAssignment = false;
			} else
				newAssignment = true;
			
			short passedCourse = extras.getShort(Values.NEW_ASSIGNMENT_COURSE_KEY);
			if (passedCourse >= 0)
				selectedCourse = passedCourse;
		}
	}
	
	private void setTitle() {
		if (newAssignment)
			setTitle(R.string.assignment_add);
		else
			setTitle(R.string.assignment_edit);
	}
	
	/**
	 * Check to see if there are courses in the database.
	 * @return true if there is at least one course stored in the database.
	 */
	private boolean courseExists() {
		return DbUtils.getCourseCount(this) > 0;
	}
	
	private void saveData() {
				
		// Get the assignment's point value
		long points = -1;
		if (pointsField.getText().length() > 0) {
			try {
				points = Long.parseLong(pointsField.getText().toString());
			} catch (NumberFormatException e) {
				Toast.makeText(this, R.string.assignment_add_points_out_of_range_message, Toast.LENGTH_LONG).show();
			}
		}
		
		if (rowId == null)
			addAssignment(titleField.getText().toString(),
					(short)courseSpinner.getSelectedItemPosition(),
					descriptionField.getText().toString(),
					DateUtils.convertMillsToMinutes(calendar.getTimeInMillis()),
					points);
		else
			updateAssignment(titleField.getText().toString(),
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
					updateDateButtonText();
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
				updateTimeButtonText();
			}
		}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
	}
	
	private void updateButtons() {
		updateDateButtonText();
		updateTimeButtonText();
	}
	
	private void updateDateButtonText() {
		dueDateButton.setText(DateUtils.formatAsString(calendar, DATE_FORMAT));
	}
	
	private void updateTimeButtonText() {
		timeDueButton.setText(DateUtils.formatAsString(calendar, TIME_FORMAT));
	}
	
    private long addAssignment(String title, short course, String description, long dueDate, long points) {
    	ContentValues values = new ContentValues();
    	values.put(Values.KEY_TITLE, title);
    	values.put(Values.ASSIGNMENT_KEY_COURSE, course);
    	values.put(Values.KEY_DESCRIPTION, description);
    	values.put(Values.ASSIGNMENT_KEY_DUE_DATE, dueDate);
    	values.put(Values.ASSIGNMENT_KEY_POINTS, points);
    	
    	return dbAdapter.add(values);
    }
    
    private boolean updateAssignment(String title, short course, String description, long dueDate, long points, Long rowId) {
    	ContentValues values = new ContentValues();
    	values.put(Values.KEY_TITLE, title);
    	values.put(Values.ASSIGNMENT_KEY_COURSE, course);
    	values.put(Values.KEY_DESCRIPTION, description);
    	values.put(Values.ASSIGNMENT_KEY_DUE_DATE, dueDate);
    	values.put(Values.ASSIGNMENT_KEY_POINTS, points);
    	
    	return dbAdapter.update(rowId, values);
    }
	
}
