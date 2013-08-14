package com.acedit.assignamo.objects;

import org.json.JSONArray;

import android.content.ContentValues;
import android.content.Context;

import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;

public class CourseEditor extends Course {
	
	private Context mContext;
	
	public CourseEditor(Context context) {
		mContext = context;
	}
	
	public CourseEditor(Context context, short id) {
		super(context, id);
	}
	
	public void setTitle(String title) {
		mName = title;
	}
	
	public void setTeacher(Teacher teacher) {
		mTeacherId = teacher.getId();
	}
	
	public void setTeacher(short id) {
		mTeacherId = id;
	}
	
	public void setDescription(String desc) {
		mDescription = desc;
	}
	
	public void setColor(int color) {
		mColor = color;
	}
	
	public void setRoom(String room) {
		mRoom = room;
	}
	
	public void setClassStartTimes(short[] startTimes) {
		mStartTimes = startTimes;
	}
	
	public void setClassStopTimes(short[] stopTimes) {
		mStopTimes = stopTimes;
	}

	public boolean commitToDatabase() {
		ContentValues values = new ContentValues();
    	values.put(Values.KEY_NAME, mName);
    	values.put(KEY_TEACHER, mTeacherId);
    	values.put(Values.KEY_DESCRIPTION, mDescription);
    	values.put(Values.KEY_ROOM, mRoom);
    	values.put(KEY_COLOR, mColor);
    	
    	JSONArray timesAsArray = new JSONArray();
    	if (mStartTimes != null && mStopTimes != null) {
	    	for (short x = 0; x < 7; x++) {
	    			timesAsArray.put(mStartTimes[x]);
	    			timesAsArray.put(mStopTimes[x]);
	    	}
    	} else for (short i = 0; i < 14; i++)
    		timesAsArray.put(0);
    	values.put(KEY_TIMES_OF_DAY, timesAsArray.toString());
    	
    	DbAdapter dbAdapter = new DbAdapter(mContext, null, TABLE_NAME).open();
		if (mId == null)
			return dbAdapter.add(values) > 0;
		return dbAdapter.update(mId, values);
	}
	
}
