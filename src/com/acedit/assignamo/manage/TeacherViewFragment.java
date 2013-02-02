package com.acedit.assignamo.manage;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.acedit.assignamo.R;
import com.acedit.assignamo.ViewFragment;
import com.acedit.assignamo.objects.Teacher;
import com.acedit.assignamo.utils.DbUtils;

public class TeacherViewFragment extends ViewFragment {
		
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.teacher_view);
	}
	
	protected void populateViews() {
		Teacher teacher = new Teacher(mContext, (short) rowId);
		((TextView)findViewById(R.id.teacher_view_name)).setText(teacher.getName());
		((TextView)findViewById(R.id.teacher_view_subject)).setText(teacher.getDepartment());
		((TextView)findViewById(R.id.teacher_view_room)).setText(teacher.getRoom());
		((TextView)findViewById(R.id.teacher_view_email)).setText(teacher.getEmail());
		((TextView)findViewById(R.id.teacher_view_phone_number)).setText(teacher.getPhoneNumberAsFormattedString(mContext), BufferType.SPANNABLE);
		((TextView)findViewById(R.id.teacher_view_notes)).setText(teacher.getNotes());
	}
	
	@Override
	protected Class<? extends FragmentActivity> getEditClass() {
		return TeacherEditActivity.class;
	}

	@Override
	protected String getDatabaseTable() {
		return Teacher.TABLE_NAME;
	}

	@Override
	protected void deleteItem() {
		DbUtils.deleteTeacher(mContext, rowId);
		finish();
	}

	@Override
	protected String getDeleteConfirmationMessage() {
		return getString(R.string.teacher_confirm_delete_message);
	}
	
}
