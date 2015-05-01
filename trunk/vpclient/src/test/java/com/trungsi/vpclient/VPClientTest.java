/**
 * 
 */
package com.trungsi.vpclient;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import static com.trungsi.vpclient.VPClient.*;
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

        String link = "/memberaccount/memberaccount";
        goToLink(newDriver, link);

        assertTrue(newDriver.getCurrentUrl(), newDriver.getCurrentUrl().endsWith(link));
    }

    @Test
    public void testOpenXpressWindow() {
        List<Article> articles = findAllArticles();
        //System.out.println(articles);

        openExpressPurchaseWindow(driver, articles.get(0));
        assertTrue(driver.getPageSource(), driver.findElements(By.xpath("//a[@id=\"addToCartLink\"]")).size() > 0); // viewer in xpress window
        //System.out.println(driver.getPageSource());
    }

    private List<Article> findAllArticles() {
        Map<String, String> selectedSale = chooseASale();
        System.out.println( selectedSale );

        Category selectedCategory = chooseACategory(selectedSale);
        System.out.println(selectedCategory);

        SubCategory subCategory = chooseASubCategory(selectedCategory);
        System.out.println(subCategory);

        return findAllArticlesInSubCategory(driver, subCategory, context);

    }

    private SubCategory chooseASubCategory(Category selectedCategory) {
        List<SubCategory> subCategories = findSubCategories(driver, selectedCategory, context);

        return randomSelect(subCategories);
    }

    private Category chooseACategory(Map<String, String> selectedSale) {
        context.put(SELECTED_SALE_DATE, selectedSale.get("dateSales"));
        context.put(SELECTED_SALE_LINK, selectedSale.get("link"));

        List<Category> categories = findAllCategories(driver, context);

        return randomSelect(categories);
    }

    private Map<String, String> chooseASale() {
        List<Map<String, String>> salesList = getSalesList(driver);
        Iterator<Map<String, String>> iter = salesList.iterator();
        while (iter.hasNext()) {
            Map<String, String> sale = iter.next();
            if (sale.get("name").contains("One Day") || sale.get("dateSales").startsWith("A partir")) {
                iter.remove();
            }
        }
        return randomSelect(salesList);
    }

    private <T> T randomSelect(List<T> salesList) {
        return salesList.get((int)(Math.random()*salesList.size()));  //To change body of created methods use File | Settings | File Templates.
    }

    @Test
    public void testGetFamilyAndProductId() {
        List<Article> articles = findAllArticles();
        //System.out.println(articles);

        openExpressPurchaseWindow(driver, articles.get(0));
        //System.out.println(driver.getPageSource());

        String[] familyAndProductId = getFamilyAndProductId(driver);
        assertNotNull(familyAndProductId[0]);
        assertNotNull(familyAndProductId[1]);
    }

    @Test
    public void testAddArticle() {
        List<Article> articles = findAllArticles();

        addArticle(driver, articles.get(0), context);
    }


}
