package com.acedit.assignamo.manage;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.acedit.assignamo.R;
import com.acedit.assignamo.database.Values;

public class BookListFragment extends BaseListFragment {
		
	public BookListFragment() {
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = getDbAdapter(Values.BOOK_TABLE);
		setListFrom( new String[] { Values.KEY_TITLE, Values.BOOK_KEY_AUTHOR, Values.KEY_DESCRIPTION } );
		setListTo( new int[] { R.id.list_title, R.id.list_author, R.id.list_description } );
		setListItem(R.layout.book_list_item);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstaceState) {
		return inflater.inflate(R.layout.book_list, container, false);
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(context, BookViewFragment.class);
		i.putExtra(Values.KEY_ROWID, id);
		startActivity(i);
	}
	
    private static final int CONTEXT_EDIT = 0;
    private static final int CONTEXT_DELETE = 1;
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(0, CONTEXT_EDIT, 0, getString(R.string.book_edit));
		menu.add(0, CONTEXT_DELETE, 0, getString(R.string.book_delete));
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		switch (item.getItemId()) {
		case CONTEXT_EDIT:
			Intent i = new Intent(context, BookEditActivity.class);
			i.putExtra(Values.KEY_ROWID, info.id);
			startActivity(i);
			return true;
		case CONTEXT_DELETE:
			delete(info.id);
			fillData();
			return true;
		}
		return false;
    }
}
