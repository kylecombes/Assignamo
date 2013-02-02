package com.acedit.assignamo.manage;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.acedit.assignamo.R;
import com.acedit.assignamo.ViewFragment;
import com.acedit.assignamo.objects.Course;
import com.acedit.assignamo.objects.Teacher;
import com.acedit.assignamo.utils.DbUtils;

public class CourseViewFragment extends ViewFragment {
	
	private Course mCourse;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_view);
	}
	
	protected void populateViews() {
		mCourse = new Course(mContext, (short) rowId);
		((TextView)findViewById(R.id.course_view_name))
			.setText(mCourse.getTitle());
		((TextView)findViewById(R.id.course_view_teacher))
			.setText(new Teacher(mContext, mCourse.getTeacherId()).getName());
		((TextView)findViewById(R.id.course_view_description))
			.setText(mCourse.getDescription());
		((TextView)findViewById(R.id.course_view_room))
			.setText(mCourse.getRoom());
	}
	
	protected void deleteItem() {
		DbUtils.deleteCourse(mContext, rowId);
		finish();
	}
	
	@Override
	protected Class<? extends FragmentActivity> getEditClass() {
		return CourseEditActivity.class;
	}

	@Override
	protected String getDatabaseTable() {
		return Course.TABLE_NAME;
	}

	@Override
	protected String getDeleteConfirmationMessage() {
		return getString(R.string.course_delete_message);
	}
	
}
