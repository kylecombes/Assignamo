package com.acedit.assignamo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.acedit.assignamo.objects.Assignment;
import com.acedit.assignamo.objects.Course;
import com.acedit.assignamo.ui.ColorStrip;
import com.acedit.assignamo.utils.DbUtils;

public class AssignmentViewFragment extends ViewFragment {
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.assignment_view);
	}
	
	protected void populateViews() {
		Assignment assignment = new Assignment(mContext, rowId);
		Course course = new Course(mContext, assignment.getCourseId());
		// Set color strip
		((ColorStrip)findViewById(R.id.assignment_view_color_strip))
		.setColor(course.getColor());
		
		// Set course label
		((TextView)findViewById(R.id.assignment_view_course))
		.setText(course.getTitle());
		
		// Set title label
		((TextView)findViewById(R.id.assignment_view_title))
		.setText(assignment.getTitle());
		
		// Set due date label
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean withTime = prefs.getBoolean("pref_appearance_show_time", true);
		((TextView)findViewById(R.id.assignment_view_date))
		.setText(getDateString(assignment.getDueDate(),
			withTime), BufferType.SPANNABLE);
		
		// Set description label
		((TextView)findViewById(R.id.assignment_view_description)).setText(getDescription(assignment.getDescription()));
	}
		
	/*---- Menus ----*/
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (DbUtils.isAssignmentCompleted(getApplicationContext(), rowId))
			menu.add(0, 0, 0, R.string.assignment_menu_mark_as_incomplete)
			.setIcon(R.drawable.checkmark);
		else
			menu.add(0, 0, 0, R.string.assignment_menu_mark_as_completed)
			.setIcon(R.drawable.checkmark);
		menu.add(0, R.id.view_edit, 0, R.string.edit).setIcon(android.R.drawable.ic_menu_edit);
		menu.add(0, R.id.view_delete, 0, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 0) {
			if (DbUtils.isAssignmentCompleted(getApplicationContext(), rowId))
				DbUtils.setAssignmentCompletionStatus(getApplicationContext(), rowId, false);
			else
				DbUtils.setAssignmentCompletionStatus(getApplicationContext(), rowId, true);
			return true;
		}
		return super.onOptionsItemSelected(item); //Process Edit or Delete
	}

	protected void deleteItem() {
		// Delete the assignment
		DbUtils.deleteAssignment(mContext, rowId);
		finish();
	}

	@Override
	protected Class<? extends FragmentActivity> getEditClass() {
		return AssignmentEditFragment.class;
	}

	@Override
	protected String getDatabaseTable() {
		return Assignment.TABLE_NAME;
	}

	@Override
	protected String getDeleteConfirmationMessage() {
		return getString(R.string.assignment_confirm_delete_message);
	}
}
