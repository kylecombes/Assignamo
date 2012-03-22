package com.awesomeapplets.assignamo.preferences;

import com.awesomeapplets.assignamo.R;
import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.Values;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class BookViewFragment extends ViewFragment {
		
	private TextView typeLabel;
	private TextView titleLabel;
	private TextView authorLabel;
	private TextView descriptionLabel;
	private TextView pagesLabel;
	private TextView chaptersLabel;
	private TextView ISBNLabel;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new DbAdapter(context, Values.DATABASE_NAME, Values.DATABASE_VERSION,
				Values.BOOK_TABLE, Values.DATABASE_CREATE, Values.KEY_ROWID);
		
		setContentView(R.layout.book_view_phone);
		typeLabel = (TextView)findViewById(R.id.book_view_type);
		titleLabel = (TextView)findViewById(R.id.book_view_title);
		authorLabel = (TextView)findViewById(R.id.book_view_author);
		descriptionLabel = (TextView)findViewById(R.id.book_view_description);
		pagesLabel = (TextView)findViewById(R.id.book_view_pages);
		chaptersLabel = (TextView)findViewById(R.id.book_view_chapters);
		ISBNLabel = (TextView)findViewById(R.id.book_view_ISBN);
		
		
		if (savedInstanceState != null)
			rowId = savedInstanceState.getLong(Values.KEY_ROWID);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		dbAdapter.open();
		setRowIdFromIntent();
		populateFields();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		dbAdapter.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.view_edit:
			Intent i = new Intent(context, BookEditActivity.class);
			i.putExtra(Values.KEY_ROWID, rowId);
			startActivity(i);
			break;
		case R.id.view_delete:
			dbAdapter.delete(rowId);
			finish();
		}
		return true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(Values.KEY_ROWID, rowId);
	}
	
	
	protected void populateFields() {
		Cursor cursor = dbAdapter.fetch(rowId,Values.BOOK_FETCH);
		startManagingCursor(cursor);
		
		String[] types = getResources().getStringArray(R.array.book_types);
		titleLabel.setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_TITLE)));
		typeLabel.setText(types[cursor.getShort(cursor.getColumnIndexOrThrow(Values.BOOK_KEY_TYPE))]);
		authorLabel.setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.BOOK_KEY_AUTHOR)));
		descriptionLabel.setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_DESCRIPTION)));
		chaptersLabel.setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.BOOK_KEY_CHAPTERS))
				+ " " + getString(R.string.add_book_chapters).toLowerCase());
		pagesLabel.setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.BOOK_KEY_PAGES))
				+ " " + getString(R.string.add_book_pages).toLowerCase());
		String ISBN = parseISBN(cursor.getLong(cursor.getColumnIndexOrThrow(Values.BOOK_KEY_ISBN)));
		ISBNLabel.setText(getString(R.string.add_book_ISBN) + ": " + ISBN);
		
	}
	
	private String parseISBN(long in) {
		String ISBN = "" + in;
		String newISBN = "";
		if (ISBN.length() == 9)
			ISBN = "0" + ISBN;
		
		if (ISBN.length() == 10) {
			
			Log.d("parseString", "ISBN length is 10");
			short loc = 0;
			newISBN += ISBN.charAt(loc);
			loc++;
			newISBN += "-";
			for (; loc < 4; loc++)
				newISBN += ISBN.charAt(loc);
			newISBN += "-";
			for (; loc < 9; loc++)
				newISBN += ISBN.charAt(loc);
			newISBN += "-";
			newISBN += ISBN.charAt(loc);
			
		} else if (ISBN.length() == 13) {
			
			short loc = 0;
			for (; loc < 3; loc++)
				newISBN += ISBN.charAt(loc);
			newISBN += "-" + ISBN.charAt(loc) + "-";
			loc++;
			for (; loc < 7; loc++)
				newISBN += ISBN.charAt(loc);
			newISBN += "-";
			for (; loc < 12; loc++)
				newISBN += ISBN.charAt(loc);
			newISBN += "-" + ISBN.charAt(loc);
			
		} else
			throw new NumberFormatException("Invalid ISBN.");
		return newISBN;
	}
	
}
