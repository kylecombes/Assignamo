package com.awesomeapplets.assignamo.preferences;

import java.util.Calendar;

import com.awesomeapplets.assignamo.R;
import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.DbUtils;
import com.awesomeapplets.assignamo.database.Values;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

public class CourseEditFragment extends FragmentActivity {
	
	private Cursor courseCursor;
	private Long rowId;
	private DbAdapter dbAdapter;
	private Calendar calendar;
	static final String DATE_FORMAT_DISPLAY = "yyyy-MM-dd";
	static final String DATE_FORMAT_SAVE = "MM/dd/yyyy";
	static final String TIME_FORMAT_DISPLAY = "kk:mm";
	static final String TIME_FORMAT_SAVE = "dd:mm a";
	static final String DATE_TIME_FORMAT = "yyyy-MM-dd kk:mm:ss";
	
	private TextView titleField;
	private Spinner teacherSpinner;
	private TextView descriptionField;
	private TextView daysField;
	private TextView timesField;
	private TextView roomNumberField;
	private TextView creditHoursField;
	private Button saveButton;
	private Button cancelButton;
		
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new DbAdapter(getBaseContext(), Values.DATABASE_NAME, Values.DATABASE_VERSION,
				Values.COURSE_TABLE, Values.DATABASE_CREATE, Values.KEY_ROWID);
		calendar = Calendar.getInstance();

		if (!teacherExists()) {
			AlertDialog.Builder d = new AlertDialog.Builder(this);
			d.setTitle(R.string.course_edit_teacher_required_title);
			d.setMessage(R.string.course_edit_teacher_required_message);
			d.setPositiveButton(R.string.ok, new AlertDialog.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Intent i = new Intent(getApplicationContext(), TeacherEditActivity.class);
					startActivity(i);
				}
			});
			d.show();
		}
		
		setContentView(R.layout.course_edit);
		
		initializeFields();

		titleField.requestFocus();
	}
	
	public void onResume() {
		super.onResume();
		dbAdapter.open();
		setRowIdFromIntent();
		populateFields();
	}
	
	public void onPause() {
		super.onPause();
		dbAdapter.close();
	}
	
	private void initializeFields() {
		titleField = (TextView)findViewById(R.id.course_edit_title_field);
		teacherSpinner = (Spinner)findViewById(R.id.course_edit_teacher_spinner);
		descriptionField = (TextView)findViewById(R.id.course_edit_description_field);
		daysField = (TextView)findViewById(R.id.course_edit_days_field);
		timesField = (TextView)findViewById(R.id.course_edit_times_field);
		roomNumberField = (TextView)findViewById(R.id.course_edit_room_field);
		creditHoursField = (TextView) findViewById(R.id.course_edit_credit_hours_field);
		saveButton = (Button)findViewById(R.id.course_edit_save);
		cancelButton = (Button)findViewById(R.id.course_edit_cancel);
		
	    saveButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				saveData();
				finish();
			}
		});
	    cancelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	
	private void populateFields() {

		courseCursor = DbUtils.getTeachers(getApplicationContext());
		startManagingCursor(courseCursor);
		
		// create an array to specify which fields we want to display
		String[] from = new String[]{Values.KEY_NAME};
		// create an array of the display item we want to bind our data to
		int[] to = new int[]{android.R.id.text1};
		// create simple cursor adapter
		SimpleCursorAdapter adapter =
		  new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, courseCursor, from, to, 0 );
		adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		teacherSpinner.setAdapter(adapter);
			    
		if (rowId != null) {
			dbAdapter.open();
			Cursor data = dbAdapter.fetch(rowId, Values.COURSE_FETCH);
			titleField.setText(data.getString(data.getColumnIndexOrThrow(Values.KEY_NAME)));
			teacherSpinner.setSelection(data.getShort(data.getColumnIndexOrThrow(Values.COURSE_KEY_TEACHER)));
			descriptionField.setText(data.getString(data.getColumnIndexOrThrow(Values.KEY_DESCRIPTION)));
			daysField.setText(data.getString(data.getColumnIndexOrThrow(Values.COURSE_KEY_DAYS_OF_WEEK)));
			timesField.setText(data.getString(data.getColumnIndexOrThrow(Values.COURSE_KEY_TIMES_OF_DAY)));
			roomNumberField.setText(data.getString(data.getColumnIndexOrThrow(Values.KEY_ROOM)));
			creditHoursField.setText(data.getString(data.getColumnIndexOrThrow(Values.COURSE_KEY_CREDIT_HOURS)));
		}
	}
	
	private void saveData() {
		
		// Get room number
		short roomNum;
		try {
			roomNum = Short.parseShort(roomNumberField.getText().toString());
		} catch (NumberFormatException e) {
			roomNum = -1;
		}
		
		// Get the number of credit hours
		short creditHours;
		try {
			creditHours = Short.parseShort(creditHoursField.getText().toString());
		} catch (NumberFormatException e) {
			creditHours = -1;
		}
		
		addCourse(titleField.getText().toString(),
				(short)teacherSpinner.getSelectedItemPosition(),
				descriptionField.getText().toString(),
				roomNum,
				daysField.getText().toString(),
				timesField.getText().toString(),
				creditHours);
		
	}
	
	private void setRowIdFromIntent() {
		if (rowId == null) {
			Bundle extras = getIntent().getExtras();
			rowId = extras != null
					? extras.getLong(Values.KEY_ROWID)
					: null;
		}
	}
	
	private boolean teacherExists() {
		return DbUtils.getTeachersAsArray(getApplicationContext()).length > 0;
	}
	
	private DatePickerDialog showDatePicker() {
			DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					calendar.set(Calendar.YEAR, year);
					calendar.set(Calendar.MONTH, monthOfYear);
					calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				}
				
			}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
			return datePicker;
	}
	
	private long addCourse(String name, short teacherId, String description, long roomNum, String daysOfWeek, String timesOfDay, short creditHours) {
    	// TODO
    	ContentValues values = new ContentValues();
    	values.put(Values.KEY_NAME, name);
    	values.put(Values.COURSE_KEY_TEACHER, teacherId);
    	values.put(Values.KEY_DESCRIPTION, description);
    	values.put(Values.KEY_ROOM, roomNum);
    	values.put(Values.COURSE_KEY_DAYS_OF_WEEK, daysOfWeek);
    	values.put(Values.COURSE_KEY_TIMES_OF_DAY, timesOfDay);
    	values.put(Values.COURSE_KEY_CREDIT_HOURS, creditHours);
    	return dbAdapter.add(values);
    }
    
}
