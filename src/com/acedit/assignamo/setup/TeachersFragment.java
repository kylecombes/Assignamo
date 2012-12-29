package com.acedit.assignamo.setup;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;

import com.acedit.assignamo.R;
import com.acedit.assignamo.manage.TeacherEditActivity;
import com.acedit.assignamo.utils.DbUtils;

public class TeachersFragment extends BaseAddFragment {

	@Override
	protected int getLayoutId() {
		return R.layout.setup_wizard_teachers;
	}

	@Override
	protected Cursor getCursor(Context context) {
		return DbUtils.getTeachersAsCursor(context);
	}

	@Override
	protected Class<? extends Activity> getEditClass() {
		return TeacherEditActivity.class;
	}

	@Override
	protected void deleteItem(Context context, long rowId) {
		DbUtils.deleteTeacher(context, rowId);
		updateAdapter();
	}
	
}
