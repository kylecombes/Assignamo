package com.acedit.assignamo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static DatabaseHelper mInstance = null;
		
	public static DatabaseHelper getInstance(Context context) {
		if (mInstance == null)
			mInstance = new DatabaseHelper(context.getApplicationContext());
		return mInstance;
	}
	
	private DatabaseHelper(Context context) {
		super(context, Values.DATABASE_NAME, null, Values.DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		if (Values.DATABASE_CREATE != null && Values.DATABASE_CREATE.length > 0)
			for (String table : Values.DATABASE_CREATE)
				db.execSQL(table);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
}