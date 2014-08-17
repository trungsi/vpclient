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
import static org.junit.Assert.assertEquals;

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
        List<Map<String, String>> articles = findAllArticles("kwala", "camper");
        //System.out.println(articles);

        openExpressPurchaseWindow(driver, articles.get(0));
        assertTrue(driver.findElements(By.xpath("//iframe[@id=\"viewer\"]")).size() > 0); // viewer in xpress window
        //System.out.println(driver.getPageSource());
    }

    @Test
    public void testGetFamilyAndProductId() {
        List<Map<String, String>> articles = findAllArticles("kwala", "camper");
        //System.out.println(articles);

        openExpressPurchaseWindow(driver, articles.get(0));
        System.out.println(driver.getPageSource());

        String familyId = getFamilyId(driver.getPageSource());
        assertNotNull(familyId);
        assertEquals(24, familyId.length());
        assertTrue(familyId.endsWith("=="));
    }

    @Test
    public void testGetSelectedProductId() {
        String selectedMark = "kwala";
        String selectedCategory = "camper";

        List<Map<String, String>> saleList = getSalesList(driver);
        System.out.println(saleList);

        Map<String, String> selectedSale = getSelectedSale(saleList, selectedMark);
        System.out.println(selectedSale);

        context.put(SELECTED_SALE_DATE, selectedSale.get("dateSales"));
        context.put(SELECTED_SALE_LINK, selectedSale.get("link"));

        List<Map<String, String>> categories = findAllCategories(driver, context);
        //System.out.println(categories);
        Map<String, String> category = getSelectedCategory(categories, selectedCategory);

        List<Map<String, String>> subCategories = findSubCategories(driver, category, context);
        System.out.println(subCategories);

        Map<String, String> subCategory = subCategories.get(0);

        List<Map<String, String>> articles = findAllArticlesInSubCategory(driver, category, subCategory, context);
        Map<String, String> article = articles.get(0);

        openExpressPurchaseWindow(driver, article);

        selectSize(driver, category, subCategory, article, context, "toto");

        System.out.println(driver.getPageSource());

        String selectedProductId = getSelectedProductId(driver.getPageSource());
        assertNotNull(selectedProductId);
        assertEquals(24, selectedProductId.length());
        assertTrue(selectedProductId.endsWith("=="));

        //addArticle(driver, category, subCategory, articles.get(0), context);
    }

    @Test
    public void testAddArticle() {
        String selectedMark = "kwala";
        String selectedCategory = "camper";

        List<Map<String, String>> saleList = getSalesList(driver);
        System.out.println(saleList);

        Map<String, String> selectedSale = getSelectedSale(saleList, selectedMark);
        System.out.println(selectedSale);

        context.put(SELECTED_SALE_DATE, selectedSale.get("dateSales"));
        context.put(SELECTED_SALE_LINK, selectedSale.get("link"));

        List<Map<String, String>> categories = findAllCategories(driver, context);
        //System.out.println(categories);
        Map<String, String> category = getSelectedCategory(categories, selectedCategory);

        List<Map<String, String>> subCategories = findSubCategories(driver, category, context);
        System.out.println(subCategories);

        Map<String, String> subCategory = subCategories.get(0);

        List<Map<String, String>> articles = findAllArticlesInSubCategory(driver, category, subCategory, context);
        Map<String, String> article = articles.get(0);

        addArticle(driver, category, subCategory, article, context);
    }


}
