package com.awesomeapplets.assignamo.preferences;

import com.awesomeapplets.assignamo.MainActivity;
import com.awesomeapplets.assignamo.R;
import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.Values;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class CourseViewFragment extends ViewFragment {
	
	private long rowId;
	private DbAdapter dbAdapter;
	
	private TextView nameLabel;
	private TextView teacherLabel;
	private TextView descriptionLabel;
	private TextView roomLabel;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new DbAdapter(getBaseContext(), MainActivity.DATABASE_NAME, MainActivity.DATABASE_VERSION,
				Values.COURSE_TABLE, MainActivity.DATABASE_CREATE, MainActivity.KEY_ROWID);
		
		setContentView(R.layout.course_view_phone);
		nameLabel = (TextView)findViewById(R.id.course_view_name);
		teacherLabel = (TextView)findViewById(R.id.course_view_teacher);
		descriptionLabel = (TextView)findViewById(R.id.course_view_description);
		roomLabel = (TextView)findViewById(R.id.course_view_room);
		
		if (savedInstanceState != null)
			rowId = savedInstanceState.getLong(MainActivity.KEY_ROWID);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		dbAdapter.close();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		dbAdapter.open();
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
			Intent i = new Intent(getApplicationContext(), CourseEditFragment.class);
			i.putExtra(MainActivity.KEY_ROWID, rowId);
			startActivity(i);
			break;
		case R.id.view_delete:
			dbAdapter.delete(rowId);
			finish();
		}
		return true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(MainActivity.KEY_ROWID, rowId);
	}
	
	
	protected void populateFields() {
		Cursor cursor = dbAdapter.fetch(rowId,Values.COURSE_FETCH);
		startManagingCursor(cursor);
		
		nameLabel.setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_NAME)));
		teacherLabel.setText(getTeacher(cursor.getShort(cursor.getColumnIndexOrThrow(Values.COURSE_KEY_TEACHER))));
		descriptionLabel.setText(getDescription(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_DESCRIPTION))),
				BufferType.SPANNABLE);
		roomLabel.setText(getRoom(cursor.getShort(cursor.getColumnIndexOrThrow(Values.KEY_ROOM))));
		
	}
	
	private String getRoom(short id) {
		if (id >= 0)
			return id + "";
		else
			return getString(R.string.course_view_no_room);
	}
	
}
