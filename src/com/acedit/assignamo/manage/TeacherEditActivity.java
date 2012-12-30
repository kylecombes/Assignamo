package com.acedit.assignamo.manage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.acedit.assignamo.R;
import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;

public class TeacherEditActivity extends FragmentActivity {
	
	private Long rowId;
	private DbAdapter dbAdapter;
	private Context context;
	static final String DATE_FORMAT_DISPLAY = "yyyy-MM-dd";
	static final String DATE_FORMAT_SAVE = "MM/dd/yyyy";
	static final String TIME_FORMAT_DISPLAY = "kk:mm";
	static final String TIME_FORMAT_SAVE = "dd:mm a";
	static final String DATE_TIME_FORMAT = "yyyy-MM-dd kk:mm:ss";
	
	private EditText nameField;
	private EditText subjectField;
	private EditText roomNumberField;
	private EditText notesField;
	private EditText emailField;
	private EditText phoneNumberField;
	private Button saveButton;
	private Button cancelButton;
	private boolean newTeacher;
		
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getBaseContext();
		dbAdapter = new DbAdapter(context, null, Values.TEACHER_TABLE);
		
		setContentView(R.layout.teacher_edit);
		
		initializeFields();
		initializeButtons();
		
		nameField.requestFocus();
	}
	
	public void onResume() {
		super.onResume();
		if (context == null)
			context = getBaseContext();
		dbAdapter.open();
		loadDataFromIntent();
		setTitle();
		populateFields();
	}
	
	public void onPause() {
		super.onPause();
		dbAdapter.close();
	}
	
	private void initializeFields() {
		nameField = (EditText)findViewById(R.id.teacher_edit_name_field);
		subjectField = (EditText)findViewById(R.id.teacher_edit_subject_field);
		notesField = (EditText)findViewById(R.id.teacher_edit_notes_field);
		roomNumberField = (EditText)findViewById(R.id.teacher_edit_room_field);
		emailField = (EditText)findViewById(R.id.teacher_edit_email_field);
		phoneNumberField = (EditText)findViewById(R.id.teacher_edit_phone_field);
	}
	
	private void initializeButtons() {
		saveButton = (Button)findViewById(R.id.teacher_edit_save);
	    saveButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				saveData();
				finish();
			}
		});
	    
		cancelButton = (Button)findViewById(R.id.teacher_edit_cancel);
	    cancelButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void loadDataFromIntent() {
		if (rowId == null) {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				rowId = extras.getLong(Values.KEY_ROWID);
				newTeacher = false;
			}
			else
				newTeacher = true;
		}
	}
	
	private void setTitle() {
		if (newTeacher)
			setTitle(R.string.teacher_edit_add_teacher);
		else
			setTitle(R.string.teacher_edit_edit_teacher);
	}
	
	private void populateFields() {
		if (rowId != null) {
			dbAdapter.open();
			Cursor data = dbAdapter.fetch(rowId, Values.TEACHER_FETCH);
			
			nameField.setText(data.getString(data.getColumnIndexOrThrow(Values.KEY_NAME)));
			subjectField.setText(data.getString(data.getColumnIndexOrThrow(Values.TEACHER_KEY_SUBJECT)));
			notesField.setText(data.getString(data.getColumnIndexOrThrow(Values.KEY_NOTES)));
			//TODO Populate room field
			int roomNum = data.getShort(data.getColumnIndexOrThrow(Values.KEY_ROOM));
			if (roomNum != -1)
				roomNumberField.setText("" + roomNum);
			
			emailField.setText(data.getString(data.getColumnIndexOrThrow(Values.TEACHER_KEY_EMAIL)));
			long phone = data.getLong(data.getColumnIndexOrThrow(Values.TEACHER_KEY_PHONE));
			if (phone >= 0)
				phoneNumberField.setText("" + phone);
		}
	}
	
	private void saveData() {
		short roomNum = -1;
		try {
			roomNum = Short.parseShort(roomNumberField.getText().toString());
		} catch (NumberFormatException e) {}
		long phoneNum = -1;
		try {
			phoneNum = Long.parseLong(phoneNumberField.getText().toString());
		} catch (NumberFormatException e) {}
		
		if (rowId == null)
			addTeacher(nameField.getText().toString().trim(),
					subjectField.getText().toString().trim(),
					notesField.getText().toString().trim(),
					roomNum,
					emailField.getText().toString().trim(),
					phoneNum);
		else
			updateTeacher(rowId,
					nameField.getText().toString().trim(),
					subjectField.getText().toString().trim(),
					notesField.getText().toString().trim(),
					roomNum,
					emailField.getText().toString().trim(),
					phoneNum);
		
	}
	
	//TODO Combine these into one
    private long addTeacher(String name, String subject, String notes, short roomNumber, String email, long phoneNumber) {
    	ContentValues values = new ContentValues();
    	values.put(Values.KEY_NAME, name);
    	values.put(Values.TEACHER_KEY_SUBJECT, subject);
    	values.put(Values.KEY_NOTES, notes);
    	values.put(Values.KEY_ROOM, roomNumber);
    	values.put(Values.TEACHER_KEY_EMAIL, email);
    	values.put(Values.TEACHER_KEY_PHONE, phoneNumber);
    	return dbAdapter.add(values);
    }

    private boolean updateTeacher(long rowId, String name, String subject, String notes, short roomNumber, String email, long phoneNumber) {
    	ContentValues values = new ContentValues();
    	values.put(Values.KEY_NAME, name);
    	values.put(Values.TEACHER_KEY_SUBJECT, subject);
    	values.put(Values.KEY_NOTES, notes);
    	values.put(Values.KEY_ROOM, roomNumber);
    	values.put(Values.TEACHER_KEY_EMAIL, email);
    	values.put(Values.TEACHER_KEY_PHONE, phoneNumber);
    	return dbAdapter.update(rowId, values);
    }
    
}
