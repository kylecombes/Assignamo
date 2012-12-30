package com.acedit.assignamo.manage;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.acedit.assignamo.R;
import com.acedit.assignamo.ViewFragment;
import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.utils.DbUtils;

public class CourseViewFragment extends ViewFragment {
	
	private TextView nameLabel, teacherLabel, descriptionLabel, roomLabel;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_view);
	}
	
	protected void mapViews() {
		nameLabel = (TextView)findViewById(R.id.course_view_name);
		teacherLabel = (TextView)findViewById(R.id.course_view_teacher);
		descriptionLabel = (TextView)findViewById(R.id.course_view_description);
		roomLabel = (TextView)findViewById(R.id.course_view_room);
	}

	protected void populateViews() {
		DbAdapter dbAdapter = new DbAdapter(getBaseContext(), null, Values.COURSE_TABLE)
		.open();
		cursor = dbAdapter.fetch(rowId,Values.COURSE_FETCH);
		dbAdapter.close();
		
		nameLabel.setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_NAME)));
		teacherLabel.setText(getTeacher(cursor.getShort(cursor.getColumnIndexOrThrow(Values.COURSE_KEY_TEACHER))));
		descriptionLabel.setText(getDescription(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_DESCRIPTION))),
				BufferType.SPANNABLE);
		roomLabel.setText(getRoom(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_ROOM))),
				BufferType.SPANNABLE);
		
		cursor.close();
	}
	
	protected void deleteItem() {
		DbUtils.deleteCourse(mContext, rowId);
		finish();
	}

	private SpannableString getRoom(String id) {
		if (id != "-1")
			return new SpannableString(id + "");
		else {
			String str = getString(R.string.course_view_no_room);
			SpannableString rStr = getItalicizedString(str);
			rStr.setSpan(new RelativeSizeSpan(0.8f), 0, str.length(), 0);
			return rStr;
		}
	}

	@Override
	protected Class<? extends FragmentActivity> getEditClass() {
		return CourseEditActivity.class;
	}

	@Override
	protected String getDatabaseTable() {
		return Values.COURSE_TABLE;
	}

	@Override
	protected String getDeleteConfirmationMessage() {
		return getString(R.string.course_delete_message);
	}
	
}
