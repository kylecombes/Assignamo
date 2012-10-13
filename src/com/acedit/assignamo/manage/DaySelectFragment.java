package com.acedit.assignamo.manage;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TimePicker;

import com.acedit.assignamo.R;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.utils.DateUtils;

public class DaySelectFragment extends FragmentActivity {
	
	private static final short DEFAULT_TIME = 720;
	
	private static short[] startTimes;
	private static short[] stopTimes;
	private short day;
	private short button;
	private CheckBox[] checkboxes = new CheckBox[7];
	private Button[][] buttons = new Button[7][2];
	private SparseArray<Short> checkboxMap = new SparseArray<Short>();
	private SparseArray<Short> buttonIdMap = new SparseArray<Short>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.course_days_select);
		
		initializeTimes(savedInstanceState);
		
		int[] checkboxIds = { R.id.course_edit_days_select_day1_checkbox, R.id.course_edit_days_select_day2_checkbox,
				R.id.course_edit_days_select_day3_checkbox, R.id.course_edit_days_select_day4_checkbox,
				R.id.course_edit_days_select_day5_checkbox, R.id.course_edit_days_select_day6_checkbox,
				R.id.course_edit_days_select_day7_checkbox };
		
		int[] firstButtonIds = { R.id.course_edit_days_select_day1_button1, R.id.course_edit_days_select_day2_button1,
				R.id.course_edit_days_select_day3_button1, R.id.course_edit_days_select_day4_button1,
				R.id.course_edit_days_select_day5_button1, R.id.course_edit_days_select_day6_button1,
				R.id.course_edit_days_select_day7_button1 };

		int[] secondButtonIds = { R.id.course_edit_days_select_day1_button2, R.id.course_edit_days_select_day2_button2,
				R.id.course_edit_days_select_day3_button2, R.id.course_edit_days_select_day4_button2,
				R.id.course_edit_days_select_day5_button2, R.id.course_edit_days_select_day6_button2,
				R.id.course_edit_days_select_day7_button2 };
		
		for (short d = 0; d < 7; d++) {
			checkboxes[d] = (CheckBox)findViewById(checkboxIds[d]);
			checkboxes[d].setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					checkBoxToggled(buttonView.getId(), isChecked);
				}
			});
			
			// Add the id of the checkbox to the Map so we can figure out which
				// day the checkbox corresponds to when it is pressed.
			checkboxMap.put(checkboxes[d].getId(), d);
			
			buttons[d][0] = (Button)findViewById(firstButtonIds[d]);
			buttons[d][1] = (Button)findViewById(secondButtonIds[d]);
			for (short i = 0; i < 2; i++) {
				buttons[d][i].setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {
						buttonPressed(v.getId());
					}
				});
			}
			// Add the id of the button to the Map so we can figure out which
				// day the button corresponds to when it is pressed.
			buttonIdMap.put(buttons[d][0].getId(),d);
			// The second buttons will be identified because they have a value greater than 7
				// I.e., day 1 button 2 = 8, day 4 button 2 = 11, etc.
			buttonIdMap.put(buttons[d][1].getId(),(short)(d+7));
			
		}
		
		// Configure Cancel button
		findViewById(R.id.course_edit_days_select_cancel).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		// Configure OK button
		findViewById(R.id.course_edit_days_select_ok).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				for (short i = 0; i < 7; i++) {
					// For each checkbox that is not checked, set the corresponding times to zero
					if (!checkboxes[i].isChecked()) {
						startTimes[i] = 0;
						stopTimes[i] = 0;
					}
				}
				Intent data = new Intent()
				.putExtra(Values.COURSE_EDIT_DAYS_SELECT_START_TIMES_KEY, startTimes)
				.putExtra(Values.COURSE_EDIT_DAYS_SELECT_STOP_TIMES_KEY, stopTimes);
				setResult(RESULT_OK, data);
				finish();
			}
		});
		
		setCheckboxStatuses();
		refreshButtonText();
	}
	
	private void initializeTimes(Bundle savedState) {
		
		if (savedState != null) {
			startTimes = savedState.getShortArray(START_TIMES);
			stopTimes = savedState.getShortArray(STOP_TIMES);
		} else {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				startTimes = extras.getShortArray(Values.COURSE_EDIT_DAYS_SELECT_START_TIMES_KEY);
				stopTimes = extras.getShortArray(Values.COURSE_EDIT_DAYS_SELECT_STOP_TIMES_KEY);
			} else {
				for (short i = 0; i < 7; i++) {
					startTimes[i] = DEFAULT_TIME;
					stopTimes[i] = DEFAULT_TIME;
				}
			}
		}
	}
	
	private static final String START_TIMES = "start_times";
	private static final String STOP_TIMES = "stop_times";
	@Override
	public void onSaveInstanceState(Bundle outState) {
		for (short i = 0; i < 7; i++)
			// Necessary to restore checkbox states correctly
			if (!checkboxes[i].isChecked() && startTimes[i] != 0) {
				startTimes[i] = 0;
				stopTimes[i] = 0;
			}
		outState.putShortArray(START_TIMES, startTimes);
		outState.putShortArray(STOP_TIMES, stopTimes);
	}

	private void updateButtonText(short day, short button) {
		if (button == 0)
			buttons[day][button].setText(DateUtils.formatAsString( startTimes[day] / 60,
				startTimes[day] % 60, false));
		else
			buttons[day][button].setText(DateUtils.formatAsString( stopTimes[day] / 60,
				stopTimes[day] % 60, false));
	}
	
	private void checkBoxToggled(int id, boolean checked) {
		short day = checkboxMap.get(id);
		buttons[day][0].setEnabled(checked);
		buttons[day][1].setEnabled(checked);
	}
	
	private void buttonPressed(int id) {
		day = buttonIdMap.get(id);
		if (day >= 7) {
			day -= 7;
			button = 1;
		}
		else
			button = 0;
		showDialog(0);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				if (button == 0)
					startTimes[day] = (short)(hourOfDay * 60 + minute);
				else
					stopTimes[day] = (short)(hourOfDay * 60 + minute);
				// If the ending time is not yet set, set it to an hour later
				if (button == 0 && stopTimes[day] == 0) {
					stopTimes[day] = (short)( (hourOfDay + 1) * 60 + minute);
					updateButtonText(day, (short)1);
				}
				updateButtonText(day, button);
			}
		};
		
		
		return new TimePickerDialog(this, listener, DEFAULT_TIME / 60, DEFAULT_TIME % 60, false);
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		// If the time is not set (0), set it to the default time
		int time;
		if (startTimes[day] == 0)
			time = DEFAULT_TIME;
		else if (button == 0)
			time = startTimes[day];
		else
			time = stopTimes[day];
		
		int hr = time / 60;
		int min = time % 60;
		((TimePickerDialog)dialog).updateTime(hr, min);
	}
	
	private void setCheckboxStatuses() {
		for (short i = 0; i < 7; i++)
			if (startTimes[i] == 0) {
				buttons[i][0].setEnabled(false);
				buttons[i][1].setEnabled(false);
			} else
				checkboxes[i].setChecked(true);
	}
	
	private void refreshButtonText() {
		// Set start time buttons
		for (short d = 0; d < 7; d++) {
			short hrs = (short)(startTimes[d] / 60);
			short mins = (short)(startTimes[d] % 60);
			String text;
			if (hrs == 0 && mins == 0)
				text = getString(R.string.course_edit_days_select_not_set);
			else
				text = DateUtils.formatAsString(hrs, mins, false);
			buttons[d][0].setText(text);
		}
		// Set stop time buttons
		for (short d = 0; d < 7; d++) {
			short hrs = (short)(stopTimes[d] / 60);
			short mins = (short)(stopTimes[d] % 60);
			String text;
			if (hrs == 0 && mins == 0)
				text = getString(R.string.course_edit_days_select_not_set);
			else
				text = DateUtils.formatAsString(hrs, mins, false);
			buttons[d][1].setText(text);
		}
	}

}