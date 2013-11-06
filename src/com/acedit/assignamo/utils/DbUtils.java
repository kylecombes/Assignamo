package com.acedit.assignamo.utils;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.objects.Assignment;
import com.acedit.assignamo.objects.Course;
import com.acedit.assignamo.objects.Teacher;

public class DbUtils {
	
	/*---------- Shared ----------*/

	public static short getPositionFromRowID(Cursor c, short rowId) {
		c.moveToFirst();
		for (short i = 0; i < c.getCount(); i++) {
			c.moveToPosition(i);
			if (c.getShort(0) == rowId)
				return i;
		}
		c.close();
		return -1;
	}
	
	public static boolean deleteItem(Context context, String table, long rowId) {
		DbAdapter adapter = new DbAdapter(context, null, table).open();
		return adapter.delete(rowId);
	}
	
	/*---------- Assignments ----------*/
	
	
	
	public synchronized static Cursor fetchAllAssignments(Context context, Short course) {
		DbAdapter adapter = new DbAdapter(context, null, Assignment.TABLE_NAME).open();
		
		if (course == null)
			return adapter.fetchAllOrdered(Assignment.LIST_FETCH_DATA, null, Assignment.KEY_DUE_DATE);
		else
			return adapter.fetchAllOrdered(Assignment.LIST_FETCH_DATA, Assignment.KEY_COURSE + "=" + course, Assignment.KEY_DUE_DATE);
	}
	
	public synchronized static Cursor fetchIncompleteAssignments(Context context, Short course) {
		DbAdapter adapter = new DbAdapter(context, null, Assignment.TABLE_NAME).open();
		
		if (course == null) // Fetching from all courses
			return adapter.fetchAllWhere(Assignment.LIST_FETCH_DATA, Assignment.KEY_STATUS + "=" + 0, Assignment.KEY_DUE_DATE);
		else
			return adapter.fetchAllWhere(Assignment.LIST_FETCH_DATA, Assignment.KEY_COURSE + "=" + course
				+ " AND " + Assignment.KEY_STATUS + "=" + 0, Assignment.KEY_DUE_DATE);
	}
	
	
	public static boolean isAssignmentCompleted(Context context, long id) {
		DbAdapter adapter = new DbAdapter(context, null, Assignment.TABLE_NAME).open();
		Cursor c = adapter.fetch(id, new String[] {Assignment.KEY_STATUS});
		boolean b = c.getShort(0) == Assignment.STATUS_COMPLETED;
		c.close();
		return b;
	}
	
	public static void setAssignmentCompletionStatus(Context context, long id, boolean completed) {
		DbAdapter adapter = new DbAdapter(context, null, Assignment.TABLE_NAME).open();
		ContentValues newValue = new ContentValues();
		newValue.put(Assignment.KEY_STATUS, completed);
		adapter.update(id, newValue);
	}
	
	
	/*---------- Courses ----------*/

	/**
	 * Deletes the specified course AND all of the assignments for that course
	 * @param context
	 * @param rowId The id of the course.
	 * @return Whether or not the deletion was successful.
	 */
	public static boolean deleteCourse(Context context, long rowId) {
		DbAdapter adapter = new DbAdapter(context, null, Course.TABLE_NAME).open();
		boolean b = adapter.delete(rowId);
		adapter.setTable(Assignment.TABLE_NAME);
		return b && adapter.deleteWhere(Assignment.KEY_COURSE + "=" + rowId);
	}
	
	/**
	 * Get the number of courses stored in the database.
	 * @param context the application context (needed to query the database)
	 * @return the number of courses
	 */
	public static int getCourseCount(Context context) {
		Cursor courseCursor = queryTable(context, Course.TABLE_NAME,
				new String[] { Values.KEY_ROWID});
		int count = courseCursor.getCount();
		courseCursor.close();
		return count;
	}
	
	public static Cursor getCoursesAsCursor(Context context) {
		return queryTable(context, Course.TABLE_NAME,	new String[] {Values.KEY_ROWID, Values.KEY_NAME});
	}
	
	/**
	 * Get the courses in the database.
	 * @param context the application context (needed to query the database)
	 * @return contains the courses
	 */
	public static String[] getCoursesAsArray(Context context) {
		Cursor c = getCoursesAsCursor(context);
    	short courseNum = (short)c.getCount();
    	String [] courseArray = new String[courseNum];
    	for (short i = 0; i < courseNum; i++) {
    		c.moveToPosition(i);
    		courseArray[i] = c.getString(1);
    	}
    	c.close();
    	return courseArray;
	}
	
	public static short[] getCourseIds(Context context) {
		Cursor c = queryTable(context, Course.TABLE_NAME,	new String[] {Values.KEY_ROWID});
		short[] ids = new short[c.getCount()];
		c.moveToFirst();
		for (short i = 0; i < c.getCount(); i++) {
			ids[i] = c.getShort(0);
			c.moveToNext();
		}
		c.close();
		return ids;
	}
	
	public static String getCourseNameFromId(Context context, long courseId) {
		Map<Short,String> map = getCourseNames(context);
		String str = (String)map.get((short)courseId);
		return str;
	}
	
	public static Map<Short,String> getCourseNames(Context context) {
		Cursor c = queryTable(context, Course.TABLE_NAME,
				new String[] {Values.KEY_ROWID, Values.KEY_NAME});
		Map<Short,String> map = new HashMap<Short,String>();
		c.moveToFirst();
		for (short i = 0; i < c.getCount(); i++) {
			map.put(c.getShort(0), c.getString(1));
			c.moveToNext();
		}
		c.close();
		return map;
	}
	
	/* ---------- Teachers ----------*/
	
	public static boolean deleteTeacher(Context context, long rowId) {
		DbAdapter adapter = new DbAdapter(context, null, Teacher.TABLE_NAME).open();
		return adapter.delete(rowId);
	}
	
	public static Cursor getTeachersAsCursor(Context context) {
		return queryTable(context, Teacher.TABLE_NAME, new String[] {Values.KEY_ROWID, Values.KEY_NAME});
	}
	
	public static String[] getTeachersAsArray(Context context) {
		Cursor c = getTeachersAsCursor(context);
		short teacherNum = (short)c.getCount();
    	String [] teacherArray = new String[teacherNum+1];
    	for (short i = 0; i < teacherNum; i++) {
    		c.moveToPosition(i);
    		teacherArray[i] = c.getString(1);
    	}
    	c.close();
    	return teacherArray;
	}
	
	/**
	 * Get a list of the teachers names and their corresponding IDs.
	 * @param context
	 * @return A list of the teachers: <b>key</b> is the ID, <b>value</b> is their name.
	 */
	public static Map<Short,String> getTeacherNames(Context context) {
		Cursor c = getTeachersAsCursor(context);
		Map<Short,String> map = new HashMap<Short,String>();
		c.moveToFirst();
		for (short i = 0; i < c.getCount(); i++) {
			map.put(c.getShort(0), c.getString(1));
			c.moveToNext();
		}
		c.close();
		return map;
	}

	private static Cursor queryTable(Context context, String table, String[] query) {
		DbAdapter adapter = new DbAdapter(context, null, table);
		adapter.open();
		return adapter.fetchAll(query);
	}
	
}
