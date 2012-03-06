package com.awesomeapplets.assignamo.preferences;

import com.awesomeapplets.assignamo.R;
import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.Values;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.SpannableString;
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
		dbAdapter = new DbAdapter(getBaseContext(), Values.DATABASE_NAME, Values.DATABASE_VERSION, Values.TEACHER_TABLE, Values.DATABASE_CREATE, Values.KEY_ROWID);
		
		setContentView(R.layout.teacher_view_phone);
		nameLabel = (TextView)findViewById(R.id.teacher_view_name);
		subjectLabel = (TextView)findViewById(R.id.teacher_view_subject);
		roomLabel = (TextView)findViewById(R.id.teacher_view_room);
		emailLabel = (TextView)findViewById(R.id.teacher_view_email);
		phoneLabel = (TextView)findViewById(R.id.teacher_view_phone_number);
		notesLabel = (TextView)findViewById(R.id.teacher_view_notes);
		
		
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.view_edit:
			Intent i = new Intent(getApplicationContext(), TeacherEditActivity.class);
			i.putExtra(Values.KEY_ROWID, rowId);
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
		phoneLabel.setText(getFormattedPhoneNumber(data.getLong(data.getColumnIndexOrThrow(Values.TEACHER_KEY_PHONE))),
				BufferType.SPANNABLE);
		
		String notes = data.getString(data.getColumnIndexOrThrow(Values.KEY_NOTES));
		if (notes.length() > 0)
			notesLabel.setText(notes);
		else
			notesLabel.setText(getItalicizedString(R.string.no_notes), BufferType.SPANNABLE);
	}
	
	/**
	 * Formats a phone number as text.<br>
	 * NOTE: Only accepts U.S. phone numbers at the moment.
	 * @param num the phone number.
	 * @return a string representation of that number, or a message if there is no phone number entered.
	 * @throws NumberFormatException if the phone number is not of a valid phone number length.
	 */
	// TODO Add support for foreign phone numbers
	private SpannableString getFormattedPhoneNumber(long num) throws NumberFormatException {
		if (num < 0)
			return getItalicizedString(getString(R.string.teacher_view_no_phone));
		
		String str = Long.toString(num);
		String rStr = "";
		
		short len = (short) str.length();
		short i = 0;
		if (len == 7) {
			for (; i < 3; i++)
				rStr += str.charAt(i);
			rStr += "-";
			for (; i < 7; i++)
				rStr += str.charAt(i);
			
		} else if (len == 10) {
			for (; i < 3; i++)
				rStr += str.charAt(i);
			rStr += "-";
			for (; i < 6; i++)
				rStr += str.charAt(i);
			rStr += "-";
			for (; i < 10; i++)
				rStr += str.charAt(i);
			
		} else if (len == 11) {
			rStr += str.charAt(i) + "-";
			i++;
			for (; i < 4; i++)
				rStr += str.charAt(i);
			rStr += "-";
			for (; i < 7; i++)
				rStr += str.charAt(i);
			rStr += "-";
			for (; i < 11; i++)
				rStr += str.charAt(i);
			
		} else
			throw new NumberFormatException("Invalid phone number.");
		
		return new SpannableString(rStr);
	}
	
}
