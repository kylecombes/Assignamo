package com.acedit.assignamo.objects;

import java.io.Serializable;

import android.content.Context;
import android.database.Cursor;

import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;

public class Assignment implements Serializable {
	
	public static final String TABLE_NAME = "assignments", KEY_COURSE = "course",
	KEY_DUE_DATE = "due_date", KEY_STATUS = "status", KEY_REMINDERS = "reminders",
	KEY_POINTS = "points";
	public static final short STATUS_INCOMPLETE = 0, STATUS_COMPLETED = 1;
	public static final String[] FETCH_DATA = { Values.KEY_ROWID, Values.KEY_TITLE,
		KEY_COURSE, Values.KEY_DESCRIPTION, KEY_DUE_DATE, KEY_STATUS };
	public static final String[] LIST_FETCH_DATA = { Values.KEY_ROWID, Values.KEY_TITLE, KEY_COURSE, Values.KEY_DESCRIPTION,
		KEY_DUE_DATE };
	public static final String DATABASE_CREATE = "create table " + TABLE_NAME
			+ " ( " + Values.KEY_ROWID + " integer primary key autoincrement, "
			+ Values.KEY_TITLE + " text not null, "
			+ KEY_COURSE + " int not null, "
			+ Values.KEY_DESCRIPTION + " text not null, "
			+ KEY_DUE_DATE + " text not null, "
			+ KEY_STATUS + " int not null default " + STATUS_INCOMPLETE + ", "
			+ KEY_REMINDERS + " text, "
			+ KEY_POINTS + " int);";
	public static final String KEY_SHOWING_COMPLETED = "show_completed_assignments";
	
	protected Long mId;
	protected String mTitle, mDescription;
	protected short mCourseId;
	/** The number of minutes (as opposed to seconds) since the UNIX epoch time. */
	protected long mDueDate;
	protected boolean isCompleted;
	
	public Assignment() {
		
	}
	
	/**
	 * Initializes a new assignment with all of its details.
	 * @param context
	 * @param id the id of the assignment (the row in the database)
	 */
	public Assignment(Context context, long id) {
		mId = id;
		Cursor c = new DbAdapter(context, null, TABLE_NAME).open()
				.fetch(id, FETCH_DATA);
		mTitle = c.getString(c.getColumnIndexOrThrow(Values.KEY_TITLE));
		mCourseId = c.getShort(c.getColumnIndexOrThrow(KEY_COURSE));
		mDescription = c.getString(c.getColumnIndexOrThrow(Values.KEY_DESCRIPTION));
		mDueDate = c.getLong(c.getColumnIndexOrThrow(KEY_DUE_DATE));
		isCompleted = c.getShort(c.getColumnIndexOrThrow(KEY_STATUS)) > 0;
		c.close();
	}
	
	public Assignment(String title, short courseId, String description, long dueDate) {
		mTitle = title;
		mCourseId = courseId;
		mDescription = description;
		mDueDate = dueDate;
	}
	
	/**
	 * Get the id of the assignment.
	 * @return the id of the assignment
	 */
	public Long getId() {
		return mId;
	}
	
	/**
	 * Get the assignment's title.
	 * @return the title of the assignment
	 */
	public String getTitle() {
		return mTitle;
	}
	
	/**
	 * Get the assignment's associated course.
	 * @return the associated course's id
	 */
	public short getCourseId() {
		return mCourseId;
	}
	
	/**
	 * Get the assignment's description.
	 * @return the assignment's description
	 */
	public String getDescription() {
		return mDescription;
	}
	
	/**
	 * Get the assignment's due date.
	 * @return the due date (in milliseconds)
	 */
	public long getDueDate() {
		return mDueDate * 60000;
	}
	
	public boolean isCompleted() {
		return isCompleted;
	}
	
}
