package com.awesomeapplets.assignamo.utils;

import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.Values;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class DbUtils {
	
	/*---------- Assignments ----------*/
	

	/**
	 * Returns all assignments.
	 * @param query the SQL query syntax.
	 * @param order whether or not to sort by due date.
	 * @return Contains all the items in the table.
	 */
	public static Cursor fetchAllAssignments(Context context, String[] query, Short course, boolean order) {
		Cursor c;
		DbAdapter adapter = new DbAdapter(context,
				Values.DATABASE_NAME,
				Values.DATABASE_VERSION,
				Values.ASSIGNMENT_TABLE,
				Values.DATABASE_CREATE,
				Values.KEY_ROWID);
		adapter.open();
		
		if (order)
			if (course == null)
				c = adapter.fetchAll(query);
			else
				c = adapter.fetchAllWhere(query, Values.ASSIGNMENT_KEY_COURSE + "=" + course);
		else
			if (course == null)
				c = adapter.fetchAllOrdered(query, null, Values.ASSIGNMENT_KEY_DUE_DATE);
			else
				c = adapter.fetchAllOrdered(query, Values.ASSIGNMENT_KEY_COURSE + "=" + course, Values.ASSIGNMENT_KEY_DUE_DATE);
		
		if (c != null)
			c.moveToFirst();
		return c;
	}

	/**
	 * Returns all assignments in the specified course.
	 * @param query the SQL query syntax.
	 * @param order whether or not to sort by due date.
	 * @return Contains all the items in the table.
	 *
	public Cursor fetchAllAssignments(Context context, String[] query, short course, boolean order) {
		Cursor c;
		DbAdapter adapter = new DbAdapter(context,
				Values.DATABASE_NAME,
				Values.DATABASE_VERSION,
				Values.ASSIGNMENT_TABLE,
				Values.DATABASE_CREATE,
				Values.KEY_ROWID);
		adapter.fetchAll(query);
		
		if (order)
			c = db.query(Values.ASSIGNMENT_TABLE, query, Values.ASSIGNMENT_KEY_COURSE + "=" + course, null, null, null, Values.ASSIGNMENT_KEY_DUE_DATE);
		else
			c = db.query(Values.ASSIGNMENT_TABLE, query, Values.ASSIGNMENT_KEY_COURSE + "=" + course, null, null, null, null);
		if (c != null)
			c.moveToFirst();
		return c;
	}
	
	/**
	 * Get all the incomplete assignments for a certain course.
	 * @param context a copy of the application context.
	 * @param query the SQL query syntax.
	 * @param course the course to fetch the assignments from. Pass null
	 * to return assignments from all courses.
	 * @param order whether or not to sort the result by due date.
	 * @return all the incomplete assignments for the specified course.
	 */
	public static Cursor fetchIncompleteAssignments(Context context, String[] query, Short course, boolean order) {
		DbAdapter adapter = new DbAdapter(context,
				Values.DATABASE_NAME,
				Values.DATABASE_VERSION,
				Values.ASSIGNMENT_TABLE,
				Values.DATABASE_CREATE,
				Values.KEY_ROWID);
		adapter.open();
		
		if (course != null) // Fetching from all courses
			return adapter.fetchAllWhere(query, Values.ASSIGNMENT_KEY_COURSE + "=" + course
				+ " AND " + Values.ASSIGNMENT_KEY_STATUS + "=" + 0);
		else
			return adapter.fetchAllWhere(query, Values.ASSIGNMENT_KEY_STATUS + "=" + 0);
	}
	
	
	/*---------- Courses ----------*/
	
	/**
	 * Get the number of courses stored in the database.
	 * @param context the application context (needed to query the database)
	 * @return the number of courses
	 */
	public static int getCourseCount(Context context) {
		Cursor courseCursor = queryCourseTable(context,
				new String[] { Values.KEY_ROWID});
		int count = courseCursor.getCount();
		courseCursor.close();
		return count;
	}
	
	/**
	 * Get the courses in the database.
	 * @param context the application context (needed to query the database)
	 * @return contains the courses
	 */
	public static String[] getCoursesAsArray(Context context) {
		Cursor c = queryCourseTable(context,
				new String[] {Values.KEY_ROWID, Values.KEY_NAME});
    	short courseNum = (short)c.getCount();
    	String [] courseArray = new String[courseNum];
    	for (short i = 0; i < courseNum; i++) {
    		c.moveToPosition(i);
    		courseArray[i] = c.getString(1);
    	}
    	c.close();
    	return courseArray;
	}
	
	public static Cursor getCoursesAsCursor(Context context) {
		return queryCourseTable(context,
				new String[] {Values.KEY_ROWID, Values.KEY_NAME});
	}
	
	private static Cursor queryCourseTable(Context context, String[] query) {
		return queryTable(context, Values.COURSE_TABLE, query);
	}
	
	public static Cursor getTeachers(Context context) {
		return queryTeacherTable(context,
				new String[] {Values.KEY_ROWID, Values.KEY_NAME});
	}

	public static String[] getTeachersAsArray(Context context) {
		Cursor c = queryTeacherTable(context,
				new String[] {Values.KEY_ROWID, Values.KEY_NAME});
		short teacherNum = (short)c.getCount();
    	String [] teacherArray = new String[teacherNum+1];
    	for (short i = 0; i < teacherNum; i++) {
    		c.moveToPosition(i);
    		teacherArray[i] = c.getString(1);
    	}
    	c.close();
    	return teacherArray;
	}
	
	private static Cursor queryTeacherTable(Context context, String[] query) {
		return queryTable(context, Values.TEACHER_TABLE, query);
	}
	
	private static Cursor queryTable(Context context, String table, String[] query) {
		DbAdapter adapter = new DbAdapter(context,
				Values.DATABASE_NAME,
				Values.DATABASE_VERSION,
				table,
				Values.DATABASE_CREATE,
				Values.KEY_ROWID);
		adapter.open();
		Cursor c = adapter.fetchAll(query);
		adapter.close();
		return c;
	}
	
	public static boolean isAssignmentCompleted(Context context, long id) {
		DbAdapter adapter = new DbAdapter(context,
				Values.DATABASE_NAME,
				Values.DATABASE_VERSION,
				Values.ASSIGNMENT_TABLE,
				Values.DATABASE_CREATE,
				Values.KEY_ROWID);
		adapter.open();
		Cursor c = adapter.fetch(id, new String[] {Values.ASSIGNMENT_KEY_STATUS});
		adapter.close();
		boolean b = c.getShort(0) == Values.ASSIGNMENT_STATUS_COMPLETED;
		c.close();
		return b;
	}
	
	public static void setAssignmentState(Context context, long id, boolean completed) {
		DbAdapter adapter = new DbAdapter(context,
				Values.DATABASE_NAME,
				Values.DATABASE_VERSION,
				Values.ASSIGNMENT_TABLE,
				Values.DATABASE_CREATE,
				Values.KEY_ROWID);
		adapter.open();
		ContentValues newValue = new ContentValues();
		newValue.put(Values.ASSIGNMENT_KEY_STATUS, completed);
		adapter.update(id, newValue);
		adapter.close();
	}
	
}
