package com.awesomeapplets.assignamo.database;

// Variable names used by the database

public final class Values {
	
	public static final String DATABASE_NAME = "data.db";
	public static final short DATABASE_VERSION = 1;
	public static final String[] DATABASE_CREATE = {Values.ASSIGNMENT_DATABASE_CREATE,
		Values.COURSE_DATABASE_CREATE, Values.BOOK_DATABASE_CREATE, Values.TEACHER_DATABASE_CREATE };
	
	/*--------- Shared Variable Values ---------*/
	public static final String KEY_ROWID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_NAME = "name";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_NOTES = "notes";
	public static final String KEY_ROOM = "room";
	
	/*--------- Assignments --------*/
	public static final String ASSIGNMENT_TABLE = "assignments";
	public static final String ASSIGNMENT_KEY_COURSE = "course";
	public static final String ASSIGNMENT_KEY_DUE_DATE = "due_date";
	public static final String ASSIGNMENT_KEY_POINTS = "points";
	public static final String ASSIGNMENT_KEY_STATUS = "status";
	public static final short ASSIGNMENT_STATUS_INCOMPLETE = 0;
	public static final short ASSIGNMENT_STATUS_COMPLETED = 1;
	public static final String[] ASSIGNMENT_FETCH = new String[] { KEY_ROWID, KEY_TITLE,
		ASSIGNMENT_KEY_COURSE, KEY_DESCRIPTION, ASSIGNMENT_KEY_DUE_DATE, ASSIGNMENT_KEY_POINTS, ASSIGNMENT_KEY_STATUS };
	public static final String ASSIGNMENT_DATABASE_CREATE = "create table " + ASSIGNMENT_TABLE
			+ " ( " + KEY_ROWID + " integer primary key autoincrement, "
			+ KEY_TITLE + " text not null, "
			+ ASSIGNMENT_KEY_COURSE + " int not null, "
			+ KEY_DESCRIPTION + " text not null, "
			+ ASSIGNMENT_KEY_DUE_DATE + " text not null, "
			+ ASSIGNMENT_KEY_POINTS + " int, "
			+ ASSIGNMENT_KEY_STATUS + " int not null default " + ASSIGNMENT_STATUS_INCOMPLETE + ");";
	public static final String ASSIGNMENT_KEY_SHOWING_COMPLETED = "showing_completed";
	
	/*--------- Courses ---------*/
	public static final String COURSE_TABLE = "courses";
	public static final String COURSE_KEY_TEACHER = "teacher";
	public static final String COURSE_KEY_DAYS_OF_WEEK = "days_of_week";
	public static final String COURSE_KEY_TIMES_OF_DAY = "times_of_day";
	public static final String COURSE_KEY_CREDIT_HOURS = "credit_hours";
	public static final String[] COURSE_FETCH = {KEY_ROWID, KEY_NAME,
		COURSE_KEY_TEACHER, KEY_DESCRIPTION, KEY_ROOM, COURSE_KEY_DAYS_OF_WEEK,
		COURSE_KEY_TIMES_OF_DAY, COURSE_KEY_CREDIT_HOURS};
	public static final String COURSE_DATABASE_CREATE = "create table " + COURSE_TABLE + " ( "
			+ KEY_ROWID + " integer primary key autoincrement, "
			+ KEY_NAME + " text not null, "
			+ COURSE_KEY_TEACHER + " text not null, "
			+ KEY_DESCRIPTION + " text, "
			+ KEY_ROOM + " int, "
			+ COURSE_KEY_DAYS_OF_WEEK + " int not null, "
			+ COURSE_KEY_TIMES_OF_DAY + " text, "
			+ COURSE_KEY_CREDIT_HOURS + " int);";
	
	/*--------- Books ---------- */
	public static final String BOOK_TABLE = "books";
	public static final String BOOK_KEY_AUTHOR = "author";
	public static final String BOOK_KEY_TYPE = "type";
	public static final String BOOK_KEY_PAGES = "pages";
	public static final String BOOK_KEY_CHAPTERS = "chapters";
	public static final String BOOK_KEY_ISBN = "ISBN";
	public static final String[] BOOK_FETCH = {KEY_ROWID, KEY_TITLE,
		BOOK_KEY_AUTHOR, KEY_DESCRIPTION, BOOK_KEY_TYPE, BOOK_KEY_CHAPTERS,
		BOOK_KEY_PAGES, BOOK_KEY_ISBN};
	public static final String BOOK_DATABASE_CREATE = "create table " + BOOK_TABLE + " ( "
			+ KEY_ROWID + " integer primary key autoincrement, "
			+ KEY_TITLE + " text not null, "
			+ BOOK_KEY_AUTHOR + " text not null, "
			+ KEY_DESCRIPTION + " text, "
			+ BOOK_KEY_TYPE + " text not null, "
			+ BOOK_KEY_CHAPTERS + " int not null, "
			+ BOOK_KEY_PAGES + " int not null, "
			+ BOOK_KEY_ISBN + " text);";
	
	/*-------- Teachers ----------*/
	public static final String TEACHER_TABLE = "teachers";
	public static final String TEACHER_KEY_SUBJECT = "subject";
	public static final String TEACHER_KEY_EMAIL = "email";
	public static final String TEACHER_KEY_PHONE = "phone_number";
	public static final String[] TEACHER_FETCH = { KEY_ROWID, KEY_NAME,
		TEACHER_KEY_SUBJECT, KEY_ROOM, TEACHER_KEY_EMAIL, TEACHER_KEY_PHONE, KEY_NOTES };
	public static final String TEACHER_DATABASE_CREATE = "create table " + TEACHER_TABLE + " ( "
			+ KEY_ROWID + " integer primary key autoincrement, "
			+ KEY_NAME + " text not null, "
			+ TEACHER_KEY_SUBJECT + " text, "
			+ KEY_ROOM + " int, "
			+ TEACHER_KEY_EMAIL + " text, "
			+ TEACHER_KEY_PHONE + " int, "
			+ KEY_NOTES + " text);";
	
}
