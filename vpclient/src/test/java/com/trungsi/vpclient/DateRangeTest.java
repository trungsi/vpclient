/**
 * 
 */
package com.trungsi.vpclient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import com.trungsi.vpclient.utils.DateRange;

import static org.junit.Assert.*;
/**
 * @author trungsi
 *
 */
public class DateRangeTest {

    private int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

	@Test
	public void testParseWithTwoBoundaries() throws Exception {
		DateRange range = DateRange.parse("Du samedi 13 octobre 9h au lundi 22 octobre 6h");
		Date from = formatDateAndHour(getCurrentYear() + "/10/13 09");
		Date to = formatDateAndHour(getCurrentYear() + "/10/22 06");
		
		assertEquals(new DateRange(from, to), range);
	}

    @Test
    public void testParseWithTwoBoundaries2() throws Exception {
        DateRange range = DateRange.parse("Du mercredi 11 septembre 7h\n" +
                "au dimanche 15 septembre 6h");
        Date from = formatDateAndHour(getCurrentYear() + "/09/11 07");
        Date to = formatDateAndHour(getCurrentYear() + "/09/15 06");

        assertEquals(new DateRange(from, to), range);
    }

	@Test
	public void testParseDateWithOneOpenBoundary() throws Exception {
		DateRange range = DateRange.parse("Ouverture le dimanche 21 octobre à 9h");
		Date from = formatDateAndHour(getCurrentYear() + "/10/21 09");
		//Date to = format.parse("2012/10/22 06");

        assertEquals(new DateRange(from, DateRange.MAX), range);
	}

    private Date formatDateAndHour(String dateStr) throws Exception {
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd hh");
        return format.parse(dateStr);

    }

    @Test
    public void testParseWithTwoBoundaries3() throws Exception {
        DateRange range = DateRange.parse("Du dim. 27 à 9h\n" +
                "au ven. 1 novembre");
        Date from = formatDateAndHour(getCurrentYear() + "/10/27 09");
        Date to = formatDateAndHour(getCurrentYear() + "/11/01 23");

        assertEquals(new DateRange(from, to), range);
    }

}
