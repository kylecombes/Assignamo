package com.awesomeapplets.assignamo.database;

import android.content.Context;
import android.database.Cursor;

import com.awesomeapplets.assignamo.*;

public class DbUtils {
	
	/**
	 * Get the number of courses stored in the database.
	 * @param context the application context (needed to query the database)
	 * @return the number of courses
	 */
	public static int getCourseCount(Context context) {
		Cursor courseCursor = queryCourseTable(context,
				new String[] { MainActivity.KEY_ROWID});
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
				new String[] {MainActivity.KEY_ROWID, Values.KEY_NAME});
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
				new String[] {MainActivity.KEY_ROWID, Values.KEY_NAME});
	}
	
	private static Cursor queryCourseTable(Context context, String[] query) {
		return queryTable(context, Values.COURSE_TABLE, query);
	}
	
	public static String[] getTeachers(Context context) {
		Cursor c = queryTeacherTable(context,
				new String[] {MainActivity.KEY_ROWID, Values.KEY_NAME});
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
				MainActivity.DATABASE_NAME,
				MainActivity.DATABASE_VERSION,
				table,
				MainActivity.DATABASE_CREATE,
				MainActivity.KEY_ROWID);
		adapter.open();
		Cursor c = adapter.fetchAll(query);
		adapter.close();
		return c;
	}
	
	
	
}
