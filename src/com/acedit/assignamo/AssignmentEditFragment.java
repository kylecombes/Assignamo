package com.acedit.assignamo;

import java.util.Calendar;
import java.util.TreeMap;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.manage.CourseEditActivity;
import com.acedit.assignamo.objects.Assignment;
import com.acedit.assignamo.objects.AssignmentEditor;
import com.acedit.assignamo.objects.Course;
import com.acedit.assignamo.utils.DateUtils;
import com.acedit.assignamo.utils.DbUtils;

public class AssignmentEditFragment extends ActionBarActivity {
	
	LinearLayout mRemindersContainer;
	private Calendar mCalendar;
	static final String DATE_FORMAT = "E, M/d/yyyy";
	static final String TIME_FORMAT = "hh:mm a";
	private AssignmentEditor mAssignment;
	private boolean userSetDateTime;
	
	private Spinner courseSpinner;
	private TextView titleField;
	private Button dueDateButton;
	private Button timeDueButton;
	private TextView descriptionField;
	private static enum DueDateButtons { DATE, TIME, BOTH };
	/** Used to keep track of whether the activity was just restored. This way we don't have
	 * to call {@link #setDueDateToNextClassTime()} when the spinner has just been populated
	 * after an orientation change. Probably not the most efficient way of doing this...
	 */
	private boolean justRestoredState;
	
