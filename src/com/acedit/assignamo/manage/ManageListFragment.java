package com.acedit.assignamo.manage;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

import com.acedit.assignamo.R;
import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.ui.ColorStrip;
import com.acedit.assignamo.utils.DateUtils;

public class ManageListFragment extends ListFragment {
	
	CustomCursorAdapter adapter;
	Cursor cursor;
	private Context context;
	
	private final String DATE_FORMAT = "c, MMMMM dd";
	private final static String KEY_POSITION = "pos";
	
	private static short position;
	private static final short POSITION_BOOKS = 0;
	private static final short POSITION_COURSES = 1;
	//private static final short POSITION_TEACHERS = 2;
	
	
	// Needed for recreating the fragment
	public ManageListFragment() {}
	
	/**
	 * Create an assignment list that shows all assignments.
	 */
	public ManageListFragment(Context context) {
		this.context = context;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_POSITION, position);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			position = savedInstanceState.getShort(KEY_POSITION);
		}
		setRetainInstance(true);
		
		// Need to give cursor a value -- null will make it not work
		updateAdapter();
		
		adapter = new CustomCursorAdapter(context, cursor, 0);
		setListAdapter(adapter);
	}

	/** Called when the activity becomes visible. */
	public void onStart() {
		super.onStart();
		registerForContextMenu(getListView());
	}

	public void onResume() {
		super.onResume();
		refresh();
	}

	public void onPause() {
		super.onPause();
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.assignment_list, container, false);
		setHasOptionsMenu(true);
		
		return v;
	}

	/**
	 * Updates the content in the adapter.
	 */
	public void updateAdapter() {
		
		switch (position) {
		case POSITION_BOOKS:
			
			break;
		case POSITION_COURSES:
			
			break;
		default:
			
		}
		
		// adapter is null when updateAdapter() is called in onCreate()
		if (adapter != null)
			adapter.changeCursor(cursor);
	}

	private void refresh() {
		updateAdapter();
		adapter.notifyDataSetChanged();
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Intent i;
		switch (this.position) {
		case POSITION_BOOKS:
			i = new Intent(context, BookViewFragment.class);
			break;
		case POSITION_COURSES:
			i = new Intent(context, CourseViewFragment.class);
			break;
		default:
			i = new Intent(context, TeacherViewFragment.class);
		}
		
		i.putExtra(Values.KEY_ROWID, id);
		startActivity(i);
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		
		MenuInflater inflater = new MenuInflater(context);
		
		switch (position) {
		case POSITION_BOOKS:
			inflater.inflate(R.menu.book_longpress, menu);
			break;
		case POSITION_COURSES:
			inflater.inflate(R.menu.course_longpress, menu);
			break;
		default:
			inflater.inflate(R.menu.teacher_context, menu);
		}
		
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		long rowId = ((AdapterContextMenuInfo) item.getMenuInfo()).id;
		
		switch (item.getItemId()) {
		
		case R.id.manage_context_menu_edit:
			Intent i;
			switch (position) {
			case POSITION_BOOKS:
				i = new Intent(context, BookEditActivity.class);
				break;
			case POSITION_COURSES:
				i = new Intent(context, CourseEditActivity.class);
				break;
			default:
				i = new Intent(context, TeacherEditActivity.class);
			}
			startActivity(i);
			return true;
			
		case R.id.manage_context_menu_delete:
			String table;
			switch (position) { // The table we are deleting from is the
									// only thing that differs.
			case POSITION_BOOKS:
				table = Values.BOOK_TABLE;
			case POSITION_COURSES:
				table = Values.COURSE_TABLE;
				break;
			default:
				table = Values.TEACHER_TABLE;	
			}
			DbAdapter db = new DbAdapter(context, null, table);
			db.open();
			db.delete(rowId);
			db.close();
			
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	/*---------- ListView Adapter and Related Subroutines ----------*/
	
	private class CustomCursorAdapter extends CursorAdapter {
		
		LayoutInflater mInflater;
		ViewHolder holder;
		int[] stripColors;
		int[] stripColorsLight;
		
		public CustomCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			this.mContext = context;
			
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@SuppressWarnings("static-access")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.assignment_list_item, parent, false);
				holder = new ViewHolder();
				holder.colorStrip = (ColorStrip) convertView.findViewById(R.id.assignment_list_color_strip);
				holder.titleLabel = (TextView) convertView.findViewById(R.id.list_title);
				holder.descriptionLabel = (TextView) convertView.findViewById(R.id.list_description);
				holder.dueLabel = (TextView) convertView.findViewById(R.id.list_due);
				
				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();
			
			cursor.moveToPosition(position);
			String title = cursor.getString(1);
			short course = cursor.getShort(2);
			String desc = cursor.getString(3);
			long due = cursor.getLong(4);
			
			holder.titleLabel.setText(title);
			holder.colorStrip.setColor(stripColors[course]);
			convertView.setBackgroundColor(stripColorsLight[course]);
			String dueString = getDateString(due);
			holder.dueLabel.setText(dueString);
			holder.descriptionLabel.setText(desc);
			
			return convertView;
		}

		@Override
		public void bindView(View arg0, Context arg1, Cursor arg2) {}

		@Override
		public View newView(Context arg0, Cursor arg1, ViewGroup arg2) { return null; }
		
	}
	
	private String getDateString(long minutes) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(DateUtils.convertMinutesToMills(minutes));
		
		return (new SimpleDateFormat(DATE_FORMAT)).format(calendar.getTime());
	}

	private static class ViewHolder {
		private static ColorStrip colorStrip;
		private static TextView titleLabel;
		private static TextView descriptionLabel;
		private static TextView dueLabel;
	}
	
}
