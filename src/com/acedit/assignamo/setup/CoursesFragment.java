package com.acedit.assignamo.setup;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;

import com.acedit.assignamo.R;
import com.acedit.assignamo.manage.CourseEditActivity;
import com.acedit.assignamo.utils.DbUtils;

public class CoursesFragment extends BaseAddFragment {

	@Override
	protected int getLayoutId() {
		return R.layout.setup_wizard_courses;
	}

	@Override
	protected Cursor getCursor(Context context) {
		return DbUtils.getCoursesAsCursor(context);
	}

	@Override
	protected Class<? extends Activity> getEditClass() {
		return CourseEditActivity.class;
	}

	@Override
	protected void deleteItem(Context context, long rowId) {
		DbUtils.deleteCourse(context, rowId);
		updateAdapter();
	}
	
}
