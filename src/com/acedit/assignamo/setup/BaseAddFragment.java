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

public abstract class BaseAddFragment extends ListFragment {
	
	private static Context context;
	private static long selectedItem;
	private CustomOnClickListener mButtonListener;
	private ItemExistenceMonitor mItemExistsMonitor;
	private ViewHolder viewHolder;
	private static String mDeleteMessage;
	private static int mFragmentId;
	
	protected abstract int getLayoutId();
	protected abstract Cursor getCursor(Context context);
	protected abstract Class<? extends Activity> getEditClass();
	protected void setDeleteConfirmationMessage(String msg) {
		mDeleteMessage = msg;
	}
	protected abstract void deleteItem(Context context, long rowId);
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(getLayoutId(), container, false);
		v.findViewById(R.id.setup_add_button).setOnClickListener(new OnClickListener() {
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
	
	protected void updateAdapter() {
		Cursor c = getCursor(context);
		CustomCursorAdapter adapter = new CustomCursorAdapter(context, c, 0);
		mItemExistsMonitor.itemsExist(c.getCount() > 0);
		getListView().setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	
	public void addButtonClicked(View v) {
		startActivity(new Intent(context, getEditClass()));
	}
	
	private class CustomOnClickListener implements OnClickListener {
		public void onClick(View v) {
			Toast.makeText(context, "Button pressed", Toast.LENGTH_SHORT).show();
			if (v.getId() == viewHolder.editButton.getId())
				startActivity(new Intent(context, getEditClass()).putExtra(Values.KEY_ROWID, (Long)v.getTag()));
			else { // Delete it
				selectedItem = (Long) v.getTag();
				DeleteDialogFragment frag = new DeleteDialogFragment();
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
	
	public static class DeleteDialogFragment extends DialogFragment {
		
		static DeleteDialogFragment newInstance(int arg) {
			return new DeleteDialogFragment();
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.confirm_delete)
				.setMessage(mDeleteMessage)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((BaseAddFragment)getTargetFragment()).deleteItem(context, selectedItem);
					}
				})
				.setNegativeButton(R.string.no, null)
				.create();
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
