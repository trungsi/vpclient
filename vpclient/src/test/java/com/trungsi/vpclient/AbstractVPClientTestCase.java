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

    protected Context context;

    @Before
    public void setUp() {
        context = new Context();
        context.put(
                Context.DRIVER_NAME, Context.HTML_UNIT);
        loadDriverForTest("dtht2000@yahoo.com", "dtht2000");
    }

    protected void loadDriverForTest(String user, String pwd) {
        context.put(Context.USER, user);
        context.put(Context.PWD, pwd);

        driver = loadDriver(context);
    }

    protected Sale getSelectedSale(List<Sale> saleList, String selectedMark) {
        for (Sale sale : saleList) {
            if (sale.getName().toLowerCase().contains(selectedMark)) {
                return sale;
            }
        }

        throw new RuntimeException("No sale found for " + selectedMark);
        //return null;
    }

    protected List<Article> findAllArticles(String selectedMark, String selectedCategory) {
        List<Sale> saleList = getSalesList(driver);

        Sale selectedSale = getSelectedSale(saleList, selectedMark);

        List<Category> categories = findAllCategories(driver, selectedSale, context);
        //System.out.println(categories);
        Category category = getSelectedCategory(categories, selectedCategory);

        List<SubCategory> subCategories = findSubCategories(driver, category, context);

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
