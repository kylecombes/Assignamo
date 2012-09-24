package com.acedit.assignamo;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.acedit.assignamo.utils.DbUtils;

public class FragmentAdapter extends FragmentPagerAdapter {
	protected static String[] CONTENT;
	protected Context context;
	private short currentPosition;
	private Cursor courseCursor;
	
	private int mCount;

	public FragmentAdapter(FragmentManager fm, String[] titles) {
		super(fm);
		CONTENT = titles;
		mCount = CONTENT.length;
	}

	@Override
	public Fragment getItem(int position) {
		setCurrentPosition(position);
		switch (position) {
		case 0:
			return new AssignmentListFragment(context);
		default:
			if (courseCursor == null)
				courseCursor = DbUtils.getCoursesAsCursor(context);
			courseCursor.moveToPosition(position - 1);
			short rowId = courseCursor.getShort(0);
			return new AssignmentListFragment(context, rowId);
		}
	}
	
	public void setCurrentPosition(short pos) {
		currentPosition = pos;
	}
	
	public void setCurrentPosition(int pos) {
		currentPosition = (short)pos;
	}
	
	@Override
	public int getCount() {
		return mCount;
	}
	
	public short getCurrentLocation() {
		return currentPosition;
	}
	
	public void setCount(int count) {
		if (count > 0 && count <= 10) {
			mCount = count;
			notifyDataSetChanged();
		}
	}
}