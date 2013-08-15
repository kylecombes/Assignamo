package com.acedit.assignamo.objects;

import android.content.ContentValues;
import android.content.Context;

import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;

public class TeacherEditor extends Teacher {
	
	private static final long serialVersionUID = 1L;
	
	public TeacherEditor() {
		
	}

	public TeacherEditor(Context context, short id) {
		super(context, id);
	}

	public void setName(String name) {
		mName = name;
	}
	
	public void setDepartment(String department) {
		mDepartment = department;
	}
	
	public void setNotes(String notes) {
		mNotes = notes;
	}
	
	public void setEmail(String email) {
		mEmail = email;
	}
		
	public void setPhoneNumber(String phoneNumber) {
		mPhoneNumber = phoneNumber;
	}
	
	public void setRoom(String room) {
		mRoom = room;
	}
	
	/**
	 * Check to see whether or not we are creating a new teacher or
	 * editing an existing teacher.
	 * @return whether or not we are editing an existing teacher
	 */
	public boolean editingExisting() {
		return mId != null;
	}
	
	public boolean commitToDatabase(Context context) {
		ContentValues values = new ContentValues();
    	values.put(Values.KEY_NAME, mName);
    	values.put(KEY_DEPARTMENT, mDepartment);
    	values.put(Values.KEY_NOTES, mNotes);
    	values.put(Values.KEY_ROOM, mRoom);
    	values.put(KEY_EMAIL, mEmail);
    	values.put(KEY_PHONE, mPhoneNumber);
    	DbAdapter dbAdapter = new DbAdapter(context, null, TABLE_NAME).open();
    	if (mId == null)
    		return dbAdapter.add(values) > 0;
    	return dbAdapter.update(mId, values);
	}
	
}
