package com.awesomeapplets.assignamo.preferences;

import com.awesomeapplets.assignamo.MainActivity;
import com.awesomeapplets.assignamo.R;
import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.Values;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class TeacherViewFragment extends ViewFragment {
	
	private TextView nameLabel;
	private TextView subjectLabel;
	private TextView roomLabel;
	private TextView emailLabel;
	private TextView phoneLabel;
	private TextView notesLabel;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new DbAdapter(getBaseContext(), MainActivity.DATABASE_NAME, MainActivity.DATABASE_VERSION, Values.TEACHER_TABLE, MainActivity.DATABASE_CREATE, MainActivity.KEY_ROWID);
		
		setContentView(R.layout.teacher_view_phone);
		nameLabel = (TextView)findViewById(R.id.teacher_view_name);
		subjectLabel = (TextView)findViewById(R.id.teacher_view_subject);
		roomLabel = (TextView)findViewById(R.id.teacher_view_room);
		emailLabel = (TextView)findViewById(R.id.teacher_view_email);
		phoneLabel = (TextView)findViewById(R.id.teacher_view_phone_number);
		notesLabel = (TextView)findViewById(R.id.teacher_view_notes);
		
		
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.view_edit:
			Intent i = new Intent(getApplicationContext(), TeacherEditActivity.class);
			i.putExtra(MainActivity.KEY_ROWID, rowId);
			startActivity(i);
			break;
		case R.id.view_delete:
			dbAdapter.delete(rowId);
			finish();
		}
		return true;
	}
	
	
	protected void populateFields() {
		Cursor data = dbAdapter.fetch(rowId,Values.TEACHER_FETCH);
		startManagingCursor(data);
		
		nameLabel.setText(data.getString(data.getColumnIndexOrThrow(Values.KEY_NAME)));
		subjectLabel.setText(data.getString(data.getColumnIndexOrThrow(Values.TEACHER_KEY_SUBJECT)));
		
		try {
			short roomNum = Short.parseShort(data.getString(data.getColumnIndexOrThrow(Values.KEY_ROOM)));
			if (roomNum >= 0)
				roomLabel.setText(getString(R.string.teacher_view_room) + ": " + roomNum);
		} catch (NumberFormatException e) {}
		emailLabel.setText(data.getString(data.getColumnIndexOrThrow(Values.TEACHER_KEY_EMAIL)));
		phoneLabel.setText(data.getString(data.getColumnIndexOrThrow(Values.TEACHER_KEY_PHONE)));
		
		String notes = data.getString(data.getColumnIndexOrThrow(Values.KEY_NOTES));
		if (notes.length() > 0)
			notesLabel.setText(notes);
		else
			notesLabel.setText(getItalicizedString(R.string.no_notes), BufferType.SPANNABLE);
	}
	
}
