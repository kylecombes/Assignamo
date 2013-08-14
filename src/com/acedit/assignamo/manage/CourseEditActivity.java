package com.acedit.assignamo.manage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.acedit.assignamo.R;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.objects.Course;
import com.acedit.assignamo.objects.CourseEditor;
import com.acedit.assignamo.utils.DbUtils;

public class CourseEditActivity extends FragmentActivity {
	
	private CourseEditor mCourse;
	private static final int DAY_SELECT_RESULT_ID = 1;
	
	private EditText titleField;
	private Spinner teacherSpinner;
	private EditText descriptionField;
	private EditText roomField;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!teacherExists()) {
			new AlertDialog.Builder(this)
			.setTitle(R.string.course_edit_teacher_required_title)
			.setMessage(R.string.course_edit_teacher_required_message)
			.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(getApplicationContext(), TeacherEditActivity.class);
					startActivity(i);
				}
			})
			.show();
		}
		
		setContentView(R.layout.course_edit);
		
		mapViews();
		if (savedInstanceState != null)
			mCourse = (CourseEditor) savedInstanceState.getSerializable("mCourse");
		else {
			Short rowId = getRowIdFromIntent();
			mCourse = rowId == null ? new CourseEditor(this) : new CourseEditor(this, rowId);
		}
		
		setTitle(mCourse.editingExisting() ? R.string.course_edit : R.string.course_add);
		
		populateFields();
	}

	private Short getRowIdFromIntent() {
		Bundle extras = getIntent().getExtras();
		return extras != null
				? (short)extras.getLong(Values.KEY_ROWID)
				: null;
	}
	
	private void mapViews() {
		titleField = (EditText)findViewById(R.id.course_edit_title_field);
		teacherSpinner = (Spinner)findViewById(R.id.course_edit_teacher_spinner);
		descriptionField = (EditText)findViewById(R.id.course_edit_description_field);
		roomField = (EditText)findViewById(R.id.course_edit_room_field);
	}
	
	private void populateFields() {
		
		Cursor teacherCursor = DbUtils.getTeachersAsCursor(this);
		
		String[] from = new String[]{Values.KEY_NAME};
		int[] to = new int[]{android.R.id.text1};
		SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
				teacherCursor, from, to, 0 );
		cursorAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		teacherSpinner.setAdapter(cursorAdapter);
			    
		if (mCourse.editingExisting()) {
			titleField.setText(mCourse.getTitle());
			teacherSpinner.setSelection(DbUtils.getPositionFromRowID(teacherCursor, mCourse.getTeacherId()));
			descriptionField.setText(mCourse.getDescription());
			roomField.setText(mCourse.getRoom());
		}
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		updateCourseObject();
		outState.putSerializable("mCourse", mCourse);
	}
	
	private void updateCourseObject() {
		mCourse.setTitle(titleField.getText().toString().trim());
		mCourse.setTeacher((short)teacherSpinner.getSelectedItemId());
		mCourse.setDescription(descriptionField.getText().toString().trim());
		mCourse.setRoom(roomField.getText().toString().trim());
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
	
	/*---------- Buttons ----------*/
	
	public void daySelectButtonPressed(View v) {
		Intent intent = new Intent(this, DaySelectFragment.class);
		if (mCourse.getClassStartTimes() != null) {
			intent.putExtra(Course.START_TIMES_KEY, mCourse.getClassStartTimes());
			intent.putExtra(Course.STOP_TIMES_KEY, mCourse.getClassStopTimes());
		}
		startActivityForResult(intent, DAY_SELECT_RESULT_ID);
	}
	
	public void colorSelectButtonPressed(View v) {
		showDialog(0);
	}
	
	// Color-picker dialog
	protected Dialog onCreateDialog(int id) {
		return new ColorPickerDialog(this, new ColorPickerDialog.OnColorChangedListener() {
			public void colorChanged(int color) {
				mCourse.setColor(color);
			}
		}, mCourse.getColor());
	}
	
	public void cancelPressed(View v) {
		finish();
	}
	
	public void savePressed(View v) {
		updateCourseObject();
		mCourse.commitToDatabase();
		finish();
	}
	
}
