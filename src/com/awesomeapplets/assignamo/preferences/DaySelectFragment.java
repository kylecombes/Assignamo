package com.awesomeapplets.assignamo.preferences;

import java.util.HashMap;
import java.util.Map;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TimePicker;

import com.awesomeapplets.assignamo.R;
import com.awesomeapplets.assignamo.database.Values;
import com.awesomeapplets.assignamo.utils.DateUtils;

public class DaySelectFragment extends FragmentActivity {
	
	private static final short DEFAULT_TIME = 0;
	
	private static short[][] times;
	private short day;
	private short button;
	private CheckBox[] checkboxes = new CheckBox[7];
	private Button[][] buttons = new Button[7][2];
	private Map<Integer,Short> checkboxMap = new HashMap<Integer,Short>();
	private Map<Integer,Short> buttonIdMap = new HashMap<Integer,Short>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.course_days_select);
		
		initializeTimes();
		
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
				
				@Override
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
					
					@Override
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
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		// Configure OK button
		findViewById(R.id.course_edit_days_select_ok).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for (short i = 0; i < 7; i++) {
					// For each checkbox that is not checked, set the corresponding times to zero
					if (!checkboxes[i].isChecked()) {
						times[i][0] = 0;
						times[i][1] = 0;
					}
				}
				Intent data = new Intent();
				short[] startTimes = new short[7];
				short[] stopTimes = new short[7];
				for (short i = 0; i < 7; i++) {
					startTimes[i] = times[i][0];
					stopTimes[i] = times[i][1];
				}
				data.putExtra(Values.COURSE_EDIT_DAYS_SELECT_START_TIMES_KEY, startTimes);
				data.putExtra(Values.COURSE_EDIT_DAYS_SELECT_STOP_TIMES_KEY, stopTimes);
				setResult(RESULT_OK, data);
				finish();
			}
		});
		
		setCheckboxStatuses();
		refreshButtonText();
	}
	
	private void initializeTimes() {
				
		Bundle extras = getIntent().getExtras();
		times = new short[7][2];
		if (extras != null) {
			short[] startTimes = extras.getShortArray(Values.COURSE_EDIT_DAYS_SELECT_START_TIMES_KEY);
			short[] stopTimes = extras.getShortArray(Values.COURSE_EDIT_DAYS_SELECT_STOP_TIMES_KEY);
			for (short i = 0; i < 7; i++) {
				times[i][0] = startTimes[i];
				times[i][1] = stopTimes[i];
			}
		} else {
			for (short i = 0; i < 7; i++)
				for (short x = 0; x < 2; x++)
					times[i][x] = DEFAULT_TIME;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				times[day][button] = (short)(hourOfDay * 60 + minute);
				// If the ending time is not yet set, set it to an hour later
				if (button == 0 && times[day][1] == 0) {
					times[day][(short)1] = (short)( (hourOfDay + 1) * 60 + minute);
					updateButtonText(day, (short)1);
				}
				updateButtonText(day, button);
			}
		}, times[day][button] / 60, times[day][button] % 60, false);
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		((TimePickerDialog)dialog).updateTime(times[day][button] / 60, times[day][button] % 60);
	}
	
	private void updateButtonText(short day, short button) {
		buttons[day][button].setText(DateUtils.formatAsString( times[day][button] / 60,
				times[day][button] % 60,
				false));
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
		if (day >= 7) {
			day -= 7;
			button = 1;
		}
		else
			button = 0;
		showDialog(0);
	}
	
	private void setCheckboxStatuses() {
		for (short i = 0; i < 7; i++)
			if (times[i][0] == 0)
				disableRow(i);
			else
				checkboxes[i].setChecked(true);
	}
	
	private void refreshButtonText() {
		for (short d = 0; d < 7; d++)
			for (short i = 0; i < 2; i++) {
				short hrs = (short)(times[d][i] / 60);
				short mins = (short)(times[d][i] % 60);
				String text;
				if (hrs == 0 && mins == 0)
					text = getString(R.string.course_edit_days_select_not_set);
				else
					text = DateUtils.formatAsString(hrs, mins, false);
				buttons[d][i].setText(text);
			}
	}

}