	private TreeMap<Long,Short> courseIdToPos;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.assignment_edit);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		
		mapViews();
		if (savedInstanceState != null) {
			mAssignment = (AssignmentEditor) savedInstanceState.getSerializable("mAssignment");
			titleField.setText(mAssignment.getTitle());
			descriptionField.setText(mAssignment.getDescription());
			mCalendar = (Calendar) savedInstanceState.getSerializable(CALENDAR_KEY);
			updateButtons(DueDateButtons.BOTH);
			userSetDateTime = savedInstanceState.getBoolean(USER_SET_DUE_DATE);
		} else { // Get data from Intent
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				if (extras.containsKey(Values.KEY_ROWID)) {
					mAssignment = new AssignmentEditor(this, extras.getLong(Values.KEY_ROWID));
				} else {
					mAssignment = new AssignmentEditor();
					short passedCourseId = extras.getShort(Assignment.KEY_COURSE);
					mAssignment.setCourseId(passedCourseId);
				}
			}
		}
		
		setTitle(mAssignment.editingExisting() ? R.string.assignment_edit : R.string.assignment_add);
		populateFields();
		
		updateButtons(DueDateButtons.BOTH);
	}
	
	private void mapViews() {
		
		mRemindersContainer = (LinearLayout) findViewById(R.id.assignment_edit_reminders_row);
		courseSpinner = (Spinner)findViewById(R.id.assignment_add_course_select);
		
		// Populate the spinner
		Cursor courseCursor = DbUtils.getCoursesAsCursor(this);
		courseCursor.moveToFirst();
		courseIdToPos = new TreeMap<Long, Short>();
		for (short i = 0;; ++i) {
			long id = courseCursor.getShort(0);
			courseIdToPos.put(id, i);
			if (courseCursor.moveToNext() == false) break;
		}
		MatrixCursor extras = new MatrixCursor(new String[] { "_id", Values.KEY_TITLE });
		extras.addRow(new String[] { "-1", getString(R.string.assignment_edit_new_course)});
		MergeCursor mc = new MergeCursor(new Cursor[] { courseCursor, extras });
		
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
				mc, new String[]{Values.KEY_NAME}, new int[]{android.R.id.text1}, 0);
		adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		courseSpinner.setAdapter(adapter);
		courseSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				if (!justRestoredState) {
					
					switch ((int)id) {
					case -1:
						startActivity(new Intent(getApplicationContext(), CourseEditActivity.class));
						break;
					default:
						if (!userSetDateTime) {
							setDueDateToNextClassTime();
							updateButtons(DueDateButtons.BOTH);
						}
					}
					
				} else {
					justRestoredState = false;
				}
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
			
		});
		
		titleField = (TextView)findViewById(R.id.assignment_add_title_field);
		descriptionField = (TextView)findViewById(R.id.assignment_add_description_field);
		dueDateButton = (Button)findViewById(R.id.assignment_add_date_due);
		timeDueButton = (Button)findViewById(R.id.assignment_add_time_due);
	}

	private void populateFields() {
		long courseId = mAssignment.getCourseId();
		if (courseId > 0) {
			courseSpinner.setSelection(courseIdToPos.get(courseId));
		}
		if (mAssignment.editingExisting()) {
			titleField.setText(mAssignment.getTitle());
			descriptionField.setText(mAssignment.getDescription());
			
			mCalendar = Calendar.getInstance();
			mCalendar.setTimeInMillis(mAssignment.getDueDate());
			userSetDateTime = true;
		} else {
			setDueDateToNextClassTime();
		}
	}

	private void setDueDateToNextClassTime() {
		Course course = new Course(this, (short) courseSpinner.getSelectedItemId());
		mCalendar = course.getNextClassTime(this);
	}
	
	private static final String USER_SET_DUE_DATE = "user_set_due_date";
	
	public void onSaveInstanceState(Bundle state) {
		updateAssignmentObject();
		state.putSerializable("mAssignment", mAssignment);
		state.putSerializable(CALENDAR_KEY, mCalendar);
		state.putBoolean(USER_SET_DUE_DATE, userSetDateTime);
	}
	
	private void updateAssignmentObject() {
		mAssignment.setTitle(titleField.getText().toString().trim());
		mAssignment.setCourseId((short) courseSpinner.getSelectedItemId());
		mAssignment.setDescription(descriptionField.getText().toString().trim());
		mAssignment.setDueDate(mCalendar);
	}
	
	/*---------- Action Bar ----------*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.edit_fragment_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		
		switch (menuItem.getItemId()) {
		
		case R.id.action_save:
			updateAssignmentObject();
			mAssignment.commitToDatabase(this);
			finish();
			return true;
			
		default:
			return false;
		}
	}
	
	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return true;
	}
	
	/*---------- Due date and time pickers ---------*/
	
	private static final String CALENDAR_KEY = "mCalendar";
	
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
		mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		mCalendar.set(Calendar.MINUTE, minute);
		userSetDateTime = true;
		updateButtons(DueDateButtons.TIME);
	}
	
	public void showTimePickerDialog(View v) {
		Bundle args = new Bundle();
		args.putSerializable("mCalendar", mCalendar);
		
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
			((AssignmentEditFragment)getActivity()).setDate(year, month, day);
		}
	}
	
	protected void setDate(int year, int month, int day) {
		mCalendar.set(Calendar.YEAR, year);
		mCalendar.set(Calendar.MONTH, month);
		mCalendar.set(Calendar.DAY_OF_MONTH, day);
		userSetDateTime = true;
		updateButtons(DueDateButtons.DATE);
	}
	
	public void showDatePickerDialog(View v) {
		Bundle args = new Bundle();
		args.putSerializable(CALENDAR_KEY, mCalendar);
		DatePickerFragment frag = new DatePickerFragment();
		frag.setArguments(args);
		frag.show(getSupportFragmentManager(), "timePicker");
	}
	
	/**
	 * Update the date and/or time buttons.
	 * @param whichButtons which button to update.
	 */
	private void updateButtons(DueDateButtons whichButtons) {
		switch (whichButtons) {
		case BOTH:
		case DATE:
			dueDateButton.setText(DateUtils.formatAsString(mCalendar, DATE_FORMAT));
			if (whichButtons != DueDateButtons.BOTH) break;
		case TIME:
			timeDueButton.setText(DateUtils.formatAsString(mCalendar, TIME_FORMAT));
		}
	}
			
}
