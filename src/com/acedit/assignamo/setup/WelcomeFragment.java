package com.acedit.assignamo.setup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.acedit.assignamo.R;
import com.acedit.assignamo.database.DbAdapter;

public class WelcomeFragment  extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState) {
		return inflater.inflate(R.layout.setup_wizard_welcome, container, false);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Create the database
		new DbAdapter(getActivity(), null, null).open().close();
	}
	
}
