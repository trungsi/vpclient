/**
 * 
 */
package com.trungsi.vpclient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.trungsi.vpclient.utils.DateRange;

import static org.junit.Assert.*;
/**
 * @author trungsi
 *
 */
public class DateRangeTest {

	@Test
	public void testParseWithTwoBoundaries() throws Exception {
		DateRange range = DateRange.parse("Du samedi 13 octobre 9h au lundi 22 octobre 6h");
		DateFormat format = new SimpleDateFormat("yyyy/MM/dd hh");
		Date from = format.parse("2012/10/13 09");
		Date to = format.parse("2012/10/22 06");
		
		assertEquals(from, range.from);
		assertEquals(to, range.to);
	}
	
	@Test
	public void testParseDateWithOneOpenBoundary() throws Exception {
		DateRange range = DateRange.parse("Ouverture le dimanche 21 octobre à 9h");
		DateFormat format = new SimpleDateFormat("yyyy/MM/dd hh");
		Date from = format.parse("2012/10/21 09");
		//Date to = format.parse("2012/10/22 06");
		
		assertEquals(from, range.from);
		assertNull(range.to);
	}
}
