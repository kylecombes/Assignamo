package com.acedit.assignamo.manage;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.acedit.assignamo.R;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.utils.DbUtils;

public class CourseListFragment extends BaseListFragment {
	
	Map<Short,String> teachers = new HashMap<Short,String>();
	
	public CourseListFragment() {}
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = getDbAdapter(Values.COURSE_TABLE);
		setListFrom( new String[] { Values.KEY_NAME, Values.COURSE_KEY_TEACHER,
				Values.COURSE_KEY_TIMES_OF_DAY } );
		setListTo( new int[] { R.id.list_title, R.id.list_teacher, R.id.list_days } );
		setListItem(R.layout.course_list_item);
		
		Context context = getActivity();
		// Get the list of teachers
		Cursor c = DbUtils.getTeachersAsCursor(context);
		c.moveToFirst();
		for (short i = 0; i < c.getCount(); i++) {
			teachers.put(c.getShort(0),c.getString(1));
			c.moveToNext();
		}
		
		setViewBinder(new ViewBinder() {
			
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				switch (view.getId()) {
				case R.id.list_teacher:
					((TextView)view).setText(teachers.get(cursor.getShort(cursor.getColumnIndexOrThrow(Values.COURSE_KEY_TEACHER))));
					return true;
				case R.id.list_days:
					((TextView)view).setText(getDaysOfWeek(cursor.getString(cursor.getColumnIndexOrThrow(Values.COURSE_KEY_TIMES_OF_DAY))));
					return true;
				}
				return false;
			}
		});
	}
	
	private String getDaysOfWeek(String strArray) {
		boolean [] days = new boolean[7];
		try {
			JSONArray array = new JSONArray(strArray);
			for (short x = 0; x < 14; x += 2)
				days[x/2] = (short)array.getInt(x) > 0;
		} catch (JSONException e) { e.printStackTrace(); }
		if (days.length == 0)
			return getString(R.string.course_no_days_set);
		else {
			String[] daysOfWeek = getResources().getStringArray(R.array.days_of_week_short);
			String rStr = "";
			for (short i = 0; i < 7; i++) {
				if (days[i])
					rStr += daysOfWeek[i] + ", ";
			}
			return rStr.substring(0, rStr.length() - 2);
		}
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.course_list, container, false);
		return v;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(context, CourseViewFragment.class);
		i.putExtra(Values.KEY_ROWID, id);
		startActivity(i);
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(R.string.course_add);
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// (The only choice is to add a course)
		Intent i = new Intent(context, CourseEditActivity.class);
		startActivity(i);
    	return true;
    }

    private static final int CONTEXT_EDIT = 2;
    private static final int CONTEXT_DELETE = 3;
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(0, CONTEXT_EDIT, 0, getString(R.string.course_edit));
		menu.add(0, CONTEXT_DELETE, 0, getString(R.string.course_delete));
    }
    
    public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    	switch (item.getItemId()) {
    	case CONTEXT_EDIT:
    		Intent i = new Intent(context, CourseEditActivity.class);
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
