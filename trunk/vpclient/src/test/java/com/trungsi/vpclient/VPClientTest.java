/**
 * 
 */
package com.trungsi.vpclient;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import static com.trungsi.vpclient.VPClient.*;
import static com.trungsi.vpclient.utils.CollectionUtils.*;
import static org.junit.Assert.*;

/**
 * @author trungsi
 *
 */
public class VPClientTest extends AbstractVPClientTestCase {


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

    @Test
    public void testCloneDriver() {
        WebDriver newDriver = cloneDriver(driver, context);
        System.out.println("==================================");
        for (Cookie cookie : newDriver.manage().getCookies()) {
            System.out.println(cookie);
        }

        String link = "/vp4/MemberAccount/Default.aspx";
        goToLink(newDriver, link);

        assertTrue(newDriver.getCurrentUrl(), newDriver.getCurrentUrl().endsWith(link));
    }

    @Test
    public void testOpenXpressWindow() {
        List<Map<String, String>> articles = findAllArticles("tecnica", "running");
        //System.out.println(articles);

        openExpressPurchaseWindow(driver, articles.get(0));
        assertTrue(driver.findElements(By.xpath("//iframe[@id=\"viewer\"]")).size() > 0); // viewer in xpress window
        System.out.println(driver.getPageSource());
    }


}
