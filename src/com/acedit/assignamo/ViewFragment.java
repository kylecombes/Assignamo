package com.acedit.assignamo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.utils.DbUtils;

public abstract class ViewFragment extends FragmentActivity {
	
	protected long rowId;
	protected Context mContext;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getBaseContext();
		if (savedInstanceState != null)
			rowId = savedInstanceState.getLong(Values.KEY_ROWID);
		else
			setRowIdFromIntent();
	}
	
	public void onStart() {
		super.onStart();
		populateViews();
	}
		
	protected abstract void populateViews();
	
	protected abstract Class<? extends FragmentActivity> getEditClass();
	
	protected abstract String getDatabaseTable();
	
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
		Map<Short, String> teachers = DbUtils.getTeacherNames(getApplicationContext());
		return teachers.get(id);
	}
	
	protected SpannableString getItalicizedString(int strId) {
		String str = getString(strId);
		SpannableString rStr = new SpannableString(str);
		rStr.setSpan(new StyleSpan(Typeface.ITALIC), 0, str.length(), 0);
		return rStr;
	}

	public static SpannableString getItalicizedString(String str) {
		SpannableString rStr = new SpannableString(str);
		rStr.setSpan(new StyleSpan(Typeface.ITALIC), 0, str.length(), 0);
		return rStr;
	}

	/**
	 * Converts a time to a stylized string.
	 * @param mContext a copy of the application mContext.
	 * @param minutes the number of minutes since January 1st, 1970.
	 * @param withTime whether or not to append the time to the end of the date
	 * (January 05, 2010 vs January 05 2010 at 5:30 pm).
	 * @return the stylized string.
	 */
	public SpannableString getDateString(long minutes, boolean withTime) {
		
		final float RELATIVE_SIZE = 1.0f;
		final String DATE_FORMAT_WITH_YEAR = "c, MMMM d, yyyy";
		final String DATE_FORMAT_WITHOUT_YEAR = "c, MMMM d";
		final String TIME_FORMAT = "h:mm a";
		
		
		Calendar calendar = Calendar.getInstance();
		int curYear = calendar.get(Calendar.YEAR);
		calendar.setTimeInMillis(minutes);
		
		SimpleDateFormat dateFormat;
		if (calendar.get(Calendar.YEAR) == curYear)
			dateFormat = new SimpleDateFormat(DATE_FORMAT_WITHOUT_YEAR);
		else
			dateFormat = new SimpleDateFormat(DATE_FORMAT_WITH_YEAR);
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
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.view_edit:
			startActivity(new Intent(mContext, getEditClass())
				.putExtra(Values.KEY_ROWID, rowId));
			return true;
		case R.id.view_delete:
			DeleteDialogFragment frag = new DeleteDialogFragment();
			frag.show(getSupportFragmentManager(), "confirmDelete");
			return true;
		default:
			return false;
		}
	}
	
	protected abstract String getDeleteConfirmationMessage();
	

	protected static class DeleteDialogFragment extends DialogFragment {
		
		public DeleteDialogFragment() {}
		
		static DeleteDialogFragment newInstance(int arg) {
			return new DeleteDialogFragment();
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.confirm_delete)
				.setMessage(((ViewFragment)getActivity()).getDeleteConfirmationMessage())
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((ViewFragment)getActivity()).deleteItem();
					}
				})
				.setNegativeButton(R.string.no, null)
				.create();
		}
	}
	
	protected abstract void deleteItem();
	
}
