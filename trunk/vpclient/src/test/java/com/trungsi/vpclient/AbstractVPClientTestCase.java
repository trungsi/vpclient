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

    protected Map<String, String> getSelectedSale(List<Map<String, String>> saleList, String selectedMark) {
        for (Map<String, String> sale : saleList) {
            if (sale.get("name").toLowerCase().contains(selectedMark)) {
                return sale;
            }
        }

        throw new RuntimeException("No sale found for " + selectedMark);
        //return null;
    }

    protected List<Article> findAllArticles(String selectedMark, String selectedCategory) {
        List<Map<String, String>> saleList = getSalesList(driver);
        System.out.println(saleList);

        Map<String, String> selectedSale = getSelectedSale(saleList, selectedMark);
        System.out.println(selectedSale);

        context.put(SELECTED_SALE_DATE, selectedSale.get("dateSales"));
        context.put(SELECTED_SALE_LINK, selectedSale.get("link"));

        List<Category> categories = findAllCategories(driver, context);
        //System.out.println(categories);
        Category category = getSelectedCategory(categories, selectedCategory);

        List<SubCategory> subCategories = findSubCategories(driver, category, context);
        System.out.println(subCategories);

        return findAllArticlesInSubCategory(driver, subCategories.get(0), context);

    }

    Category getSelectedCategory(List<Category> categories, String selectedCategory) {
        for(Category category : categories) {
            System.out.println(category.getName() + " : " + selectedCategory);
            if (category.getName().toLowerCase().contains(selectedCategory)) {
                return category;
            }
        }

        return null;
    }
}
