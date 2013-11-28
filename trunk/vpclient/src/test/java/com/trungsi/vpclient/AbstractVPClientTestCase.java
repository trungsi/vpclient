package com.trungsi.vpclient;

import org.junit.Before;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.Map;

import static com.trungsi.vpclient.VPClient.*;
import static com.trungsi.vpclient.VPClient.loadDriver;
import static com.trungsi.vpclient.utils.CollectionUtils.entry;
import static com.trungsi.vpclient.utils.CollectionUtils.map;

/**
 * Created with IntelliJ IDEA.
 * User: trungsi
 * Date: 28/10/2013
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractVPClientTestCase {
    protected WebDriver driver;

    protected Map<String, String> context;

    @Before
    public void setUp() {
        context = map(
                entry(DRIVER_NAME, HTML_UNIT));
        loadDriverForTest("dtht2000@yahoo.com", "dtht2000");
    }

    protected void loadDriverForTest(String user, String pwd) {
        context.put(USER, user);
        context.put(PWD, pwd);

        driver = loadDriver(context);
    }

    protected Map<String, String> getSelectedSale(List<Map<String, String>> saleList, String skate) {
        for (Map<String, String> sale : saleList) {
            if (sale.get("name").toLowerCase().contains(skate)) {
                return sale;
            }
        }

        return null;
    }

    protected List<Map<String, String>> findAllArticles(String selectedMark, String selectedCategory) {
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

        return findAllArticlesInSubCategory(driver, category, subCategories.get(0), context);

    }

    Map<String, String> getSelectedCategory(List<Map<String, String>> categories, String selectedCategory) {
        return getSelectedSale(categories, selectedCategory);
    }
}