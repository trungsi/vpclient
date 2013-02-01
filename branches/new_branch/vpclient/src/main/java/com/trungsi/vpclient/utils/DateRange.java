/**
 * 
 */
package com.trungsi.vpclient.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * @author trungsi
 *
 */
public class DateRange {
	public final Date from;
	public final Date to;
	
	private static final String[] MONTHS = new String[] {
		"janvier", "février", "mars", "avril", "mai", "juin",
		"juillet", "août", "septembre", "octobre", "novembre", "décembre"};
	
	public DateRange(Date from, Date to) {
		this.from = from;
		this.to = to;
	}
	
	public static DateRange parse(String dateStr) {
		String[] array = dateStr.split(" ");
		if (array[0].equals("Du")) {
			return parseWithTwoBoundaries(array);
		} else if (array[0].equals("Ouverture")) {
			return parseWithOneOpenBoundary(array);
		}
		
		return new DateRange(null, null);
		
	}


	private static DateRange parseWithOneOpenBoundary(String[] array) {
		Date from = buildDate(array[3], array[4], array[6]);
		
		return new DateRange(from, null);
	}

	private static DateRange parseWithTwoBoundaries(String[] array) {
		Date from = buildDate(array[2], array[3], array[4]);
		Date to = buildDate(array[7], array[8], array[9]);
		
		return new DateRange(from, to);
	}

	public static Date buildDate(String dateStr, String monthStr, String hourStr) {
		int date = toIntDate(dateStr);
		int month = toIntMonth(monthStr);
		int hour = toIntHour(hourStr);
		
		Calendar cal = Calendar.getInstance();
        //System.out.println("before " + cal);
        cal.set(cal.get(Calendar.YEAR), month, date, hour, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
        //System.out.println("after " + cal);

        // TODO Auto-generated method stub
		return cal.getTime();
	}

	private static int toIntHour(String hourStr) {
		return Integer.parseInt(hourStr.substring(0, hourStr.length()-1)); // remove trailing h
	}

	private static int toIntMonth(String monthStr) {
		for (int i = 0; i < MONTHS.length; i++) {
			if (MONTHS[i].equals(monthStr)) {
				return i;
			}
		}
		
		//return -1;
        throw new RuntimeException("month not found " + monthStr);
	}

	private static int toIntDate(String dateStr) {
		// TODO Auto-generated method stub
		return Integer.parseInt(dateStr);
	}

	public boolean containsDate(Date currentDate) {
		return (from == null || from.compareTo(currentDate) <= 0) &&
				(to == null || to.compareTo(currentDate) >= 0);
	}
}
