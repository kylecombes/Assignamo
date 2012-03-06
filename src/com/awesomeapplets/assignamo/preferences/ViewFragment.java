package com.awesomeapplets.assignamo.preferences;

import com.awesomeapplets.assignamo.R;
import com.awesomeapplets.assignamo.database.DbAdapter;
import com.awesomeapplets.assignamo.database.DbUtils;
import com.awesomeapplets.assignamo.database.Values;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class ViewFragment extends FragmentActivity {
	
	protected long rowId;
	protected DbAdapter dbAdapter;
	
	protected abstract void populateFields();
	
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
	
	/*---------- Menus ----------*/
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_menu, menu);
		return true;
	}
	
	public abstract boolean onOptionsItemSelected(MenuItem item);
	
}
