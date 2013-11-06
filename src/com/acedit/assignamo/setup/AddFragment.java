package com.acedit.assignamo.setup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.acedit.assignamo.R;
import com.acedit.assignamo.database.Values;
import com.acedit.assignamo.manage.CourseEditActivity;
import com.acedit.assignamo.manage.TeacherEditActivity;
import com.acedit.assignamo.utils.DbUtils;

public class AddFragment extends ListFragment {
	
	private static Context context;
	private CustomOnClickListener mButtonListener;
	private ItemExistenceMonitor mItemExistsMonitor;
	private ViewHolder viewHolder;
	private static String mDeleteMessage;
	private static int mFragmentId;
	public static enum DisplayState { TEACHERS, COURSES };
	private static DisplayState displayState = DisplayState.TEACHERS;
	private static TextView titleMsg, msgMsg;
	
	protected void setDeleteConfirmationMessage(String msg) {
		mDeleteMessage = msg;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.setup_wizard_add, container, false);
		titleMsg = (TextView)v.findViewById(R.id.setup_wizard_add_screen_title);
		msgMsg = (TextView)v.findViewById(R.id.setup_wizard_add_screen_message);
		updateTitle();
		Button addButton = (Button)v.findViewById(R.id.setup_add_button);
		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				addButtonClicked(v);
			}
		});
		mFragmentId = this.getId();
		mButtonListener = new CustomOnClickListener();
		return v;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mItemExistsMonitor = (ItemExistenceMonitor) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ItemExistenceMonitor.");
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		context = getActivity();
		updateAdapter();
	}
	
	public void setDisplayState(DisplayState newState) {
		displayState = newState;
		updateTitle();
		updateAdapter();
	}
	
	protected void updateAdapter() {
		Cursor c = displayState == DisplayState.TEACHERS ? DbUtils.getTeachersAsCursor(context)
				: DbUtils.getCoursesAsCursor(context);
		CustomCursorAdapter adapter = new CustomCursorAdapter(context, c, 0);
		mItemExistsMonitor.itemsExist(c.getCount() > 0);
		getListView().setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	
	private void updateTitle() {
		switch (displayState) {
		case TEACHERS:
			titleMsg.setText(R.string.setup_teachers_title);
			msgMsg.setText(R.string.setup_teachers_message);
			break;

		case COURSES:
			titleMsg.setText(R.string.setup_courses_title);
			msgMsg.setText(R.string.setup_courses_message);
			break;
		}
	}
	
	public void addButtonClicked(View v) {
		startActivity(new Intent(context, displayState == DisplayState.TEACHERS ? TeacherEditActivity.class : CourseEditActivity.class ));
	}
	
	private class CustomOnClickListener implements OnClickListener {
		public void onClick(View v) {
			Toast.makeText(context, "Button pressed", Toast.LENGTH_SHORT).show();
			if (v.getId() == viewHolder.editButton.getId()) {
				Class<? extends Activity> targetClass = displayState == DisplayState.TEACHERS ? TeacherEditActivity.class
						: CourseEditActivity.class;
				startActivity(new Intent(context, targetClass).putExtra(Values.KEY_ROWID, (Long)v.getTag()));
			} else { // Delete it
				DeleteDialogFragment frag = new DeleteDialogFragment().setTargetItemId((Long)v.getTag());
				frag.setTargetFragment(getFragmentManager().findFragmentById(mFragmentId), 0);
				frag.show(getFragmentManager(), "confirmDelete");
			}
		}
		
	}
	
	private class CustomCursorAdapter extends CursorAdapter {

		LayoutInflater mInflater;
		
		public CustomCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.setup_wizard_list_item, parent, false);
				viewHolder = new ViewHolder();
				viewHolder.text = (TextView) convertView.findViewById(R.id.setup_list_item_text);
				viewHolder.editButton = (Button) convertView.findViewById(R.id.setup_wizard_edit_button);
				viewHolder.deleteButton = (Button) convertView.findViewById(R.id.setup_wizard_delete_button);
			}
			mCursor.moveToPosition(position);
			long rowId = mCursor.getLong(mCursor.getColumnIndexOrThrow(Values.KEY_ROWID));
			viewHolder.text.setText(mCursor.getString(mCursor.getColumnIndexOrThrow(Values.KEY_NAME)));
			viewHolder.editButton.setOnClickListener(mButtonListener);
			viewHolder.editButton.setTag(rowId);
			viewHolder.deleteButton.setOnClickListener(mButtonListener);
			viewHolder.deleteButton.setTag(rowId);
			
			return convertView;
		}
		
		@Override
		public void bindView(View arg0, Context arg1, Cursor arg2) {}

		@Override
		public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {return null;}
		
	}
	
	private static class ViewHolder {
		
		TextView text;
		Button editButton, deleteButton;
		
		public ViewHolder() {}
	}
	
	/*---------- Delete Confirmation Dialog ----------*/
	
	public static class DeleteDialogFragment extends DialogFragment {
		
		private DialogOnClickListener mOnClickListener;
		
		static DeleteDialogFragment newInstance(int arg) {
			return new DeleteDialogFragment();
		}
		
		public DeleteDialogFragment setTargetItemId(long id) {
			mOnClickListener = new DialogOnClickListener().setTargetItemId(id);
			return this;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.confirm_delete)
				.setMessage(mDeleteMessage)
				.setPositiveButton(R.string.yes, mOnClickListener)
				.setNegativeButton(R.string.no, null)
				.create();
		}
		
		private class DialogOnClickListener implements DialogInterface.OnClickListener {
			
			private long mTargetId;
			
			public DialogOnClickListener setTargetItemId(long targedId) {
				mTargetId = targedId;
				return this;
			}
			
			public void onClick(DialogInterface dialog, int which) {
				if (displayState == DisplayState.TEACHERS) {
					DbUtils.deleteTeacher(context, mTargetId);
				} else {
					DbUtils.deleteCourse(context, mTargetId);
				}
			}
			
		}
	}
	
	
	/** Used by SetupWizard to enable/disable Next button depending on whether or not the user
		has added items to the database (teachers or courses). This prevents the user from
		 navigating forward with no teachers or courses in the database.
	*/ 
	
	public interface ItemExistenceMonitor {
		
		public void itemsExist(boolean itemsDoExist);
		
	}
	
}
