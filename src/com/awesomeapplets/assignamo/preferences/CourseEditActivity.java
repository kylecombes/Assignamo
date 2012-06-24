package com.awesomeapplets.assignamo.preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.awesomeapplets.assignamo.R;
import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.Values;
import com.awesomeapplets.assignamo.utils.DbUtils;

public class CourseEditActivity extends Activity {
	
	private Cursor courseCursor;
	private Long rowId;
	private DbAdapter dbAdapter;
	private Context context;
	static final String DATE_FORMAT_DISPLAY = "yyyy-MM-dd";
	static final String DATE_FORMAT_SAVE = "MM/dd/yyyy";
	static final String TIME_FORMAT_DISPLAY = "kk:mm";
	static final String TIME_FORMAT_SAVE = "dd:mm a";
	static final String DATE_TIME_FORMAT = "yyyy-MM-dd kk:mm:ss";
	static final int DAY_SELECT_RESULT_ID = 1;
	
	private TextView titleField;
	private Spinner teacherSpinner;
	private TextView descriptionField;
	private TextView roomNumberField;
	private TextView creditHoursField;
	private Button saveButton;
	private Button cancelButton;
	private short[][] times = new short[7][2];
		
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getBaseContext();
		dbAdapter = new DbAdapter(context, Values.DATABASE_NAME, Values.DATABASE_VERSION,
				Values.COURSE_TABLE, Values.DATABASE_CREATE, Values.KEY_ROWID);

		if (!teacherExists()) {
			AlertDialog.Builder d = new AlertDialog.Builder(this);
			d.setTitle(R.string.course_edit_teacher_required_title);
			d.setMessage(R.string.course_edit_teacher_required_message);
			d.setPositiveButton(R.string.ok, new AlertDialog.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Intent i = new Intent(context, TeacherEditActivity.class);
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
		if (context == null)
			context = getBaseContext();
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
		roomNumberField = (TextView)findViewById(R.id.course_edit_room_field);
		creditHoursField = (TextView) findViewById(R.id.course_edit_credit_hours_field);
		saveButton = (Button)findViewById(R.id.course_edit_save);
		cancelButton = (Button)findViewById(R.id.course_edit_cancel);
		
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

		courseCursor = DbUtils.getTeachersAsCursor(context);
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
			roomNumberField.setText(data.getString(data.getColumnIndexOrThrow(Values.KEY_ROOM)));
			creditHoursField.setText(data.getString(data.getColumnIndexOrThrow(Values.COURSE_KEY_CREDIT_HOURS)));
		}
		
		((Button)findViewById(R.id.course_edit_days_button)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, DaySelectFragment.class);
				if (times != null) {
					short[] startTimes = new short[7];
					short[] stopTimes = new short[7];
					for (short i = 0; i < 7; i++) {
						startTimes[i] = times[i][0];
						stopTimes[i] = times[i][1];
					}
					intent.putExtra(Values.COURSE_EDIT_DAYS_SELECT_START_TIMES_KEY, startTimes);
					intent.putExtra(Values.COURSE_EDIT_DAYS_SELECT_STOP_TIMES_KEY, stopTimes);
				}
				startActivityForResult(intent, DAY_SELECT_RESULT_ID);
			}
		});
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
		
		/*addCourse(titleField.getText().toString(),
				(short)teacherSpinner.getSelectedItemPosition(),
				descriptionField.getText().toString(),
				roomNum,
				daysField.getText().toString(),
				timesField.getText().toString(),
				creditHours);*/
		
	}
	
	private void setRowIdFromIntent() {
		if (rowId == null) {
			Bundle extras = getIntent().getExtras();
			rowId = extras != null
					? extras.getLong(Values.KEY_ROWID)
					: null;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			short[] startTimes = data.getShortArrayExtra(Values.COURSE_EDIT_DAYS_SELECT_START_TIMES_KEY);
			short[] stopTimes = data.getShortArrayExtra(Values.COURSE_EDIT_DAYS_SELECT_STOP_TIMES_KEY);
			for (short i = 0; i < 7; i++) {
				times[i][0] = startTimes[i];
				times[i][1] = stopTimes[i];
			}
		}
	}
	
	private boolean teacherExists() {
		return DbUtils.getTeachersAsArray(context).length > 0;
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
