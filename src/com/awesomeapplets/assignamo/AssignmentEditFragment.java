package com.awesomeapplets.assignamo;

import java.util.Calendar;

import com.awesomeapplets.assignamo.database.DateAdapter;
import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.DbUtils;
import com.awesomeapplets.assignamo.database.Values;
import com.awesomeapplets.assignamo.preferences.CourseEditFragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AssignmentEditFragment extends FragmentActivity {
	
	private DbAdapter dbAdapter;
	private Calendar calendar;
	static final String DATE_FORMAT = "MM/dd/yyyy";
	static final String TIME_FORMAT = "dd:mm a";
	static final String DATE_TIME_FORMAT = "yyyy-MM-dd kk:mm:ss";
	private static final short DATE_PICKER_DIALOG = 0;
	private static final short TIME_PICKER_DIALOG = 1;
	private Long rowId;
	
	private Spinner courseSpinner;
	protected Object selectedCourse;
	private TextView titleField;
	private Button dueDateButton;
	private Button timeDueButton;
	private TextView pointsField;
	private TextView descriptionField;
	private Button saveButton;
	private Button cancelButton;
	boolean newBook;
		
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new DbAdapter(getBaseContext(), Values.DATABASE_NAME, Values.DATABASE_VERSION,
				Values.ASSIGNMENT_TABLE, Values.DATABASE_CREATE, Values.KEY_ROWID);
		calendar = Calendar.getInstance();
		
		if (!courseExists()) {
			AlertDialog.Builder d = new AlertDialog.Builder(this);
			d.setTitle(R.string.assignment_edit_course_required_title);
			d.setMessage(R.string.assignment_edit_course_required_message);
			d.setPositiveButton(R.string.ok, new AlertDialog.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(getApplicationContext(), CourseEditFragment.class);
					startActivity(i);
				}
			});
			d.show();
		}
		
		setContentView(R.layout.assignment_edit);
		
		loadDataFromIntent();
		initializeFields();
		populateFields();
		initializeButtons();
		
		updateDateButtonText();
		updateTimeButtonText();
		titleField.requestFocus();
	}
	
	public void onResume() {
		super.onResume();
		dbAdapter.open();
		loadDataFromIntent();
		setTitle();
		populateFields();
	}
	
	public void onPause() {
		super.onPause();
		dbAdapter.close();
	}
	
	private void initializeFields() {
		courseSpinner = (Spinner)findViewById(R.id.assignment_add_course_select);
		titleField = (TextView)findViewById(R.id.assignment_add_title_field);
		descriptionField = (TextView)findViewById(R.id.assignment_add_description_field);
		
		pointsField = (TextView)findViewById(R.id.assignment_add_points);
		
	}
	
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

		Cursor courseCursor = DbUtils.getCoursesAsCursor(getApplicationContext());
		
		// create an array to specify which fields we want to display
		String[] from = new String[]{Values.KEY_NAME};
		// create an array of the display item we want to bind our data to
		int[] to = new int[]{android.R.id.text1};
		// create simple cursor adapter
		SimpleCursorAdapter adapter =
				new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, courseCursor, from, to, 0);
		adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		courseSpinner.setAdapter(adapter);
		
	    
		if (rowId != null) {
			dbAdapter.open();
			Cursor assignmentData = dbAdapter.fetch(rowId, Values.ASSIGNMENT_FETCH);
			courseSpinner.setSelection(assignmentData.getShort(assignmentData.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_COURSE)));
			titleField.setText(assignmentData.getString(assignmentData.getColumnIndexOrThrow(Values.KEY_TITLE)));
			descriptionField.setText(assignmentData.getString(assignmentData.getColumnIndexOrThrow(Values.KEY_DESCRIPTION)));
			pointsField.setText("" + assignmentData.getLong(assignmentData.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_POINTS)));
			
			long time = assignmentData.getLong(assignmentData.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_DUE_DATE));
			time = DateAdapter.convertMinutesToMills(time);
			calendar.setTimeInMillis(time);
		}
	}
	
	private void loadDataFromIntent() {
		if (rowId == null) {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				rowId = extras.getLong(Values.KEY_ROWID);
				newBook = false;
			}
			newBook = true;
		}
	}
	
	private void setTitle() {
		if (newBook)
			setTitle(R.string.assignment_add);
		else
			setTitle(R.string.assignment_edit);
	}
	
	private boolean courseExists() {
		return DbUtils.getCourseCount(getApplicationContext()) > 0;
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
					DateAdapter.convertMillsToMinutes(calendar.getTimeInMillis()),
					points);
		else
			updateAssignment(titleField.getText().toString(),
					(short)courseSpinner.getSelectedItemPosition(),
					descriptionField.getText().toString(),
					DateAdapter.convertMillsToMinutes(calendar.getTimeInMillis()),
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
	
	
	private void updateDateButtonText() {
		dueDateButton.setText(DateAdapter.formatAsString(calendar, DATE_FORMAT));
	}
	
	private void updateTimeButtonText() {
		timeDueButton.setText(DateAdapter.formatAsString(calendar, TIME_FORMAT));
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
