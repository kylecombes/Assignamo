package com.acedit.assignamo.manage;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.acedit.assignamo.R;
import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.utils.DbUtils;

public class CourseEditActivity extends Activity {
	
	private Long rowId;
	private Context context = this;
	private static final int DAY_SELECT_RESULT_ID = 1;
	private static final String START_TIMES = "start_times";
	private static final String STOP_TIMES = "stop_times";
	
	private TextView titleField;
	private Spinner teacherSpinner;
	private TextView descriptionField;
	private TextView roomNumberField;
	private short[] startTimes = new short[7];
	private short[] stopTimes = new short[7];
	
	int courseColor;
		
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!teacherExists()) {
			AlertDialog.Builder d = new AlertDialog.Builder(this);
			d.setTitle(R.string.course_edit_teacher_required_title);
			d.setMessage(R.string.course_edit_teacher_required_message);
			d.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(context, TeacherEditActivity.class);
					startActivity(i);
				}
			});
			d.show();
		}
		
		setContentView(R.layout.course_edit);
		
		mapViews();
		setRowIdFromIntent();
		populateFields();
		
		if (savedInstanceState != null) {
			startTimes = savedInstanceState.getShortArray(START_TIMES);
			stopTimes = savedInstanceState.getShortArray(STOP_TIMES);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putShortArray(START_TIMES, startTimes);
		outState.putShortArray(STOP_TIMES, stopTimes);
	}
	
	public void onResume() {
		super.onResume();
		context = this;
	}
	
	private void mapViews() {
		titleField = (TextView)findViewById(R.id.course_edit_title_field);
		teacherSpinner = (Spinner)findViewById(R.id.course_edit_teacher_spinner);
		descriptionField = (TextView)findViewById(R.id.course_edit_description_field);
		roomNumberField = (TextView)findViewById(R.id.course_edit_room_field);
		
		((Button)findViewById(R.id.course_edit_color_select_button))
		.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(0);
			}
		});
	    ((Button)findViewById(R.id.course_edit_cancel)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	    ((Button)findViewById(R.id.course_edit_save)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveData();
				finish();
			}
		});
	}
	
	// Color-picker dialog
	protected Dialog onCreateDialog(int id) {
		ColorPickerDialog.OnColorChangedListener listener = new ColorPickerDialog.OnColorChangedListener() {
			public void colorChanged(int color) {
				courseColor = color;
			}
		};
		return new ColorPickerDialog(this, listener, courseColor);
	}
	
	private void populateFields() {
		
		Cursor courseCursor = DbUtils.getTeachersAsCursor(this);
		
		String[] from = new String[]{Values.KEY_NAME};
		int[] to = new int[]{android.R.id.text1};
		SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
				courseCursor, from, to, 0 );
		cursorAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		teacherSpinner.setAdapter(cursorAdapter);
			    
		if (rowId != null) {
			DbAdapter adapter = new DbAdapter(this, null, Values.COURSE_TABLE);
			adapter.open();
			Cursor data = adapter.fetch(rowId, Values.COURSE_FETCH);
			titleField.setText(data.getString(data.getColumnIndexOrThrow(Values.KEY_NAME)));
			teacherSpinner.setSelection(DbUtils.getPositionFromRowID(DbUtils.getTeachersAsCursor(context),
					data.getShort(data.getColumnIndexOrThrow(Values.COURSE_KEY_TEACHER))));
			descriptionField.setText(data.getString(data.getColumnIndexOrThrow(Values.KEY_DESCRIPTION)));
			String room = data.getString(data.getColumnIndexOrThrow(Values.KEY_ROOM));
			if (!room.equals("-1"))
				roomNumberField.setText(room);
			
			loadTimes(data.getString(data.getColumnIndexOrThrow(Values.COURSE_KEY_TIMES_OF_DAY)));
			
			courseColor = data.getInt(data.getColumnIndexOrThrow(Values.COURSE_KEY_COLOR));
			
			data.close();
			adapter.close();
		} else
			courseColor = Color.parseColor(getString(R.color.default_course_color));
		
		((Button)findViewById(R.id.course_edit_days_button)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(context, DaySelectFragment.class)
				.putExtra(Values.COURSE_EDIT_DAYS_SELECT_START_TIMES_KEY, startTimes)
				.putExtra(Values.COURSE_EDIT_DAYS_SELECT_STOP_TIMES_KEY, stopTimes);
				startActivityForResult(intent, DAY_SELECT_RESULT_ID);
			}
		});
		
	}
	
	private void loadTimes(String timesStr) {
		try {
			JSONArray array = new JSONArray(timesStr);
			for (short x = 0, y = 0; x < 7; x++, y += 2) {
				startTimes[x] = (short)array.getInt(y);
				stopTimes[x] = (short)array.getInt(y+1);
			}
		} catch (JSONException e ) { e.printStackTrace(); }
	}
	
	private void saveData() {
		
		// Get room number
		short roomNum;
		String room = roomNumberField.getText().toString();
		if (room.length() > 0)
			try {
				roomNum = Short.parseShort(room);
			} catch (NumberFormatException e) {
				roomNum = -1;
			}
		else
			roomNum = -1;
		room = null;
		
		addCourse(titleField.getText().toString(),
				(short)teacherSpinner.getSelectedItemId(),
				descriptionField.getText().toString(),
				roomNum,
				startTimes,
				stopTimes,
				rowId);
		
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
			startTimes = data.getShortArrayExtra(Values.COURSE_EDIT_DAYS_SELECT_START_TIMES_KEY);
			stopTimes = data.getShortArrayExtra(Values.COURSE_EDIT_DAYS_SELECT_STOP_TIMES_KEY);
		}
	}
	
	private boolean teacherExists() {
		return DbUtils.getTeachersAsCursor(getBaseContext()).getCount() > 0;
	}
	
	private void addCourse(String name, short teacherId, String description, long roomNum, short[] startTimes, short[] stopTimes, Long rowId) {
    	ContentValues values = new ContentValues();
    	values.put(Values.KEY_NAME, name);
    	values.put(Values.COURSE_KEY_TEACHER, teacherId);
    	values.put(Values.KEY_DESCRIPTION, description);
    	values.put(Values.KEY_ROOM, roomNum);
    	
    	JSONArray timesAsArray = new JSONArray();
    	if (startTimes != null && stopTimes != null) {
	    	for (short x = 0; x < 7; x++) {
	    			timesAsArray.put(startTimes[x]);
	    			timesAsArray.put(stopTimes[x]);
	    	}
    	} else for (short i = 0; i < 14; i++)
    		timesAsArray.put(0);
    	
    	values.put(Values.COURSE_KEY_TIMES_OF_DAY, timesAsArray.toString());
    	
    	values.put(Values.COURSE_KEY_COLOR, courseColor);
    	
    	DbAdapter adapter = new DbAdapter(this, null, Values.COURSE_TABLE);
    	adapter.open();
    	
    	if (rowId == null)
    		adapter.add(values);
    	else
    		adapter.update(rowId, values);
    	adapter.close();
    }
	
}
