package com.acedit.assignamo.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;

public class OnBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) { //Pg 301
		/*ReminderManager reminderMgr = new ReminderManager(context);
		DbAdapter adapter = new DbAdapter(context, Values.DATABASE_NAME, Values.ASSIGNMENT_TABLE);
		Cursor c = adapter.fetchAll(new String[] { Values.ASSIGNMENT_KEY_REMINDERS });*/
	}

}
