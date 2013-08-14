package com.acedit.assignamo.objects;

import java.util.Calendar;

import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;

import android.content.ContentValues;
import android.content.Context;

public class AssignmentEditor extends Assignment {
	
	private Context mContext;
	
	public AssignmentEditor(Context context) {
		mContext = context;
	}
	
	public AssignmentEditor(Context context, long id) {
		super(context, id);
	}
	
	/**
	 * Check to see whether or not we are creating a new assignment or
	 * editing an existing assignment.
	 * @return whether or not we are editing an existing assignment
	 */
	public boolean editingExisting() {
		return mId != null;
	}

	/**
	 * Set the assignment's title.
	 * @param title the new title
	 */
	public void setTitle(String title) {
		mTitle = title;
	}
	
	/**
	 * Set the course the assignment is for.
	 * @param id the id of the course
	 */
	public void setCourseId(short id) {
		mCourseId = id;
	}
	
	/**
	 * Set the assignment's description.
	 * @param description the new description
	 */
	public void setDescription(String description) {
		mDescription = description;
	}
	
	/**
	 * Set the assignment's due date.
	 * @param dueDate the new due date (in milliseconds)
	 */
	public void setDueDate(long dueDate) {
		mDueDate = dueDate / 60000;
	}
	
	/**
	 * Set the assignment's due date.
	 * @param dueDate the new due date
	 */
	public void setDueDate(Calendar dueDate) {
		mDueDate = dueDate.getTimeInMillis() / 60000;
	}
	
	/**
	 * Commits the assignment to the database. If the assignment already exists,
	 * it will update it. If it is new, it will add a new assignment.
	 * @param context
	 * @return whether or not the commit was successful
	 */
	public boolean commitToDatabase() {
		ContentValues values = new ContentValues();
    	values.put(Values.KEY_TITLE, mTitle);
    	values.put(Assignment.KEY_COURSE, mCourseId);
    	values.put(Values.KEY_DESCRIPTION, mDescription);
    	values.put(Assignment.KEY_DUE_DATE, mDueDate);
    	DbAdapter dbAdapter = new DbAdapter(mContext, null, TABLE_NAME).open();
		if (mId == null)
			return dbAdapter.add(values) > 0;
		return dbAdapter.update(mId, values);
	}
	
	
}
