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
		List<Sale> salesList = VPClient.getSalesList(driver);
		assertFalse(salesList.isEmpty());
		Sale sale = salesList.get(0);
		//System.out.println(driver.getPageSource());
		assertNotNull(sale.getLink());
		assertNotNull(sale.getDatesSale());
		System.out.println(salesList);
		for (Sale otherSale : salesList) {
			assertFalse(sale.getName().isEmpty());
			String dateSales = otherSale.getDatesSale();
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
    public void testFindAllCategories() {
    	Sale selectedSale = chooseASale("mcs");
    	
    	long start = System.currentTimeMillis();
    	List<Category> categories = findAllCategoriesOfSelectedSale(selectedSale);
    	System.out.println(System.currentTimeMillis() - start);
    }
    
    @Test
    public void testFindAllArticles() {
    	Sale selectedSale = chooseASale("mcs");
    	
    	Category selectedCategory = chooseACategory(selectedSale);
        System.out.println(selectedCategory);

        SubCategory subCategory = chooseASubCategory(selectedCategory);
        System.out.println(subCategory);

        long start = System.currentTimeMillis();
        findAllArticlesInSubCategory(driver, subCategory, context);
        System.out.println(System.currentTimeMillis() - start);
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
        Sale selectedSale = chooseASale();
        System.out.println( selectedSale );

        return findAllArticlesInSubCategoriesForAsSelectedSale(selectedSale);

    }

    private List<Article> findAllArticles(String saleName) {
        Sale selectedSale = chooseASale(saleName);
        System.out.println( selectedSale );

        return findAllArticlesInSubCategoriesForAsSelectedSale(selectedSale);

    }

	private List<Article> findAllArticlesInSubCategoriesForAsSelectedSale(
			Sale selectedSale) {
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

    private Category chooseACategory(Sale selectedSale) {
        List<Category> categories = findAllCategoriesOfSelectedSale(selectedSale);

        return randomSelect(categories);
    }

	private List<Category> findAllCategoriesOfSelectedSale(
			Sale selectedSale) {
		
        List<Category> categories = findAllCategories(driver, selectedSale, context);
		return categories;
	}

    private Sale chooseASale() {
        List<Sale> salesList = getSalesList();
        return randomSelect(salesList);
    }

    private Sale chooseASale(String saleName) {
        List<Sale> salesList = getSalesList();
        return salesList.stream()
        		.filter(sale -> sale.getName().toLowerCase().contains(saleName.toLowerCase()))
        		.findFirst().get();
    }
    
	private List<Sale> getSalesList() {
		List<Sale> salesList = VPClient.getSalesList(driver);
        Iterator<Sale> iter = salesList.iterator();
        while (iter.hasNext()) {
            Sale sale = iter.next();
            if (sale.getName().contains("One Day") || sale.getDatesSale().startsWith("A partir")) {
                iter.remove();
            }
        }
		return salesList;
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
