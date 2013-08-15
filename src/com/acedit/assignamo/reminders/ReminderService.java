package com.acedit.assignamo.reminders;

import com.acedit.assignamo.database.Values;

import android.content.Intent;

public class ReminderService extends ReminderIntentService {

	public ReminderService() {
		super("ReminderService");
	}

	@Override
	protected void doReminderWork(Intent intent) {
		long id = intent.getExtras().getLong(Values.KEY_ROWID);
		
		
	}

}
