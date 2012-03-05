package com.awesomeapplets.assignamo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

public class DbAdapter {
	
	private String DATABASE_NAME;
	private String DATABASE_TABLE;
	private short DATABASE_VERSION;
	public String KEY_ROWID;
	private String[] DATABASE_CREATE;
	
	protected SQLiteDatabase db;
	private DatabaseHelper dbHelper;
	private final Context context;
	
	
	/**
	 * Creates a new database adapter.
	 * @param context
	 * @param databaseName the name of the database
	 * @param tableName the name of the table in the database
	 * @param databaseVersion the version of the database
	 */
	public DbAdapter(Context context, String databaseName, short databaseVersion, String tableName, String databaseCreate[], String rowId) {
		this.context = context;
		DATABASE_NAME = databaseName;
		DATABASE_VERSION = databaseVersion;
		DATABASE_TABLE = tableName;
		if (databaseCreate.length > 0)
			DATABASE_CREATE = databaseCreate;
		KEY_ROWID = rowId;
	}
	
	/**
	 * Opens the database.
	 * @return
	 * @throws SQLException if there was a problem
	 */
	public DbAdapter open() throws SQLException {
		dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
		return this;
	}
	
	/**
	 * Closes the database.
	 */
	public void close() {
		if (db != null) {
			try {
				db.close();
				dbHelper.close();
			} catch (NullPointerException e) {
				Log.e("Close", "Error: " + e + " " + e.getMessage());
			}
		} else
			Log.e("Close", "Error! db \"" + DATABASE_TABLE + "\" is null.");
	}
	
	public long add(ContentValues values) {
		return db.insert(DATABASE_TABLE, null, values);
	}
	
	/**
	 * Finds and returns a Cursor object containing the data found in the specified row.
	 * @param rowId The number of the row to read.
	 * @param query the columns to query in the table
	 * @return the data found in the row
	 * @throws SQLException
	 */
	public Cursor fetch(long rowId, String[] query) throws SQLException {
		Cursor cursor = db.query(true, DATABASE_TABLE, query,
				KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}
	
	public Cursor fetchAllWhere(String[] query, String where) throws SQLException{
		Cursor cursor = db.query(true, DATABASE_TABLE, query,
				where, null, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}
	
	/**
	 * Returns all items in the table.
	 * @return Contains all the items in the table.
	 */
	public Cursor fetchAll(String[] query) {
		Cursor c = db.query(DATABASE_TABLE, query, null, null, null, null, null);
		if (c != null)
			c.moveToFirst();
		return c;
	}
	
	public boolean update(long rowId, ContentValues values) {
		return db.update(DATABASE_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	/**
	 * Deletes a row in the table.
	 * @param rowId The row to be deleted.
	 * @return Whether or not the deletion was successful.
	 */
	public boolean delete(long rowId) {
		return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	public class DatabaseHelper extends SQLiteOpenHelper {
		
		String[] tables;
		
		/**
		 * Creates a new DatabaseHelper
		 * @param context
		 * @param databaseVersion The version of the database
		 * @param databaseCreateString The SQL CREATE argument.
		 */
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d("onCreate", "onCreate() called.");
			if (DATABASE_CREATE != null && DATABASE_CREATE.length > 0)
				for (String table : DATABASE_CREATE)
					db.execSQL(table);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Not used, but you could upgrade the database with ALTER
				// Scripts
		}
	}
}
