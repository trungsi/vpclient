package com.trungsi.vpclient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;

import com.gargoylesoftware.htmlunit.util.Cookie;

import static com.trungsi.vpclient.VPClient.*;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, String> context = new HashMap<String, String>();
		context.put("driverName", "HtmlUnit");
		context.put("selectedSale", "Spatum Camp");
		context.put("selectedCats", "ralph,lamarthe,massimo,porsche");
		context.put("ignoreSubCats", "junior,garçon,kid");
		context.put("user", "trungsi@hotmail.com");
		context.put("pwd", "trungsi");
		context.put("poolSize", "8");
				
		/*WebDriver driver = loadDriver(context);
		//openSelectedSale(driver, context);
		
		List<Map<String, String>> categories = findAllCategories(driver, context);
		System.out.println(categories);
		Map<String, String> category = categories.get(0);
		List<Map<String, String>> subCategories = findSubCategories(driver, category, context);
		
		Map<String, String> subCategory = subCategories.get(0);
		List<Map<String, String>> articleElems = findAllArticlesInSubCategory(driver, category, subCategory);
		
		Map<String, String> articleElem = articleElems.get(0);
		addArticle(driver, category, subCategory, articleElem, context);
		//Map<String, Object> result = openExpressPurchaseWindow(driver, articleElem);
		//System.out.println(result);
		//addArticleToCart
		
		WebDriver driver2 = new MyHtmlUnitDriver();
		for (org.openqa.selenium.Cookie cookie : driver.manage().getCookies()) {
			driver2.manage().addCookie(cookie);
		}
		addArticle(driver2, category, subCategory, articleElem, context);
		
		//Map<String, Object> result = openExpressPurchaseWindow(driver2, articleElem);
		//System.out.println(result);*/
		
		WebDriver driver = new MyHtmlUnitDriver();
		System.out.println(driver);
	}

}
