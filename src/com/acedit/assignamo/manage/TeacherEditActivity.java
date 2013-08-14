package com.acedit.assignamo.manage;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;

import com.acedit.assignamo.R;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.objects.TeacherEditor;

public class TeacherEditActivity extends FragmentActivity {
	
	private TeacherEditor mTeacher;
	
	private EditText nameField, departmentField, roomField, notesField,
	emailField, phoneNumberField;
		
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.teacher_edit);
		
		if (savedInstanceState != null)
			mTeacher = (TeacherEditor) savedInstanceState.getSerializable("mTeacher");
		else {
			Short rowId = getRowIdFromIntent();
			mTeacher = rowId == null ? new TeacherEditor(this) : new TeacherEditor(this, rowId);
		}
		
		setTitle(mTeacher.editingExisting() ? R.string.teacher_edit_edit_teacher : R.string.teacher_edit_add_teacher);
		
		mapViews();
		populateFields();
	}
	
	private Short getRowIdFromIntent() {
		Bundle extras = getIntent().getExtras();
		return extras != null
				? (short)extras.getLong(Values.KEY_ROWID)
				: null;
	}
			
	private void mapViews() {
		nameField = (EditText)findViewById(R.id.teacher_edit_name_field);
		departmentField = (EditText)findViewById(R.id.teacher_edit_subject_field);
		notesField = (EditText)findViewById(R.id.teacher_edit_notes_field);
		roomField = (EditText)findViewById(R.id.teacher_edit_room_field);
		emailField = (EditText)findViewById(R.id.teacher_edit_email_field);
		phoneNumberField = (EditText)findViewById(R.id.teacher_edit_phone_field);
	}
	
	private void populateFields() {
		if (mTeacher.editingExisting()) {
			nameField.setText(mTeacher.getName());
			departmentField.setText(mTeacher.getDepartment());
			notesField.setText(mTeacher.getNotes());
			roomField.setText(mTeacher.getRoom());
			emailField.setText(mTeacher.getEmail());
			phoneNumberField.setText(mTeacher.getPhoneNumberAsString());
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		updateTeacherObject();
		outState.putSerializable("mTeacher", mTeacher);
	}
	
	private void updateTeacherObject() {
		mTeacher.setName(nameField.getText().toString());
		mTeacher.setDepartment(departmentField.getText().toString());
		mTeacher.setNotes(notesField.getText().toString());
		mTeacher.setEmail(emailField.getText().toString());
		mTeacher.setPhoneNumber(phoneNumberField.getText().toString());
		mTeacher.setRoom(roomField.getText().toString());
		mTeacher.setPhoneNumber(phoneNumberField.getText().toString());
	}
	
	public void cancelButtonPressed(View v) {
		finish();
	}
	
	public void saveButtonPressed(View v) {
		updateTeacherObject();
		mTeacher.commitToDatabase();
		finish();
	}
	
}
