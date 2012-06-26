package com.acedit.assignamo;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class FragmentAdapter extends FragmentPagerAdapter {
	protected static String[] CONTENT;
	protected Context context;
	private short currentPosition;
	
	private int mCount;

	public FragmentAdapter(FragmentManager fm, String[] titles) {
		super(fm);
		CONTENT = titles;
		mCount = CONTENT.length;
	}

	@Override
	public Fragment getItem(int position) {
		currentPosition = (short)position;
		switch (position) {
		case 0:
			return new AssignmentListFragment(context);
		default:
			return new AssignmentListFragment(context, (short)(position - 1));
		}
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