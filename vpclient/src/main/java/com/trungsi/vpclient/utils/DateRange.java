/**
 * 
 */
package com.trungsi.vpclient.utils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * @author trungsi
 *
 */
public class DateRange {
    private static final Date MIN = new Date(0);
    public static final Date MAX = new Date(Long.MAX_VALUE);

	private final Date from;
	private final Date to;
	
	private static final String[] MONTHS = new String[] {
		"janvier", "février", "mars", "avril", "mai", "juin",
		"juillet", "août", "septembre", "octobre", "novembre", "décembre"};
	
	public DateRange(Date from, Date to) {
		this.from = from != null ? (Date) from.clone() : MIN;
		this.to = to != null ? (Date) to.clone() : MAX;
	}
	
	public static DateRange parse(String dateStr) {
		String[] array = dateStr.split(" |\n");
        System.out.println(Arrays.toString(array));

		if (array[0].equals("Du")) {
            if (array.length == 12) { // [Du, mardi, 15, juil., à, 7h, au, lundi, 21, juil., à, 6h]
                return parseWithTwoBoundaries3(array);
            }
            if (array[3].equals("à")) { // [Du, dim., 22, à, 9h, au, ven., 27, septembre]
                return parseWithTwoBoundaries2(array);
            }
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

    private static DateRange parseWithTwoBoundaries2(String[] array) {
        Date from = buildDate(array[2], array[8], array[4]);

        Calendar cal = Calendar.getInstance();
        /*if(/*Integer.parseInt(array[2]) > Integer.parseInt(array[7])
                cal.get(Calendar.DAY_OF_MONTH) < Integer.parseInt(array[2])) { // if start day_of_month > end day_of_month => end of month to the beginning of next month
             from = prevMonth(from);
        }*/

        Date to = buildDate(array[7], array[8], "23h");
        if (to.getMonth()+1 == 1 && to.getDate() < Integer.parseInt(array[2])) {
            from = buildDate(array[2], "décembre", array[4]);
            to = nextYear(to);

        }
        /*if (to.before(new Date())) {
            to = nextYear(to);
        }*/
        if (from.after(to)) {
            from = prevMonth(from);
        }
        return new DateRange(from, to);
    }

    private static DateRange parseWithTwoBoundaries3(String[] array) {
        Date from = buildDate(array[2], array[3], array[5]);

        Date to = buildDate(array[8], array[9], array[11]);

        return new DateRange(from, to);
    }

    private static Date nextYear(Date to) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(to);
        cal.add(Calendar.YEAR, 1);

        return cal.getTime();
    }

    private static Date prevMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);

        return cal.getTime();
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
        boolean abbreviate = monthStr.endsWith("."); // ex: juil.

		for (int i = 0; i < MONTHS.length; i++) {
			if (MONTHS[i].equals(monthStr) || (abbreviate && MONTHS[i].startsWith(monthStr.substring(0, monthStr.length()-1)))) {
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

    public DateRange getNextTo(Date date) {
        // date must before from
        return new DateRange(date, from);
    }

    public long getTimestamp() {
        //
        return to.getTime() - from.getTime();
    }

    public String toString() {
        return from + " " + to;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DateRange)) {
            return false;
        }

        DateRange other = (DateRange) obj;

        return from.equals(other.from) && to.equals(other.to);
    }

    public int hashCode() {
        return 37*from.hashCode() + 17*to.hashCode() + 7;
    }
}
