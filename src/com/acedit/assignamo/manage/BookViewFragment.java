package com.acedit.assignamo.manage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.acedit.assignamo.R;
import com.acedit.assignamo.ViewFragment;
import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.utils.DbUtils;

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
		
		setContentView(R.layout.book_view);
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
	
	protected void reloadData() {
		DbAdapter dbAdapter = new DbAdapter(context, Values.DATABASE_NAME, Values.DATABASE_VERSION,
				Values.BOOK_TABLE, Values.DATABASE_CREATE, Values.KEY_ROWID);
		dbAdapter.open();
		cursor = dbAdapter.fetch(rowId,Values.BOOK_FETCH);
		dbAdapter.close();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setRowIdFromIntent();
		populateFields();
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
			DbUtils.deleteAssignment(context,rowId);
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
		if (ISBN != null)
			ISBNLabel.setText(getString(R.string.add_book_ISBN) + ": " + ISBN);
		
	}
	
	private String parseISBN(long in) {
		if (in == 0)
			return null;
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
