package com.acedit.assignamo.reminders;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public abstract class ReminderIntentService extends IntentService {
	
	public static final String LOCK_NAME_STATIC = "com.acedit.assignamo.reminders.Static";
	private static PowerManager.WakeLock wakeLock;
	
	public ReminderIntentService(String name) {
		super(name);
	}
	
	@Override
	final protected void onHandleIntent(Intent intent) {
		try {
			doReminderWork(intent);
		} finally {
			getLock(this).release();
		}
	}
	
	protected abstract void doReminderWork(Intent intent);
	
	public static void acquireStaticLock(Context context) {
		getLock(context).acquire();
	}
	
	synchronized private static PowerManager.WakeLock getLock(Context context) {
		if (wakeLock == null) {
			PowerManager mgr = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
			wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
			wakeLock.setReferenceCounted(true);
		}
		return wakeLock;
	}

}
