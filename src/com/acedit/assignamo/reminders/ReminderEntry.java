/*
 * Based on the source of the Android calendar application.
 * 
 * This code was taken from com.android.calendar.CalendarEventModels.java
 * and modified slightly.
 */

package com.acedit.assignamo.reminders;

import android.provider.CalendarContract.Reminders;


/**
 * A single reminder entry.
 *
 * Instances of the class are immutable.
 */
public class ReminderEntry implements Comparable<ReminderEntry> {
	private final int mMinutes;

    /**
     * Returns a new ReminderEntry, with the specified minutes.
     *
     * @param minutes Number of minutes before the start of the event that the alert will fire.
     * @param method Type of alert ({@link Reminders#METHOD_ALERT}, etc).
     */
    public static ReminderEntry valueOf(int minutes) {
        // TODO: cache common instances
        return new ReminderEntry(minutes);
    }

    /**
     * Constructs a new ReminderEntry.
     *
     * @param minutes Number of minutes before the start of the event that the alert will fire.
     * @param method Type of alert ({@link Reminders#METHOD_ALERT}, etc).
     */
    private ReminderEntry(int minutes) {
        // TODO: error-check args
        mMinutes = minutes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ReminderEntry)) {
            return false;
        }

        ReminderEntry re = (ReminderEntry) obj;

        if (re.mMinutes == mMinutes) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "ReminderEntry for " + mMinutes + " minutes";
    }

    /**
     * Comparison function for a sort ordered primarily descending by minutes,
     * secondarily ascending by method type.
     */
    public int compareTo(ReminderEntry re) {
        if (re.mMinutes != mMinutes) {
            return re.mMinutes - mMinutes;
        }
        return 0;
    }

    /** Returns the minutes. */
    public int getMinutes() {
        return mMinutes;
    }

}