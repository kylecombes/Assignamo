package com.awesomeapplets.assignamo.preferences;

import com.awesomeapplets.assignamo.R;
import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.Values;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class CourseViewFragment extends ViewFragment {
	
	private TextView nameLabel;
	private TextView teacherLabel;
	private TextView descriptionLabel;
	private TextView roomLabel;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new DbAdapter(getBaseContext(), Values.DATABASE_NAME, Values.DATABASE_VERSION,
				Values.COURSE_TABLE, Values.DATABASE_CREATE, Values.KEY_ROWID);
		
		setContentView(R.layout.course_view_phone);
		nameLabel = (TextView)findViewById(R.id.course_view_name);
		teacherLabel = (TextView)findViewById(R.id.course_view_teacher);
		descriptionLabel = (TextView)findViewById(R.id.course_view_description);
		roomLabel = (TextView)findViewById(R.id.course_view_room);
		
		if (savedInstanceState != null)
			rowId = savedInstanceState.getLong(Values.KEY_ROWID);
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
			i.putExtra(Values.KEY_ROWID, rowId);
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
		outState.putLong(Values.KEY_ROWID, rowId);
	}
	
	
	protected void populateFields() {
		Cursor cursor = dbAdapter.fetch(rowId,Values.COURSE_FETCH);
		startManagingCursor(cursor);
		
		nameLabel.setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_NAME)));
		teacherLabel.setText(getTeacher(cursor.getShort(cursor.getColumnIndexOrThrow(Values.COURSE_KEY_TEACHER))));
		descriptionLabel.setText(getDescription(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_DESCRIPTION))),
				BufferType.SPANNABLE);
		roomLabel.setText(getRoom(cursor.getShort(cursor.getColumnIndexOrThrow(Values.KEY_ROOM))),
				BufferType.SPANNABLE);
		
	}
	
	private SpannableString getRoom(short id) {
		if (id >= 0)
			return new SpannableString(id + "");
		else {
			String str = getString(R.string.course_view_no_room);
			SpannableString rStr = getItalicizedString(str);
			rStr.setSpan(new RelativeSizeSpan(0.8f), 0, str.length(), 0);
			return rStr;
		}
	}
	
}
