package com.acedit.assignamo.manage;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.acedit.assignamo.AssignmentListFragment;
import com.acedit.assignamo.R;
import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.objects.Course;
import com.acedit.assignamo.utils.DbUtils;
import com.acedit.assignamo.utils.UiUtils;

public class CourseListFragment extends BaseListFragment {
	
	Map<Short,String> teachers = new HashMap<Short,String>();
	Map<Short,Integer> colors = new HashMap<Short,Integer>(); // Colors are light
	
	private long selectedItem;
	
	public CourseListFragment() {}
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = getDbAdapter(Course.TABLE_NAME);
		setListFrom( new String[] { Values.KEY_NAME, Course.KEY_TEACHER,
				Course.KEY_TIMES_OF_DAY } );
		setListTo( new int[] { R.id.list_title, R.id.list_teacher, R.id.list_days } );
		setListItem(R.layout.course_list_item);
		
		Context mContext = getActivity();
		// Get the list of teachers
		Cursor c = DbUtils.getTeachersAsCursor(mContext);
		c.moveToFirst();
		for (short i = 0; i < c.getCount(); i++) {
			teachers.put(c.getShort(0),c.getString(1));
			c.moveToNext();
		}
		
		setViewBinder(new ViewBinder() {
			
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				switch (view.getId()) {
				case R.id.list_title:
					((TextView)view).setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_NAME)));
					((LinearLayout)view.getParent()).setBackgroundColor(colors.get(cursor.getShort(0)));
					return true;
				case R.id.list_teacher:
					((TextView)view).setText(teachers.get(cursor.getShort(cursor.getColumnIndexOrThrow(Course.KEY_TEACHER))));
					return true;
				case R.id.list_days:
					((TextView)view).setText(getDaysOfWeek(cursor.getString(cursor.getColumnIndexOrThrow(Course.KEY_TIMES_OF_DAY))));
					return true;
				}
				return false;
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// Has to be done every time activity is resumed because a course could have
			// been added/edited, and we have to (re)load the color for that course. 
		loadCourseColors();
	}
	
	private void loadCourseColors() {
		DbAdapter adapter = new DbAdapter(mContext, null, Course.TABLE_NAME);
		adapter.open();
		Cursor c = adapter.fetchAll(new String[] { Values.KEY_ROWID, Course.KEY_COLOR } );
		c.moveToFirst();
		for (short i = 0; i < c.getCount(); i++) {
			short id = c.getShort(c.getColumnIndexOrThrow(Values.KEY_ROWID));
			int color = c.getInt(c.getColumnIndexOrThrow(Course.KEY_COLOR));
			colors.put(id, UiUtils.changeAlpha(color,AssignmentListFragment.ALPHA));
			c.moveToNext();
		}
		c.close();
	}
	
	private String getDaysOfWeek(String strArray) {
		boolean [] days = new boolean[7];
		try {
			JSONArray array = new JSONArray(strArray);
			for (short x = 0; x < 14; x += 2)
				days[x/2] = (short)array.getInt(x) > 0;
		} catch (JSONException e) { e.printStackTrace(); }
		
		for (short i = 0; i < 7; i++)
			if (days[i] == true)
				break;
			else if (i == 6) // We have reached the end of the list and all are false
				return getString(R.string.course_no_days_set);
		
		String[] daysOfWeek = getResources().getStringArray(R.array.days_of_week_short);
		String rStr = "";
		for (short i = 0; i < 7; i++) {
			if (days[i])
				rStr += daysOfWeek[i] + ", ";
		}
		return rStr.substring(0, rStr.length() - 2);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.course_list, container, false);
		return v;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(mContext, CourseViewFragment.class);
		i.putExtra(Values.KEY_ROWID, id);
		startActivity(i);
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(R.string.course_add);
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// (The only choice is to add a course)
		Intent i = new Intent(mContext, CourseEditActivity.class);
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
		long id = ((AdapterContextMenuInfo)item.getMenuInfo()).id;
    	switch (item.getItemId()) {
    	case CONTEXT_EDIT:
    		startActivity( new Intent(mContext, CourseEditActivity.class)
    			.putExtra(Values.KEY_ROWID, id) );
    		return true;
    	case CONTEXT_DELETE:
    		selectedItem = id;
			DeleteDialogFragment frag = new DeleteDialogFragment();
			frag.setTargetFragment(this, 0);
			frag.show(getFragmentManager(), "confirmDelete");
			return true;
		}
    	return false;
    }
    
/*---------- Course-Delete Prompt ----------*/
	
	public static class DeleteDialogFragment extends DialogFragment {
		
		static DeleteDialogFragment newInstance(int arg) {
			return new DeleteDialogFragment();
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.confirm_delete)
				.setMessage(R.string.course_delete_message)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((CourseListFragment)getTargetFragment()).deleteCourse();
					}
				})
				.setNegativeButton(R.string.no, null)
				.create();
		}
	}
	
	public void deleteCourse() {
		DbUtils.deleteCourse(mContext, selectedItem);
		fillData();
	}
}
