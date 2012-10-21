/**
 * 
 */
package com.trungsi.vpclient;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static com.trungsi.vpclient.VPClient.*;
import static com.trungsi.vpclient.utils.CollectionUtils.*;
import static org.junit.Assert.*;

/**
 * @author trungsi
 *
 */
public class VPClientTest {

	private WebDriver driver;
	
	@Before
	public void setUp() {
		Map<String, String> context = map(
				entry(DRIVER_NAME, HTML_UNIT), 
				entry(USER, "trungsi@hotmail.com"), 
				entry(PWD, "trungsi"));
		driver = loadDriver(context);
	}
	
	@Test
	public void testGetSalesList() {
		List<Map<String, String>> salesList = getSalesList(driver);
		assertFalse(salesList.isEmpty());
		Map<String, String> sale = salesList.get(0);
		//System.out.println(driver.getPageSource());
		assertNotNull(sale.get("link"));
		assertNotNull(sale.get("dateSales"));
		System.out.println(salesList);
		for (Map<String, String> otherSale : salesList) {
			assertFalse(sale.get("name").isEmpty());
			String dateSales = otherSale.get("dateSales");
			System.out.println(dateSales);
			assertFalse(dateSales.isEmpty());
		}
	}
}
