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
import com.acedit.assignamo.objects.Course;
import com.acedit.assignamo.utils.DateUtils;

public class DaySelectFragment extends FragmentActivity {
	
	private static final short DEFAULT_TIME = 720;
	
	private static short[] startTimes;
	private static short[] stopTimes;
	private short day;
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
				.putExtra(Course.START_TIMES_KEY, startTimes)
				.putExtra(Course.STOP_TIMES_KEY, stopTimes);
				setResult(RESULT_OK, data);
				finish();
			}
		});
		
		setCheckboxStatuses();
		refreshButtonText();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putShortArray(Course.START_TIMES_KEY, startTimes);
		outState.putShortArray(Course.STOP_TIMES_KEY, stopTimes);
	}
	
	private void initializeTimes(Bundle savedInstanceState) {
		
		if (savedInstanceState != null) {
			startTimes = savedInstanceState.getShortArray(Course.START_TIMES_KEY);
			stopTimes = savedInstanceState.getShortArray(Course.STOP_TIMES_KEY);
		} else {
			Bundle extras = getIntent().getExtras();
			if (extras != null && extras.containsKey(Course.START_TIMES_KEY)) {
				startTimes = extras.getShortArray(Course.START_TIMES_KEY);
				stopTimes = extras.getShortArray(Course.STOP_TIMES_KEY);
			} else {
				startTimes = new short[7];
				stopTimes = new short[7];
			}
		}
	}

	private static final int START_BUTTON = 0;
	private static final int STOP_BUTTON = 1;
	
	@Override
	protected Dialog onCreateDialog(int id) {
		TimePickerDialog.OnTimeSetListener listener;
		int hour, minute;
		if (id == START_BUTTON) {
			listener = new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					startTimes[day] = (short)(hourOfDay * 60 + minute);
					// If the ending time is not yet set, set it to an hour later
					if (stopTimes[day] == 0) {
						stopTimes[day] = (short)( (hourOfDay + 1) * 60 + minute);
						updateButtonText(day, (short)1);
					}
					updateButtonText(day, START_BUTTON);
				}
			};
			hour = startTimes[day] / 60;
			minute = startTimes[day] % 60;
		} else {
			listener = new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					stopTimes[day] = (short)(hourOfDay * 60 + minute);
					updateButtonText(day, STOP_BUTTON);
				}
			};
			hour = stopTimes[day] / 60;
			minute = stopTimes[day] % 60;
		}
		return new TimePickerDialog(this, listener, hour, minute, false);
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		short time = id == START_BUTTON ? startTimes[day] : stopTimes[day];
		if (time == 0)
			time = DEFAULT_TIME;
		((TimePickerDialog)dialog).updateTime(time / 60, time % 60);
	}
	
	private void updateButtonText(short day, int button) {
		buttons[day][button].setText( button == START_BUTTON ?
			DateUtils.formatAsString( startTimes[day] / 60, startTimes[day] % 60, false)
			: DateUtils.formatAsString( stopTimes[day] / 60, stopTimes[day] % 60, false)
		);
	}
	
	private void checkBoxToggled(int id, boolean checked) {
		short day = checkboxMap.get(id);
		if (checked)
			enableRow(day);
		else
			disableRow(day);
	}
	
	private void enableRow(short day) {
		buttons[day][0].setEnabled(true);
		buttons[day][1].setEnabled(true);
	}
	
	private void disableRow(short day) {
		buttons[day][0].setEnabled(false);
		buttons[day][1].setEnabled(false);
	}
	
	private void buttonPressed(int id) {
		day = buttonIdMap.get(id);
		int button = START_BUTTON;
		if (day >= 7) {
			day -= 7;
			button = STOP_BUTTON;
		}
		showDialog(button);
	}
	
	private void setCheckboxStatuses() {
		for (short i = 0; i < 7; i++)
			if (startTimes[i] == 0)
				disableRow(i);
			else
				checkboxes[i].setChecked(true);
	}
	
	private void refreshButtonText() {
		for (short d = 0; d < 7; d++) {
			updateButtonText(d, START_BUTTON);
			updateButtonText(d, STOP_BUTTON);
		}
	}

}