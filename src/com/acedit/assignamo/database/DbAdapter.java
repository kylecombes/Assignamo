package com.acedit.assignamo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DbAdapter {
	
	private String mDatabaseTable;
	protected SQLiteDatabase db;
	private final Context mContext;
	
	/**
	 * Creates a new database adapter.
	 * @param mContext
	 * @param databaseName the name of the database. Pass <b>null</b> to use the default database.
	 * @param tableName the name of the table in the database
	 */
	public DbAdapter(Context mContext, String databaseName, String tableName) {
		this.mContext = mContext;
		mDatabaseTable = tableName;
	}
	
	public DbAdapter open() throws SQLException {
		DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
		db = dbHelper.getWritableDatabase();
		return this;
	}
		
	public long add(ContentValues values) {
		return db.insert(mDatabaseTable, null, values);
	}

	public boolean update(long rowId, ContentValues values) {
		return db.update(mDatabaseTable, values, Values.KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	/**
	 * Deletes a row in the table.
	 * @param rowId The row to be deleted.
	 * @return Whether or not the deletion was successful.
	 */
	public boolean delete(long rowId) {
		return db.delete(mDatabaseTable, Values.KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	/**
	 * Delete all of the rows that match the WHERE clause.
	 * @param where The SQL WHERE clause.
	 * @return Whether or not the deletion was successful.
	 */
	public boolean deleteWhere(String where) {
		return db.delete(mDatabaseTable, where, null) > 0;
	}
	
	/**
	 * Finds and returns a Cursor object containing the data found in the specified row.
	 * @param rowId The number of the row to read.
	 * @param query the columns to query in the table
	 * @return the data found in the row
	 * @throws SQLException
	 */
	public Cursor fetch(long rowId, String[] query) throws SQLException {
		Cursor cursor = db.query(true, mDatabaseTable, query,
				Values.KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}

	/**
	 * Returns all items in the table.
	 * @return Contains all the items in the table.
	 */
	public Cursor fetchAll(String[] query) {
		Cursor c = db.query(mDatabaseTable, query, null, null, null, null, null);
		if (c != null)
			c.moveToFirst();
		return c;
	}
	
	/**
	 * Get all the items in a database sorted in a specific order.
	 * @param query the SQL QUERY clause.
	 * @param where a SQL WHERE clause. Pass null to return all the
	 * items in the table.
	 * @param orderBy the column which the results will be sorted by.
	 * @return
	 */
	public Cursor fetchAllOrdered(String[] query, String where, String orderBy) {
		Cursor c;
		c = db.query(mDatabaseTable, query, where, null, null, null, orderBy);
		if (c != null)
			c.moveToFirst();
		return c;
	}
	
	public Cursor fetchAllWhere(String[] query, String where, String orderBy) throws SQLException {
		Cursor cursor = db.query(true, mDatabaseTable, query,
				where, null, null, null, orderBy, null);
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}
	
	/**
	 * Set the table the adapter is accessing.
	 * @param newTable The name of the new table.
	 */
	public void setTable(String newTable) {
		mDatabaseTable = newTable;
	}
	
}
