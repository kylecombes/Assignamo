package com.acedit.assignamo.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	
	public static final String TWENTY_FOUR_HOUR_FORMAT = "H:mm";
	public static final String TWELVE_HOUR_FORMAT = "hh:mm a";
	
	/**
	 * Format a Calendar object as a string.
	 * @param calendar the calendar object to format
	 * @param format a SimpleDateFormat format
	 * @return A String representation of the Calendar object.
	 */
	public static String formatAsString(Calendar calendar, String format) {
		return new SimpleDateFormat(format).format(calendar.getTime());
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
		long hrs = hour * 3600000;
		long newTime = hrs + (minute * 60000);
		return format.format(new Date(newTime));
	}
	
}
