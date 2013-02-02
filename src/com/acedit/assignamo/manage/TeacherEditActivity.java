package com.acedit.assignamo.manage;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.acedit.assignamo.R;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.objects.Teacher;

public class TeacherEditActivity extends FragmentActivity {
	
	private Context mContext;
	private Teacher mTeacher;
	
	private EditText nameField, departmentField, roomField, notesField,
	emailField, phoneNumberField;
		
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.teacher_edit);
		
		if (savedInstanceState != null)
			mTeacher = (Teacher) savedInstanceState.getSerializable("mTeacher");
		else
			mTeacher = new Teacher(mContext, getRowIdFromIntent());
		
		if (!mTeacher.existsInDatabase())
			setTitle(R.string.teacher_edit_add_teacher);
		else
			setTitle(R.string.teacher_edit_edit_teacher);
		
		mapViews();
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
		nameField = (EditText)findViewById(R.id.teacher_edit_name_field);
		departmentField = (EditText)findViewById(R.id.teacher_edit_subject_field);
		notesField = (EditText)findViewById(R.id.teacher_edit_notes_field);
		roomField = (EditText)findViewById(R.id.teacher_edit_room_field);
		emailField = (EditText)findViewById(R.id.teacher_edit_email_field);
		phoneNumberField = (EditText)findViewById(R.id.teacher_edit_phone_field);
		((Button)findViewById(R.id.teacher_edit_save))
	    .setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				saveData();
				finish();
			}
		});
	    
		((Button)findViewById(R.id.teacher_edit_cancel))
	    .setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void populateFields() {
		if (mTeacher.existsInDatabase()) {
			nameField.setText(mTeacher.getName());
			departmentField.setText(mTeacher.getDepartment());
			notesField.setText(mTeacher.getNotes());
			roomField.setText(mTeacher.getRoom());
			emailField.setText(mTeacher.getEmail());
			phoneNumberField.setText(mTeacher.getPhoneNumberAsString());
		}
	}
	
	private void saveData() {
		mTeacher.setName(nameField.getText().toString());
		mTeacher.setDepartment(departmentField.getText().toString());
		mTeacher.setNotes(notesField.getText().toString());
		mTeacher.setEmail(emailField.getText().toString());
		try {
			short phoneNumber = Short.parseShort(phoneNumberField.getText().toString());
			mTeacher.setPhoneNumber(phoneNumber);
		} catch (NumberFormatException e) {}
		mTeacher.setRoom(roomField.getText().toString());
		mTeacher.commitToDatabase(mContext);
	}
	
}
