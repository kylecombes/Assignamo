package com.acedit.assignamo.manage;

import java.util.NoSuchElementException;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.acedit.assignamo.database.DbAdapter;

public abstract class BaseListFragment extends ListFragment {

	protected Context mContext;
	
	protected DbAdapter dbAdapter;
	private Cursor cursor;
	private String[] from;
	private int[] to;
	private int listItem;
	
	private int listItemLayout;
	private String[] fetchSQL;
	private ViewBinder viewBinder;
	
	public BaseListFragment() {}
	
	public DbAdapter getDbAdapter(String tableName) {
		return new DbAdapter(mContext, null, tableName);
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
	}
	
	/** Called when the activity becomes visible. */
	public void onStart() {
		super.onStart();
		registerForContextMenu(getListView());
	}
	
	public void onResume() {
		super.onResume();
		fillData();
	}
	
	public void onPause() {
		super.onPause();
		cursor.close();
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (listItemLayout == 0)
			throw new NoSuchElementException("The resource id of the ListItem layout has not been set.");
		View v = inflater.inflate(listItemLayout, container, false);
		return v;
	}
	
	/** Set the XML file to use as a layout for the ListView. MUST be called
	 * before super.onCreateView().
	 * @param id the id for the XML layout resource
	 */
	protected void setListLayout(int id) {
		listItemLayout = id;
	}
	
	@Override
	public abstract void onListItemClick(ListView l, View v, int position, long id);	
	
	protected void setListFrom(String[] from) {
		this.from = from;
	}

	protected void setListTo(int[] to) {
		this.to = to;
	}
	
	protected void setListItem(int id) {
		listItem = id;
	}
	
	protected void setViewBinder(ViewBinder viewBinder) {
		this.viewBinder = viewBinder;
	}
	
    public void fillData() {
    	if (from.length == 0)
    		throw new IllegalStateException("from array not set.");
    	if (to.length == 0)
    		throw new IllegalStateException("to array not set.");
    	
    	dbAdapter.open();
    	cursor = dbAdapter.fetchAll(fetchSQL);
    	SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(mContext, listItem, cursor, from, to, 0);
    	if (viewBinder != null)
    		cursorAdapter.setViewBinder(viewBinder);
    	setListAdapter(cursorAdapter);
    }
    
    protected boolean delete(long rowId) {
    	dbAdapter.open();
    	return dbAdapter.delete(rowId);
    }
    
    @Override
    public abstract void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo);
    
    @Override
    public abstract boolean onContextItemSelected(MenuItem item);
    
}
