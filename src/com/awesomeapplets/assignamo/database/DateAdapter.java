package com.awesomeapplets.assignamo.database;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateAdapter {
	
	/**
	 * Format a Calendar object as a string.
	 * @param calendar the calendar object to format
	 * @param format a SimpleDateFormat format
	 * @return A String representation of the Calendar object.
	 */
	public static String formatAsString(Calendar calendar, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(calendar.getTime());
	}
		
	/**
	 * Convert the number of milliseconds since January 1st, 1970,
	 * into seconds.
	 * @param long the number of milliseconds
	 * @return the number of minutes since January 1st, 1970
	 */
	public static long convertMillsToMinutes(long epoch) {
		return epoch / 60000;
	}
	
	/**
	 * Convert the number of minutes since January 1st, 1970, back into
	 * milliseconds.
	 * @param seconds the number of minutes since January 1st, 1970
	 * @return the UNIX epoch time (milliseconds)
	 */
	public static long convertMinutesToMills(long minutes) {
		return minutes * 60000;
	}
}
