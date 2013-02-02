package com.acedit.assignamo.manage;

import android.content.Intent;
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

import com.acedit.assignamo.R;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.objects.Teacher;

public class TeacherListFragment extends BaseListFragment {
	
	public TeacherListFragment() {
		
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = getDbAdapter(Teacher.TABLE_NAME);
		setListFrom( new String[] { Values.KEY_NAME, Teacher.KEY_DEPARTMENT, Values.KEY_NOTES } );
		setListTo( new int[] { R.id.list_title, R.id.list_department, R.id.list_room_num } );
		setListItem(R.layout.teacher_list_item);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.teacher_list, container, false);
	}

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(mContext, TeacherViewFragment.class);
		i.putExtra(Values.KEY_ROWID, id);
		startActivity(i);
	}
    
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(R.string.teacher_add);
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// (The only choice is to add a teacher)
		Intent i = new Intent(mContext, TeacherEditActivity.class);
		startActivity(i);
    	return true;
    }

    private static final int CONTEXT_EDIT = 4;
    private static final int CONTEXT_DELETE = 5;
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(0, CONTEXT_EDIT, 0, getString(R.string.teacher_edit));
		menu.add(0, CONTEXT_DELETE, 0, getString(R.string.teacher_delete));
    }
    
    public boolean onContextItemSelected(MenuItem item) {
    	
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		
    	switch (item.getItemId()) {
    	case CONTEXT_EDIT:
    		Intent i = new Intent(mContext, TeacherEditActivity.class);
    		i.putExtra(Values.KEY_ROWID, info.id);
    		startActivity(i);
    		break;
    	case CONTEXT_DELETE:
    		delete(info.id);
			fillData();
			return true;
		}
    	return false;
    }
}
