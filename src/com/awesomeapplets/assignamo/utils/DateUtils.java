package com.awesomeapplets.assignamo.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	
	public static final String TWENTY_FOUR_HOUR_FORMAT = "HH:mm";
	public static final String TWELVE_HOUR_FORMAT = "hh:mm a";
	
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
	 * Formats a given time in either 24-hour or 12-hour format
	 * @param hour the hour of the day (24-hour format)
	 * @param minute the minute of the day
	 * @param tfHour format in 24-hour format?
	 * @return the String representation of the time
	 */
	public static String formatAsString(int hour, int minute, boolean tfHour) {
		return formatAsString((short)hour, (short)minute, tfHour);
	}
		
	/**
	 * Formats a given time in either 24-hour or 12-hour format
	 * @param hour the hour of the day (24-hour format)
	 * @param minute the minute of the day
	 * @param tfHour format in 24-hour format?
	 * @return the String representation of the time
	 */
	public static String formatAsString(short hour, short minute, boolean tfHour) {
		SimpleDateFormat format;
		if (tfHour)
			format = new SimpleDateFormat(TWENTY_FOUR_HOUR_FORMAT);
		else
			format = new SimpleDateFormat(TWELVE_HOUR_FORMAT);
		Date date = new Date();
		date.setHours(hour);
		date.setMinutes(minute);
		return format.format(date);
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
