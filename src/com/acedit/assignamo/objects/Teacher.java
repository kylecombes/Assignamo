package com.acedit.assignamo.objects;

import java.io.Serializable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.SpannableString;

import com.acedit.assignamo.R;
import com.acedit.assignamo.ViewFragment;
import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;

public class Teacher implements Serializable {
	
	public static final String TABLE_NAME = "teachers", KEY_DEPARTMENT = "subject",
	KEY_EMAIL = "email", KEY_PHONE = "phone_number";
	public static final String[] FETCH_DATA = { Values.KEY_ROWID, Values.KEY_NAME,
		KEY_DEPARTMENT, Values.KEY_ROOM, KEY_EMAIL, KEY_PHONE, Values.KEY_NOTES };
	public static final String DATABASE_CREATE = "create table " + TABLE_NAME + " ( "
			+ Values.KEY_ROWID + " integer primary key autoincrement, "
			+ Values.KEY_NAME + " text not null, "
			+ KEY_DEPARTMENT + " text, "
			+ Values.KEY_ROOM + " int, "
			+ KEY_EMAIL + " text, "
			+ KEY_PHONE + " int, "
			+ Values.KEY_NOTES + " text);";
	
	private Short mId;
	private String mName, mDepartment, mNotes, mEmail, mRoom;
	private short mPhoneNumber;
	
	public Teacher(Context context, short id) {
		mId = id;
		Cursor c = new DbAdapter(context, null, TABLE_NAME).open()
				.fetch(mId, FETCH_DATA);
		mName = c.getString(c.getColumnIndexOrThrow(Values.KEY_NAME));
		mDepartment = c.getString(c.getColumnIndexOrThrow(KEY_DEPARTMENT));
		mNotes = c.getString(c.getColumnIndexOrThrow(Values.KEY_NOTES));
		mEmail = c.getString(c.getColumnIndexOrThrow(KEY_EMAIL));
		mRoom = c.getString(c.getColumnIndexOrThrow(Values.KEY_ROOM));
		mPhoneNumber = c.getShort(c.getColumnIndexOrThrow(KEY_PHONE));
		c.close();
	}
	
	public Short getId() {
		return mId;
	}
	
	public String getName() {
		return mName;
	}
	
	public String getDepartment() {
		return mDepartment;
	}
	
	public String getNotes() {
		return mNotes;
	}
	
	public String getEmail() {
		return mEmail;
	}
	
	public String getRoom() {
		return mRoom;
	}
	
	public short getPhoneNumber() {
		return mPhoneNumber;
	}
	
	public String getPhoneNumberAsString() {
		return mPhoneNumber + "";
	}
	
	/**
	 * Formats a phone number as text.<br>
	 * NOTE: Only accepts U.S. phone numbers at the moment.
	 * @param number the phone number.
	 * @return a string representation of that number, or a message if there is no phone number entered.
	 * @throws NumberFormatException if the phone number is not of a valid phone number length.
	 */
	// TODO Add support for foreign phone numbers
	public SpannableString getPhoneNumberAsFormattedString(Context context) throws NumberFormatException {
		if (mPhoneNumber < 0)
			return ViewFragment.getItalicizedString(context.getString(R.string.teacher_view_no_phone));
		
		String str = Long.toString(mPhoneNumber);
		String rStr = "";
		
		short len = (short) str.length();
		short i = 0;
		if (len == 7) {
			for (; i < 3; i++)
				rStr += str.charAt(i);
			rStr += "-";
			for (; i < 7; i++)
				rStr += str.charAt(i);
			
		} else if (len == 10) {
			for (; i < 3; i++)
				rStr += str.charAt(i);
			rStr += "-";
			for (; i < 6; i++)
				rStr += str.charAt(i);
			rStr += "-";
			for (; i < 10; i++)
				rStr += str.charAt(i);
			
		} else if (len == 11) {
			rStr += str.charAt(i) + "-";
			i++;
			for (; i < 4; i++)
				rStr += str.charAt(i);
			rStr += "-";
			for (; i < 7; i++)
				rStr += str.charAt(i);
			rStr += "-";
			for (; i < 11; i++)
				rStr += str.charAt(i);
			
		} else
			throw new NumberFormatException("Invalid phone number.");
		
		return new SpannableString(rStr);
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
	
	public void setPhoneNumber(short phoneNumber) {
		mPhoneNumber = phoneNumber;
	}
	
	public void setRoom(String room) {
		mRoom = room;
	}
	
	/**
	 * Check whether or not the teacher is stored in the database. This does
	 * <strong>not</strong> check to see if the copy in the database is
	 * up to date. 
	 * @return whether or not a version of the teacher is stored in the database
	 */
	public boolean existsInDatabase() {
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
