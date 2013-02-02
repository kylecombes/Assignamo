package com.acedit.assignamo.database;

import com.acedit.assignamo.objects.*;

/** Values used by the database */

public final class Values {
	
	public static final String DATABASE_NAME = "data.db";
	public static final short DATABASE_VERSION = 1;
	public static final String[] DATABASE_CREATE = {Assignment.DATABASE_CREATE,
		Course.DATABASE_CREATE, Teacher.DATABASE_CREATE };
	public static final String INTENT_REFRESH_KEY = "refresh_id";
	public static final String INTENT_REFRESH_COURSE_KEY = "refresh_course_id";
	public static final String INTENT_REFRESH_ACTION = "assignamo.REFRESH";
	public static final String PREFS_DB_INITIALIZED = "db_initialized";
	
	/*--------- Shared Variable Values ---------*/
	public static final String KEY_ROWID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_NAME = "name";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_NOTES = "notes";
	public static final String KEY_ROOM = "room";	
	
}
