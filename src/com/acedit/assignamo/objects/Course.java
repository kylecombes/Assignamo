package com.acedit.assignamo.objects;

import java.io.Serializable;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.database.Cursor;

import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;

public class Course implements Serializable {
	
	public static final String TABLE_NAME = "courses", KEY_TEACHER = "teacher",
	KEY_TIMES_OF_DAY = "times_of_day", START_TIMES_KEY= "start_times",
	STOP_TIMES_KEY= "stop_times", KEY_COLOR = "color";
	public static final String[] FETCH_DATA = { Values.KEY_ROWID, Values.KEY_NAME, KEY_TEACHER,
		Values.KEY_DESCRIPTION,	Values.KEY_ROOM, KEY_TIMES_OF_DAY, KEY_COLOR };
	public static final String DATABASE_CREATE = "create table " + TABLE_NAME + " ( "
			+ Values.KEY_ROWID + " integer primary key autoincrement, "
			+ Values.KEY_NAME + " text not null, "
			+ KEY_TEACHER + " text not null, "
			+ Values.KEY_DESCRIPTION + " text, "
			+ Values.KEY_ROOM + " text, "
			+ KEY_TIMES_OF_DAY + " text, "
			+ KEY_COLOR + " int not null);";
	
	protected Short mId;
	protected String mName, mDescription, mRoom;
	protected short mTeacherId;
	protected int mColor;
	protected short[] mStartTimes, mStopTimes;
	
	/**
	 * Create a new manipulatable Course object.
	 */
	public Course() {
		
	}
	
	public Course(Context context, short id) {
		mId = id;
		Cursor data = new DbAdapter(context, null, TABLE_NAME).open()
				.fetch(id, FETCH_DATA);
		mName = data.getString(data.getColumnIndexOrThrow(Values.KEY_NAME));
		mTeacherId = data.getShort(data.getColumnIndexOrThrow(KEY_TEACHER));
		mDescription = data.getString(data.getColumnIndexOrThrow(Values.KEY_DESCRIPTION));
		mRoom = data.getString(data.getColumnIndexOrThrow(Values.KEY_ROOM));
		
		loadTimes(data.getString(data.getColumnIndexOrThrow(KEY_TIMES_OF_DAY)));
		
		mColor = data.getInt(data.getColumnIndexOrThrow(KEY_COLOR));
		
		data.close();
	}
	
	/**
	 * Check to see whether or not we are creating a new course or
	 * editing an existing course.
	 * @return whether or not we are editing an existing course
	 */
	public boolean editingExisting() {
		return mId != null;
	}
	
	public Short getId() {
		return mId;
	}
	
	public String getTitle() {
		return mName;
	}
	
	public short getTeacherId() {
		return mTeacherId;
	}
	
	public String getDescription() {
		return mDescription;
	}
	
	public short[] getClassStartTimes() {
		return mStartTimes;
	}
	
	public short[] getClassStopTimes() {
		return mStopTimes;
	}
	
	public boolean[] getDays() {
		boolean[] days = new boolean[7];
		for (short i = 0; i < 7; i++)
			if (mStartTimes[i] != 0)
				days[i] = true;
		return days;
	}
	
	public Calendar getNextClassTime(Context context) {
		Calendar cal = Calendar.getInstance();
		short[] courseStartTimes = getClassStartTimes();
		boolean[] days = getDays();
		short curDayOfWeek = (short) (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1);
		short dayDiff = 1;
		// Defaults to noon
		short startTime = 720;
		for (int day = curDayOfWeek + 1; dayDiff < 8; day++, dayDiff++) {
			if (day == 7)
				day = 0;
			if (days[day] == true) {
				startTime = courseStartTimes[day];
				break;
			}
		}
		if (dayDiff > 7) dayDiff = 1;
		cal.add(Calendar.DATE, dayDiff);
		cal.set(Calendar.HOUR_OF_DAY, startTime / 60);
		cal.set(Calendar.MINUTE, startTime % 60);
		return cal;
	}
	
	public int getColor() {
		return mColor;
	}
	
	public String getRoom() {
		return mRoom;
	}
	
	private void loadTimes(String timesStr) {
		JSONArray array;
		try {
			array = new JSONArray(timesStr);
			mStartTimes = new short[7];
			mStopTimes = new short[7];
			for (short x = 0; x < 14; x+=2) {
				mStartTimes[x/2] = (short)array.getInt(x);
				mStopTimes[x/2] = (short)array.getInt(x);
			}
		} catch (JSONException e) {e.printStackTrace();}
	}
	
}
