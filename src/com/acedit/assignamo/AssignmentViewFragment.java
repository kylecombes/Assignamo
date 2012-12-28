package com.acedit.assignamo;

import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.ui.ColorStrip;
import com.acedit.assignamo.utils.DbUtils;

public class AssignmentViewFragment extends ViewFragment {
	
	private TextView courseLabel, titleLabel, dueDateLabel, descriptionLabel;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.assignment_view);
	}
	
	protected void mapViews() {
		courseLabel = (TextView)findViewById(R.id.assignment_view_course);
		titleLabel = (TextView)findViewById(R.id.assignment_view_title);
		dueDateLabel = (TextView)findViewById(R.id.assignment_view_date);
		descriptionLabel = (TextView)findViewById(R.id.assignment_view_description);
	}
	
	protected void populateViews() {
		DbAdapter dbAdapter = new DbAdapter(getBaseContext(), null, Values.ASSIGNMENT_TABLE);
		dbAdapter.open();
		cursor = dbAdapter.fetch(rowId, Values.ASSIGNMENT_FETCH);
		dbAdapter.close();
		
		short course = cursor.getShort(cursor.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_COURSE));
		
		// Set color strip
		ColorStrip colorStrip = (ColorStrip)findViewById(R.id.assignment_view_color_strip);
		colorStrip.setColor(getCourseColors().get(course));
		
		// Set course label
		String courseText = DbUtils.getCourseNameFromId(context, course);
		courseLabel.setText(courseText);
		
		// Set title label
		titleLabel.setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_TITLE)));
		
		// Set due date label
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean withTime = prefs.getBoolean("pref_appearance_show_time", true);
		dueDateLabel.setText(getDateString(	cursor.getLong(cursor.getColumnIndexOrThrow(Values.ASSIGNMENT_KEY_DUE_DATE)),
				withTime), BufferType.SPANNABLE);
		
		// Set description label
		descriptionLabel.setText(getDescription(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_DESCRIPTION))));
		cursor.close();
	}
	
	private HashMap<Short, Integer> getCourseColors() {
		HashMap<Short, Integer> colors = new HashMap<Short, Integer>();
		DbAdapter adapter = new DbAdapter(context, null, Values.COURSE_TABLE);
		adapter.open();
		Cursor c = adapter.fetchAll(new String[] { Values.KEY_ROWID, Values.COURSE_KEY_COLOR } );
		c.moveToFirst();
		for (short i = 0; i < c.getCount(); i++) {
			short id = c.getShort(c.getColumnIndexOrThrow(Values.KEY_ROWID));
			int color = c.getInt(c.getColumnIndexOrThrow(Values.COURSE_KEY_COLOR));
			colors.put(id, color);
			c.moveToNext();
		}
		c.close();
		adapter.close();
		return colors;
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
		menu.add(0, 1, 0, R.string.edit).setIcon(android.R.drawable.ic_menu_edit);
		menu.add(0, 2, 0, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			if (DbUtils.isAssignmentCompleted(getApplicationContext(), rowId))
				DbUtils.setAssignmentState(getApplicationContext(), rowId, false);
			else
				DbUtils.setAssignmentState(getApplicationContext(), rowId, true);
			break;
		case 1:
			Intent i = new Intent(getApplicationContext(), AssignmentEditFragment.class);
			i.putExtra(Values.KEY_ROWID, rowId);
			startActivity(i);
			break;
		case 2:
			DeleteDialogFragment frag = new DeleteDialogFragment();
			frag.show(getSupportFragmentManager(), "confirmDelete");
		}
		return true;
	}

	public static class DeleteDialogFragment extends DialogFragment {
		
		static DeleteDialogFragment newInstance(int arg) {
			return new DeleteDialogFragment();
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.confirm_delete)
				.setMessage(R.string.assignment_confirm_delete_message)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((AssignmentViewFragment)((ViewFragment)getActivity())).deleteAssignment();
					}
				})
				.setNegativeButton(R.string.no, null)
				.create();
		}
	}
	
	public void deleteAssignment() {
		// Delete the assignment
		DbUtils.deleteAssignment(context, rowId);
		finish();
	}
}
