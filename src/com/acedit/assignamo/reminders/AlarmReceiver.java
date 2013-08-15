package com.acedit.assignamo.reminders;

import com.acedit.assignamo.database.Values;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ReminderIntentService.acquireStaticLock(context);
		
		context.startService(new Intent(context, ReminderService.class)
		.putExtra(Values.KEY_ROWID, intent.getExtras().getLong(Values.KEY_ROWID)));
		
	}

}
