/**
 * 
 */
package com.trungsi.vpclient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.openqa.selenium.WebDriver;

/**
 * @author trungsi
 *
 */
public class StreamVPClientAsync {

	static Context context;
	static Sale selectedSale;
	
	static AtomicInteger count;
	public StreamVPClientAsync(Sale selectedSale, Context context) {
		this.selectedSale = selectedSale;
		this.context = context;
		count = new AtomicInteger();
	}
	
	public static void main(String[] args) {
		String vpHome = System.getProperty("user.home") + "/vente-privee";
		Context context = VPGUI.loadContext(vpHome);
		
		List<Sale> saleList = VPClient.getSalesListNew(VPClient.loadDriver(context));
		Optional<Sale> sale = saleList.stream()
			.filter(map -> map.getName().toLowerCase().contains("mcs"))
			.findFirst();
		
		//context.put(VPClient.SELECTED_SALE, "mcs");
		//context.put(VPClient.SELECTED_SALE_DATE, "Jusqu'au mardi 5 mai à 6h");
		
		long start = System.currentTimeMillis();
		StreamVPClientAsync vpClientAsync = new StreamVPClientAsync(sale.get(), context);
		vpClientAsync.execute();
		System.out.println(System.currentTimeMillis() - start + " : " + count.intValue());
		
	}
	
	public void execute() {
		/* loadDrivers(5)
		 * goToSelectedSale()
		 * findCategories()
		 * findSubCategoriesForCategory()
		 * findArticlesForSubCategory()
		 * addArticleToBasket()
		 *   openPurchaseXPressWindow()
		 *   selectPreferredSize()
		 *   addItem()   
		 */
		WebDriver webDriver = loadDriver(0);
		List<WebDriver> drivers = IntStream.range(0, 10)
			.mapToObj(i -> webDriver)
			.map(StreamVPClientAsync::cloneDriver)
			.collect(Collectors.toList());
		
		ForkJoinPool pool = new ForkJoinPool(100);
		try {
			pool.submit(() -> 
			Stream.of(new WebDriverPool(drivers))
				//.parallel()
				.map(StreamVPClientAsync::findCategories)
				.flatMap(l -> l.stream())
				.parallel()
				.map(StreamVPClientAsync::findSubCategories)
				.flatMap(l -> l.stream())
				.map(StreamVPClientAsync::findArticles)
				.flatMap(l -> l.stream())
				.filter(StreamVPClientAsync::filterExclusiveArticles)
				.forEach(StreamVPClientAsync::addArticleToBasket)).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static WebDriver loadDriver(int i) {
		return VPClient.loadDriver(context);
	}
	
	public static WebDriver cloneDriver(WebDriver webDriver) {
		return VPClient.cloneDriver(webDriver, context);
	}
	
	public static List<CategorySelector> findCategories(WebDriverPool wdProvider) {
		return wdProvider.doWithWebDriver2(driver -> 
			VPClient.findAllCategories(driver, selectedSale, context))
			.stream().map(category -> new CategorySelector(category, wdProvider))
			.collect(Collectors.toList());
	}
	
	public static List<SubCategorySelector> findSubCategories(CategorySelector categorySelector) {
		return categorySelector.getWdProvider().doWithWebDriver2(driver -> 
			VPClient.findSubCategories(driver, categorySelector.getCategory(), context))
			.stream().map(subCategory -> new SubCategorySelector(subCategory, categorySelector.getWdProvider()))
			.collect(Collectors.toList());
	}
	
	public static List<ArticleSelector> findArticles(SubCategorySelector subCategorySelector) {
		return subCategorySelector.getWdProvider().doWithWebDriver2(driver -> 
			VPClient.findAllArticlesInSubCategory(driver, subCategorySelector.getSubCategory(), context))
			.stream()
			.map(article -> new ArticleSelector(article, subCategorySelector.getWdProvider()))
			.collect(Collectors.toList());
	}
	
	public static boolean filterExclusiveArticles(ArticleSelector articleSelector) {
		return articleSelector.getWdProvider().doWithWebDriver2(driver -> 
				VPClient.matchFilterArticle(driver, articleSelector.getArticle(), context));
	}
	
	public static void addArticleToBasket(ArticleSelector articleSelector) {
		count.incrementAndGet();
		articleSelector.getWdProvider().doWithWebDriver2(driver -> 
				VPClient.addArticle(driver, articleSelector.getArticle(), context));
	}
}
