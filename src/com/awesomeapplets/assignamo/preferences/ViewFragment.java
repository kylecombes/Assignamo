package com.awesomeapplets.assignamo.preferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.awesomeapplets.assignamo.R;
import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.Values;
import com.awesomeapplets.assignamo.utils.DateUtils;
import com.awesomeapplets.assignamo.utils.DbUtils;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class ViewFragment extends FragmentActivity {
	
	protected long rowId;
	protected DbAdapter dbAdapter;
	protected Context context;
	
	protected abstract void populateFields();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getBaseContext();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (context == null)
			context = getBaseContext();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(Values.KEY_ROWID, rowId);
	}
	
	protected void setRowIdFromIntent() {
			Bundle extras = getIntent().getExtras();
			rowId = extras != null
					? extras.getLong(Values.KEY_ROWID)
					: null;
	}
	
	/**
	 * Get the name of a teacher.
	 * @param id the id of the teacher
	 * @return the name of the teacher
	 */
	protected String getTeacher(short id) {
		String[] teachers = DbUtils.getTeachersAsArray(getApplicationContext());
		return teachers[id];
	}

	protected SpannableString getDescription(String desc) {
		android.util.Log.d("getDescription", "Length: " + desc.length());
		if (desc.length() == 0) {
			String message = getString(R.string.no_description);
			return getItalicizedString(message);
		}
		return new SpannableString(desc);
	}
	
	
	protected SpannableString getItalicizedString(int strId) {
		String str = getString(strId);
		SpannableString rStr = new SpannableString(str);
		rStr.setSpan(new StyleSpan(Typeface.ITALIC), 0, str.length(), 0);
		return rStr;
	}

	protected SpannableString getItalicizedString(String str) {
		SpannableString rStr = new SpannableString(str);
		rStr.setSpan(new StyleSpan(Typeface.ITALIC), 0, str.length(), 0);
		return rStr;
	}

	/**
	 * Converts a time to a stylized string.
	 * @param context a copy of the application context.
	 * @param minutes the number of minutes since January 1st, 1970.
	 * @param withTime whether or not to append the time to the end of the date
	 * (January 05, 2010 vs January 05 2010 at 5:30 pm).
	 * @return the stylized string.
	 */
	public SpannableString getDateString(long minutes, boolean withTime) {
		
		final float RELATIVE_SIZE = 1.0f;
		final String DATE_FORMAT = "c, MMMMM dd, yyyy";
		final String TIME_FORMAT = "hh:mm a";
		
		
		Calendar calendar = Calendar.getInstance();
		//if (rowId != null)
		calendar.setTimeInMillis(DateUtils.convertMinutesToMills(minutes));
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
		
		SpannableString returnString;
		
		// Either way, we need to have the date
		String dueString = "";// getString(R.string.due) + " ";
		String dateString = dateFormat.format(calendar.getTime());
		
		if (!withTime) {
			// We only want the date to be returned (no time)
			returnString = new SpannableString( dueString + dateString );
		} else {
			// If we are going to show the time, get it here
			String atString = " " + getString(R.string.at) + " ";
			String timeString = timeFormat.format(calendar.getTime());
			
			// We want the time as well as the date to be returned
			returnString = new SpannableString( dueString
					+ dateString + atString + timeString);
			
			returnString.setSpan(new StyleSpan(Typeface.BOLD),
					dueString.length() + dateString.length() + atString.length(),
					dueString.length() + dateString.length() + atString.length() + timeString.length(),
					0);
			returnString.setSpan(new RelativeSizeSpan(RELATIVE_SIZE),
					dueString.length() + dateString.length() + atString.length(),
					dueString.length() + dateString.length() + atString.length() + timeString.length(),
					0);
		}
		// This could really be up right after defining returnString, but
			// then I would have to put it in twice, as it is needed in
			// both cases. So instead I put in in here, seemingly out
			// of order.
		returnString.setSpan(new StyleSpan(Typeface.BOLD),
				dueString.length(),
				dueString.length() + dateString.length(),
				0);
		returnString.setSpan(new RelativeSizeSpan(RELATIVE_SIZE),
				dueString.length(),
				dueString.length() + dateString.length(),
				0);
		return returnString;
	}
	
	
	/*---------- Menus ----------*/
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_menu, menu);
		return true;
	}
	
	public abstract boolean onOptionsItemSelected(MenuItem item);
	
}
