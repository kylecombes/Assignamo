package com.acedit.assignamo.manage;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.EditText;
import android.widget.Spinner;

import com.acedit.assignamo.R;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.objects.Course;
import com.acedit.assignamo.utils.DbUtils;

public class CourseEditActivity extends FragmentActivity {
	
	private Context mContext;
	private Course mCourse;
	private static final int DAY_SELECT_RESULT_ID = 1;
	
	private EditText titleField;
	private Spinner teacherSpinner;
	private EditText descriptionField;
	private EditText roomNumberField;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!teacherExists()) {
			AlertDialog.Builder d = new AlertDialog.Builder(this);
			d.setTitle(R.string.course_edit_teacher_required_title);
			d.setMessage(R.string.course_edit_teacher_required_message);
			d.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(mContext, TeacherEditActivity.class);
					startActivity(i);
				}
			});
			d.show();
		}
		
		setContentView(R.layout.course_edit);
		
		mapViews();
		if (savedInstanceState != null)
			mCourse = (Course) savedInstanceState.getSerializable("mCourse");
		else {
			Short rowId = getRowIdFromIntent();
			if (rowId != null)
				mCourse = new Course(mContext, rowId);
			else
				mCourse = new Course();
		}
		populateFields();
	}

	private Short getRowIdFromIntent() {
		Bundle extras = getIntent().getExtras();
		return extras != null
				? (short)extras.getLong(Values.KEY_ROWID)
				: null;
	}
	
	public void onResume() {
		super.onResume();
		mContext = this;
	}
	
	private void mapViews() {
		titleField = (EditText)findViewById(R.id.course_edit_title_field);
		teacherSpinner = (Spinner)findViewById(R.id.course_edit_teacher_spinner);
		descriptionField = (EditText)findViewById(R.id.course_edit_description_field);
		roomNumberField = (EditText)findViewById(R.id.course_edit_room_field);
		
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
				mCourse.setTitle(titleField.getText().toString().trim());
				mCourse.setTeacher((short)teacherSpinner.getSelectedItemId());
				mCourse.setDescription(descriptionField.getText().toString().trim());
				mCourse.setRoom(roomNumberField.getText().toString().trim());
				mCourse.commitToDatabase(mContext);
				finish();
			}
		});
	}
	
	// Color-picker dialog
	protected Dialog onCreateDialog(int id) {
		return new ColorPickerDialog(this, new ColorPickerDialog.OnColorChangedListener() {
			public void colorChanged(int color) {
				mCourse.setColor(color);
			}
		}, mCourse.getColor());
	}
	
	private void populateFields() {
		
		Cursor teacherCursor = DbUtils.getTeachersAsCursor(this);
		
		String[] from = new String[]{Values.KEY_NAME};
		int[] to = new int[]{android.R.id.text1};
		SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
				teacherCursor, from, to, 0 );
		cursorAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		teacherSpinner.setAdapter(cursorAdapter);
			    
		if (mCourse.getId() != null) {
			titleField.setText(mCourse.getTitle());
			teacherSpinner.setSelection(DbUtils.getPositionFromRowID(teacherCursor, mCourse.getTeacherId()));
			descriptionField.setText(mCourse.getDescription());
			roomNumberField.setText(mCourse.getRoom());
		}
		
		((Button)findViewById(R.id.course_edit_days_button)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(mContext, DaySelectFragment.class);
				if (mCourse.getClassStartTimes() != null) {
					intent.putExtra(Course.START_TIMES_KEY, mCourse.getClassStartTimes());
					intent.putExtra(Course.STOP_TIMES_KEY, mCourse.getClassStopTimes());
				}
				startActivityForResult(intent, DAY_SELECT_RESULT_ID);
			}
		});
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("mCourse", mCourse);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			mCourse.setClassStartTimes(data.getShortArrayExtra(Course.START_TIMES_KEY));
			mCourse.setClassStopTimes(data.getShortArrayExtra(Course.STOP_TIMES_KEY));
		}
	}
	
	private boolean teacherExists() {
		return DbUtils.getTeachersAsCursor(getBaseContext()).getCount() > 0;
	}
	
}
