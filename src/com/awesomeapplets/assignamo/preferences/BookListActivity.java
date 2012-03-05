package com.awesomeapplets.assignamo.preferences;

import com.awesomeapplets.assignamo.MainActivity;
import com.awesomeapplets.assignamo.R;
import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.Values;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class BookListActivity extends ListActivity {
	
	private DbAdapter bookDb;
	
	public BookListActivity() {
		
	}
	
	/** Called when the activity becomes visible. */
	public void onStart() {
		super.onStart();
		registerForContextMenu(getListView());
	}
	
	public void onResume() {
		super.onResume();
		//context = getActivity();
		openDatabase();
		fillData();
	}
	
	public void onPause() {
		super.onPause();
		closeDatabase();
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstaceState) {
		View v = inflater.inflate(R.layout.book_list, container, false);
		//setHasOptionsMenu(true);
		return v;
	}
	
	public void openDatabase() {
		if (bookDb == null)
			bookDb = new DbAdapter(this, MainActivity.DATABASE_NAME, MainActivity.DATABASE_VERSION,
					Values.BOOK_TABLE, new String[0], MainActivity.KEY_ROWID);
		bookDb.open();
	}
	
	public void closeDatabase() {
		if (bookDb != null)
			bookDb.close();
	}
	
    public void fillData() {
    	Cursor booksCursor = bookDb.fetchAll(new String[] { MainActivity.KEY_ROWID, Values.KEY_TITLE,
    			Values.KEY_DESCRIPTION});
    	startManagingCursor(booksCursor);
    	
    	// Create and array to specify the fields we want (only the TITLE)
    	String[] from = new String[] {Values.KEY_TITLE, Values.KEY_DESCRIPTION};
    	
    	// and an array of the fields we want to bind in the view
    	int[] to = new int[]{R.id.book_list_title, R.id.book_list_description};
    	
    	// Now create a simple cursor adapter and set it to display
    	SimpleCursorAdapter reminders = new SimpleCursorAdapter(this, R.layout.book_list_item, booksCursor, from, to);
    	setListAdapter(reminders);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, BookViewFragment.class);
		i.putExtra(MainActivity.KEY_ROWID, id);
		startActivity(i);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.book_menu, menu);
		return true;
	}
	
    // TODO
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case R.id.book_menu_add:
			Intent i = new Intent(this, BookEditFragment.class);
			startActivity(i);
			return true;
		}
    	return true;
    }
    
    // TODO
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	//super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.book_longpress, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.book_context_menu_delete:
			// Delete the book
			bookDb.delete(info.id);
			fillData();
			return true;
		case R.id.book_context_menu_edit:
			Intent i = new Intent(this, BookEditFragment.class);
			i.putExtra(MainActivity.KEY_ROWID, info.id);
			startActivity(i);
			return true;
		}
		return super.onContextItemSelected(item);
    }
}
