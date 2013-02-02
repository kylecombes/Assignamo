package com.acedit.assignamo;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.objects.Assignment;
import com.acedit.assignamo.objects.Course;
import com.acedit.assignamo.utils.DateUtils;
import com.acedit.assignamo.utils.DbUtils;

public class AssignmentEditFragment extends FragmentActivity {
	
	Context mContext;
	LinearLayout mRemindersContainer;
	private Calendar mCalendar;
	static final String DATE_FORMAT = "E, M/d/yyyy";
	static final String TIME_FORMAT = "hh:mm a";
	private Long rowId;
	private Assignment mAssignment;
	private boolean userSetDateTime;
	private Cursor courseCursor;
	/** If we just restored the state, we don't need to reload the next class time */
	private boolean justRestoredState;
	
	private Spinner courseSpinner;
	private TextView titleField;
	private Button dueDateButton;
	private Button timeDueButton;
	private TextView descriptionField;
	private static enum DueDateButtons { DATE, TIME, BOTH };  
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.assignment_edit);
		mContext = getApplicationContext();
		
		mRemindersContainer = (LinearLayout) findViewById(R.id.assignment_edit_reminders_row);
		loadDataFromIntent();
		setTitle(rowId == null ? R.string.assignment_add : R.string.assignment_edit);
		mapViews();
		populateFields();
		
		updateButtons(DueDateButtons.BOTH);
	}
	
	private void mapViews() {
		courseSpinner = (Spinner)findViewById(R.id.assignment_add_course_select);
		titleField = (TextView)findViewById(R.id.assignment_add_title_field);
		descriptionField = (TextView)findViewById(R.id.assignment_add_description_field);
		dueDateButton = (Button)findViewById(R.id.assignment_add_date_due);
		timeDueButton = (Button)findViewById(R.id.assignment_add_time_due);
	}

	private void populateFields() {
		// Populate the spinner
		courseCursor = DbUtils.getCoursesAsCursor(this);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
				courseCursor, new String[]{Values.KEY_NAME}, new int[]{android.R.id.text1}, 0);
		adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		courseSpinner.setAdapter(adapter);
		courseSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (!justRestoredState)
					if (!userSetDateTime) {
						setDueDateToNextClassTime();
						updateButtons(DueDateButtons.BOTH);
					}
				else
					justRestoredState = false;
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		if (rowId != null) {
			Assignment mAssignment = new Assignment(mContext, rowId);
			courseSpinner.setSelection(DbUtils.getPositionFromRowID(courseCursor, mAssignment.getCourseId()));
			titleField.setText(mAssignment.getTitle());
			descriptionField.setText(mAssignment.getTitle());
			
			long time = mAssignment.getDueDate();
			time = DateUtils.convertMinutesToMills(time);
			mCalendar = Calendar.getInstance();
			mCalendar.setTimeInMillis(time);
			userSetDateTime = true;
		} else {
			setDueDateToNextClassTime();
		}
	}

	private void setDueDateToNextClassTime() {
		Course course = new Course(mContext, (short) courseSpinner.getSelectedItemId());
		mCalendar = course.getNextClassTime(mContext);
	}
	
	private static final String CALENDAR_TIME_KEY = "mCalendar_time";
	private static final String USER_SET_DUE_DATE = "user_set_due_date";
	
	public void onSaveInstanceState(Bundle state) {
		state.putShort(Assignment.KEY_COURSE, (short)courseSpinner.getSelectedItemPosition()); // Selected course
		state.putString(Values.KEY_TITLE, titleField.getText().toString());
		state.putString(Values.KEY_DESCRIPTION, descriptionField.getText().toString());
		state.putBoolean(USER_SET_DUE_DATE, userSetDateTime);
		state.putLong(CALENDAR_TIME_KEY, mCalendar.getTimeInMillis());
	}
	
	public void onRestoreInstanceState(Bundle oldState) {
		if (oldState != null) {
			try {
				courseSpinner.setSelection(oldState.getShort(Assignment.KEY_COURSE));
				titleField.setText(oldState.getString(Values.KEY_TITLE));
				descriptionField.setText(oldState.getString(Values.KEY_DESCRIPTION));
				long time = oldState.getLong(CALENDAR_TIME_KEY);
				mCalendar.setTimeInMillis(time);
				updateButtons(DueDateButtons.BOTH);
				userSetDateTime = oldState.getBoolean(USER_SET_DUE_DATE);
			} catch (NullPointerException e) {}
			justRestoredState = true;
		}
	}
	
	private void loadDataFromIntent() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (rowId == null && extras.containsKey(Values.KEY_ROWID)) {
				// We are editing an existing mAssignment
				rowId = extras.getLong(Values.KEY_ROWID);
			}
			short passedCourse = extras.getShort(Assignment.KEY_COURSE);
			if (passedCourse > 0)
				// The user entered Add Assignment from viewing a course
					// other than the first course, so we should change to
					// that course. The course id was passed in the Bundle.
				courseSpinner.setSelection(passedCourse);
		}
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
	
	public void cancelPressed(View v) {
		finish();
	}
	
	public void savePressed(View v) {
		mAssignment.setTitle(titleField.getText().toString().trim());
		mAssignment.setCourseId((short) courseSpinner.getSelectedItemId());
		mAssignment.setDescription(descriptionField.getText().toString().trim());
		mAssignment.setDueDate(DateUtils.convertMillsToMinutes(mCalendar.getTimeInMillis()));
		mAssignment.commitToDatabase(mContext);
		finish();
	}
		
}
