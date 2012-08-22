package com.trungsi.vpclient;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author trungsi
 *
 */
public class VPClient {

	private static final Logger LOG = Logger.getLogger(VPClient.class);
	
	public static final String PWD = "pwd";
	public static final String USER = "user";
	public static final String HTML_UNIT = "HtmlUnit";
	public static final String DRIVER_NAME = "driverName";
	public static final String IGNORE_SUB_CATS = "ignoreSubCats";
	public static final String SELECTED_CATS = "selectedCats";
	public static final String SELECTED_SALE = "selectedSale";
	
	public static final String WOMAN_JEAN_SIZES = "womanJeanSizes";
	public static final String WOMAN_SHOES_SIZES = "womanShoesSizes";
	public static final String WOMAN_LINGERIE_SIZES = "womanLingerieSizes";
	public static final String WOMAN_CLOTHING_SIZES = "womanClothingSizes";
	public static final String GIRL_SHOES_SIZES = "girlShoesSizes";
	public static final String GIRL_CLOTHING_SIZES = "girlClothingSizes";
	public static final String MAN_JEAN_SIZES = "manJeanSizes";
	public static final String MAN_SHOES_SIZES = "manShoesSizes";
	public static final String MAN_COSTUME_SIZES = "manCostumeSizes";
	public static final String MAN_CLOTHING_SIZES = "manClothingSizes";
	

	public static WebDriver loadDriver(Map<String, String> context) {
		WebDriver driver = newDriver(context);
		
		boolean loggedin = false;
		do {
			loggedin = login(driver, context);
		} while (!loggedin);

		return driver;
	}

	public static WebDriver cloneDriver(WebDriver driver, Map<String, String> context) {
		WebDriver newDriver = newDriver(context);
		newDriver.get(baseUrl);
		
		for (Cookie cookie : driver.manage().getCookies()) {
			newDriver.manage().addCookie(cookie);
		}
		
		return newDriver;
	}
	public static WebDriver newDriver(Map<String, String> context) {
		WebDriver driver = null;
		String driverName = context.get(DRIVER_NAME);

		//println(driverName);
		
		if (driverName.equals(HTML_UNIT)) {
			driver = new MyHtmlUnitDriver();
		}
		return driver;
	}

	public static void sleep (long timeout) {
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static String baseUrl = "http://fr.vente-privee.com";

	private static boolean login(WebDriver driver, Map<String, String> context) {
		driver.get(baseUrl + "/vp4/Login/Portal.ashx");
		//println driver.title
		sleep (20);

		WebElement emailElem = driver.findElement(By.name("txtEmail"));
		emailElem.sendKeys(context.get(USER));
		sleep(20);

		WebElement pwdElem = driver.findElement(By.name("txtPassword"));
		pwdElem.sendKeys(context.get(PWD));
		sleep(20);

		WebElement submitElem = driver.findElement(By.name("btSubmit"));

		submitElem.click();

		sleep(20);

		return checkLoggedIn(driver);
	}

	private static boolean checkLoggedIn(WebDriver driver) {
		return driver.findElements(By.xpath("//ul[@class=\"currentlySales\"]")).size() == 1;
	}

	private static void openSelectedSale(WebDriver driver, Map<String, String> context) {
		if (Thread.currentThread().isInterrupted()) return;
		
		try {
			long start = System.currentTimeMillis();
	
			List<WebElement> currentSalesElem = findCurrentSaleList(driver);
			WebElement selectedElem = getSelectedSaleFromList(currentSalesElem, context);
	
			selectedElem.click();
	
			
				waitForElementReady("//div[@class=\"obj_menuEV\"]", 5000L, driver);
			
	
			long time = System.currentTimeMillis() - start;
			log(driver + " goToSelectedSale : " + time);
		} catch (Error e) {
			if (driver.getPageSource().contains(context.get(SELECTED_SALE))) {
				log(driver + " cannot open selectedSale, try to refresh");
				driver.navigate().refresh();
				openSelectedSale(driver, context);
			} else {
				throw e;
			}
		}
	}

	public static List<WebElement> findCurrentSaleList(WebDriver driver) {
		long start = System.currentTimeMillis();
		List<WebElement> currentSalesElem = driver.findElements(By.xpath("//ul[@class=\"currentlySales\"]//a[@id=\"linkSale\"]"));
		if (currentSalesElem.isEmpty()) {
			throw new Error("No item in sale\n" /*+ driver.getPageSource()*/);
		}

		long time = System.currentTimeMillis() - start;
		log(driver + " findCurrentSaleList : " + time);

		return currentSalesElem;
	}

	public static WebElement getSelectedSaleFromList(List<WebElement> currentSaleList,Map<String, String> context) {
		long start = System.currentTimeMillis();

		WebElement selectedElem = null;
		for (WebElement elem : currentSaleList) {
			if (!elem.findElements(
					By.xpath("h4[text()=\"" + context.get(SELECTED_SALE) + "\"]/..")).isEmpty()) {
				selectedElem = elem;
				break;
			}
		}


		long time = System.currentTimeMillis() - start;
		log("getSelectedSaleFromList : " + time);

		if (selectedElem == null) {
			throw new Error("No sale " + context.get(SELECTED_SALE) + " found\n" + currentSaleList);
		}

		return selectedElem;
	}

	public static void waitForElementReady(Object xpathOrClosure, long timeout, WebDriver driver) {
		long wait = 0;
		long interval = 200;
		while (wait < timeout) {
			if (evaluateXpathOrClosure(xpathOrClosure, driver)) {
				return;
			}

			sleep(interval);
			wait += interval;
		}

		throw new ElementNotReadyException("Cannot find element " + xpathOrClosure + " after " + timeout + " in \n " + driver.getPageSource());
	}

	public static class ElementNotReadyException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public ElementNotReadyException(String string) {
			super(string);
		}
	}
	
	public static boolean evaluateXpathOrClosure (Object xpathOrClosure,WebDriver driver) {
		//println("xpathOrClosure " + xpathOrClosure.getClass())
		if (xpathOrClosure instanceof String) {
			return !driver.findElements(By.xpath(xpathOrClosure.toString())).isEmpty();
		} else {
			// Function[?, ?] ???
			return ((Function)xpathOrClosure).apply();
		}

	}

	public static interface Function {
		boolean apply();
	}

	public static void log(String msg) {
		msg = new Date() + " : " + msg;
		//println(msg);
		LOG.info(msg);
	}

	public static void println(Object obj) {
		LOG.debug(obj);
	}

	public static boolean listContains(List<String> list, String text) {
		for (String elem : list) {
			if (text.contains(elem)) {
				return true;
			}
		}
		return false;
	}

	public static <T> List<T> list(T... ts) {
		ArrayList<T> list = new ArrayList<T>();
		for (T t : ts) {
			list.add(t);
		}

		return list;
	}

	public static <K, V> Map<K, V> map(Entry<K, V>... entries) {
		HashMap<K, V> map = new HashMap<K, V>();
		for (Entry<K, V> entry : entries) {
			map.put(entry.key, entry.value);
		}

		return map;
	}

	public static class Entry<K, V> {
		public K key;
		public V value;

		public Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}

	@SuppressWarnings("all")
	public static <K, V> Entry<K, V> entry(K key, V value) {
		return new Entry(key, value);
	}

	@SuppressWarnings("all")
	public static List<Map<String, String>> findAllCategories(WebDriver driver, Map<String, String> context) {
		long start = System.currentTimeMillis();

		openSelectedSale(driver, context);

		List<WebElement> catElems = driver.findElements(By.xpath("//ul[@class=\"menuEV\"]/li/a/span/..")); // car premier <a> sans <span> n'est pas intÃ©ressant

		if (catElems.isEmpty()) {
			throw new Error("No category found for marque " + context.get(SELECTED_SALE));
		}

		List<Map<String, String>> categories = filterCategories(catElems, context);

		log(driver + " " + categories.size() + " categories found : \n" + categories);

		long time = System.currentTimeMillis() - start;
		log(driver + " findAllCategories : " + time);

		return categories;
	}

	@SuppressWarnings("all")
	private static List<Map<String, String>> filterCategories(
			List<WebElement> catElems, Map<String, String> context) {
		
		List<Map<String, String>> categories = new ArrayList<Map<String, String>>();
		List<String> selectedCats = getSelectedCategories(context);
		
		for (WebElement catElem : catElems) {
			if (isSelectedCategory(selectedCats, catElem)) {
				categories.add(
						map(entry("name", catElem.getText()),
							entry("link", catElem.getAttribute("href"))));
			}
		}
		
		
		return categories;
	}

	private static boolean isSelectedCategory(List<String> selectedCats, WebElement catElem) {
		return selectedCats.isEmpty() || (listContains(selectedCats, catElem.getText().toLowerCase()));
	}
	
	private static List<String> getSelectedCategories(
			Map<String, String> context) {
		String selectedCatsString = context.get(SELECTED_CATS);
		if (selectedCatsString != null && !selectedCatsString.equals("")) {
			return list(selectedCatsString.split("\\|"));
		}
		
		return list();
	}

	private static List<String> getIgnoreSubCategories(Map<String, String> context) {
		String ignoreSubCatsString = context.get(IGNORE_SUB_CATS);
		List<String> ignoreCats = ignoreSubCatsString != null && !ignoreSubCatsString.equals("") ? 
				list(ignoreSubCatsString.split("\\|")) : new ArrayList<String>();
		return ignoreCats;
	}

	/*private static void addArticlesInCategory (WebDriver driver,Map<String, String> category, Map<String, String> context) {
		long start = System.currentTimeMillis();

		List<Map<String, String>> subCategories = findSubCategories(driver, category, context);
		for (Map<String, String> subCategory : subCategories) {
			if (Thread.currentThread().isInterrupted()) {
				println("Stopped");
				return;
			}
			addArticlesInSubCategory(driver, category, subCategory);
		}

		long time = System.currentTimeMillis() - start;
		log("addArticlesInCategory : " + time);
	}*/

	/*private static void addArticlesInSubCategory(WebDriver driver, Map<String, String> category, Map<String, String> subCategory, Map<String, String> context) {
		long start = System.currentTimeMillis();

		List<Map<String, String>> articleElems = findAllArticlesInSubCategory(driver, category, subCategory);
		for (Map<String, String> articleElem : articleElems) {
			if (Thread.currentThread().isInterrupted()) {
				println("Stopped");
				return;
			}
			addArticle(driver, category, subCategory, articleElem, context);
		}

		long time = System.currentTimeMillis() - start;
		log("addArticles : " + time);
	}*/

	public static boolean addArticle(WebDriver driver, Map<String, String> category, 
			Map<String, String> subCategory, Map<String, String> article, Map<String, String> context) {
		long start = System.currentTimeMillis();

		boolean added = false;
		//String mainWindowHandle = driver.getWindowHandle();
		try {
			Map<String, Object> result = openExpressPurchaseWindow(driver, article);
			if ((Boolean)result.get("ok")) {
				added = addArticleToCart(driver, category, subCategory, article, context);
			} else {
				log(driver + " Cannot add article $articleName in category " + category.get("name") + ".\nCause :" + result.get("message"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			//println(driver.getPageSource)
		} finally {
			// en cas de HtmlUnit, il faut re-dÃ©sactiver javascript et ne pas fermer la fenÃªtre
			if (driver instanceof HtmlUnitDriver) {
				((HtmlUnitDriver)driver).setJavascriptEnabled(false);
			} else {
				driver.close();
				println ("window close");
				sleep (50);
			}

			//switchToWindow(driver, mainWindowHandle);
		}

		long time = System.currentTimeMillis() - start;
		log(driver + " addArticle : " + time);
		
		return added;
	}

	private static boolean addArticleToCart (WebDriver driver, Map<String, String> category, 
			Map<String, String> subCategory, Map<String, String> article, Map<String, String> context) {
		long start = System.currentTimeMillis();
		String info = category.get("name") + "|" + subCategory.get("name") + "|" + article.get("name") + " : ";
		try {
			List<WebElement> addToCartBt = driver.findElements(By.id("addToCart"));
			if (addToCartBt.isEmpty()) {
				//println(driver.getPageSource());
				List<WebElement> elems = driver.findElements(By.id("product_pUnavailable"));
				if (!elems.isEmpty()) {
					log(driver + " " + info + elems.get(0).getText());
				} else {
					log(driver + " " + info + "No addToCart button found, the article must be sold");
				}
				return false;
			}

			List<Map<String, String>> selectableSizes = null;
			List<WebElement> selectElems = driver.findElements(By.id("productId"));
			//sleep(500);
			if (selectElems.isEmpty() || !selectElems.get(0).getTagName().equals("select")) {
				log(driver + " " + info + " No model/size found. The article must not have this info");
				List<WebElement> productSize = driver.findElements(By.xpath("//p[@id='product_pUniqueModelRow']/span"));
				if (!productSize.isEmpty()) {
					String sizeText = productSize.get(0).getText();
					//println("sizeText=" + sizeText);
					List<String> preferedSize = getPreferedSize(driver, category, subCategory, article, context);
					boolean match = listContains(preferedSize, sizeText);
					log(driver + " + sizeText=" + sizeText + " match (" + match + ") in " + preferedSize);
					if (sizeText.contains("T.") 
							&& !match) {
						log(driver + " " + info + " size " + sizeText + " not in " + preferedSize);
						return false;
					}
				}
				LOG.debug("Unique size model ??? \n" + driver.getPageSource());
			} else {
				selectableSizes = getMostAppropriateSizes(driver, category, subCategory, article, context, selectElems.get(0));
			}

			//if (selectableSizes != null && selectableSizes.size() > 1) {
			// comment faire ???
			//} else {
			if (selectableSizes == null || selectableSizes.size() >= 1) {
				if (selectableSizes != null) {
					log(driver + " " + info + selectableSizes.get(selectableSizes.size()-1));
					//driver.navigate().to(baseUrl + "/vp4/Catalog/WebServices/Cart.asmx/AddProduct") 
					//MyHtmlUnitDriver htmlUnitDriver = (MyHtmlUnitDriver) driver;
					//htmlUnitDriver.postRequest(
					//		baseUrl + "/vp4/Catalog/WebServices/Cart.asmx/AddProduct", 
					//		"{\"productId\" : \"" + selectableSizes.get(selectableSizes.size()-1).get("value") + "\", \"familyId\" : \"" + driver.findElement(By.id("familyId")).getValue() + "\", \"quantity\" : \"1\"}");
				} else {
					log(driver + " " + info + " has no size");
										
				}
				
				addToCartBt.get(0).click();
				
				sleep(100);

				List<WebElement> resultBlocs = driver.findElements(By.xpath("//p[@id=\"resultBloc\"]"));

				if (!resultBlocs.isEmpty() && resultBlocs.get(0).isDisplayed()) {
					log(driver + " " + resultBlocs.get(0).getText());
				}

				List<WebElement> validResultBlocs = driver.findElements(By.xpath("//p[@id=\"validResultBloc\"]"));
				if (validResultBlocs.isEmpty() || !validResultBlocs.get(0).isDisplayed()) {
					log(driver + " " + info + "No confirmation after add article to cart");
				} else {
					log(driver + " " + info +" ADDED");
				}

				return true;
				
			} else {
				log(driver + " " + info + " No appropriate size");
				return false;
			}

		} finally {
			long time = System.currentTimeMillis() - start;
			log(driver + " addArticleToCart : " + info + time);
		}
	}

	private static List<Map<String, String>> getMostAppropriateSizes(WebDriver driver, Map<String, String> category, 
			Map<String, String> subCategory, Map<String, String> article, 
			Map<String, String> context, WebElement selectElem) {
		Select select = new Select(selectElem);
		
		List<Map<String, String>> selected = selectMostAppropriateSizes(driver, category, subCategory, article, context, select);
		
		return selected;

	}

	/*private static void switchToWindow(WebDriver driver, String mainWindowHandle) {
		println ("switch to " + mainWindowHandle);
		driver.switchTo().window(mainWindowHandle);
	}*/

	private static List<Map<String, String>> selectMostAppropriateSizes (WebDriver driver, Map<String, String> category, 
			Map<String, String> subCategory, Map<String, String> article, Map<String, String> context, Select select) {
		List<String> preferedSize = getPreferedSize(driver, category, subCategory, article, context);
		if (preferedSize.isEmpty()) {
			return new ArrayList<Map<String, String>>();
		} else {
			return selectSize(select, preferedSize);
		}
	}

	private static String getDefault(String name, Map<String, String> context, String defaultValue) {
		String value = context.get(name);
		return (value == null || value.equals("")) ? defaultValue : value;
	}
	
	private static List<String> getWomanJeanSizes(Map<String, String> context) {
		return list(getDefault(WOMAN_JEAN_SIZES, context, " 26 |W26|T.36|T. 36").split("\\|"));
	}
	
	private static List<String> getWomanShoesSizes(Map<String, String> context) {
		return list(getDefault(WOMAN_SHOES_SIZES, context, " 37 |T.37").split("\\|"));
	}
	
	private static List<String> getWomanLingerieSizes(Map<String, String> context) {
		return list(getDefault(WOMAN_LINGERIE_SIZES, context, "90A").split("\\|"));
	}
	
	private static List<String> getWomanClothingSizes(Map<String, String> context) {
		return list(getDefault(WOMAN_CLOTHING_SIZES, context, " 36 |T.36 (FR)|T.36 |T. 36|34/36| S |.S ").split("\\|"));
	}
	
	private static List<String> getGirlShoesSizes(Map<String, String> context) {
		return list(getDefault(GIRL_SHOES_SIZES, context, " 23 |T.23|T. 23").split("\\|"));
	}
	
	private static List<String> getGirlClothingSizes(Map<String, String> context) {
		return list(getDefault(GIRL_CLOTHING_SIZES, context, "3 ans").split("\\|"));
	}
	
	private static List<String> getManJeanSizes(Map<String, String> context) {
		return list(getDefault(MAN_JEAN_SIZES, context, " 30 |W30|T.30|T.40|T. 40").split("\\|"));
	}
	
	private static List<String> getManShoesSizes(Map<String, String> context) {
		return list(getDefault(MAN_SHOES_SIZES, context, "40.5| 41 |T.41|T. 41").split("\\|"));
	}
	
	private static List<String> getManCostumeSizes(Map<String, String> context) {
		return list(getDefault(MAN_COSTUME_SIZES, context, " M |.M |T.40|T. 40").split("\\|"));
	}
	
	private static List<String> getManClothingClothingSizes(Map<String, String> context) {
		return list(getDefault(MAN_CLOTHING_SIZES, context, " M |.M | 38 | 40 |T.40|T. 40").split("\\|"));
	}
	
	private static List<String> getPreferedSize(WebDriver driver, Map<String, String> category, 
			Map<String, String> subCategory, Map<String, String> article, Map<String, String> context) {
		String articleInfo = getArticleInfo(driver, category, subCategory, article); 
		
		if (isManArticle(articleInfo)) {
			if (isJean(articleInfo)) {
				return getManJeanSizes(context);
			} else if (isShoes(articleInfo)) {
				return getManShoesSizes(context);
			} else if (isCostume(articleInfo)) {
				return getManCostumeSizes(context);
			} else {
				return getManClothingClothingSizes(context);
			}
		}
		if (isWomanArticle(articleInfo)) {
			if (isJean(articleInfo)) {
				return getWomanJeanSizes(context);
			} else if (isShoes(articleInfo)) {
				return getWomanShoesSizes(context) ;
			} else if (isSoutienGorge(articleInfo)) {
				return getWomanLingerieSizes(context);
			} else {
				return getWomanClothingSizes(context);
			}
		} else if (isGirlArticle(articleInfo)) {
			if (isShoes(articleInfo)) {
				return getGirlShoesSizes(context);
			} else {
				return getGirlClothingSizes(context);
			}
		} else if (isBoyArticle(articleInfo)) {
			// ne fait rien
			return new ArrayList<String>();
		} else {
			if (isJean(articleInfo)) {
				return getManJeanSizes(context);
			} else if (isShoes(articleInfo)) {
				return getManShoesSizes(context);
			} else if (isCostume(articleInfo)) {
				return getManCostumeSizes(context);
			} else {
				return getManClothingClothingSizes(context);
			}
		}
	}
	
	private static boolean isManArticle(String articleInfo) {
		return articleInfo.contains("homme");
	}

	private static boolean isSoutienGorge(String articleInfo) {
		return articleInfo.contains("soutien");
	}

	private static boolean isCostume(String articleInfo) {
		return listContains(list("costume", "veste"), articleInfo);
	}

	private static String getArticleInfo(WebDriver driver, Map<String, String> category, 
			Map<String, String> subCategory, Map<String, String> article) {
		return (category.get("name") + "|" + subCategory.get("name") 
				+ (subCategory.get("femme") != null ? "femme" :
					subCategory.get("homme" != null ? "homme" : ""))
				+ "|" + article.get("name") + "|" + getArticleDetail(driver)).toLowerCase();
	}

	private static boolean isWomanArticle(String articleInfo) {
		return isSaleForWomanOnly() || listContains(list("femme", "woman", "women", "jupe", "robe", "soutien", "lingerie", "body", "bodies", "collant", "legging", "chemisier"), articleInfo);
	}

	private static boolean isGirlArticle(String articleInfo) {
		return articleInfo.contains("fille");
	}

	private static boolean isBoyArticle(String articleInfo) {
		return articleInfo.contains("garçon");
	}

	private static String getArticleDetail(WebDriver driver) {
		String source = driver.getPageSource();
		int index = source.indexOf("xtpage = \"") + 10;
		if (index >= 10) {
			source = source.substring(index);
			int endIndex = source.indexOf("\"");
			return source.substring(0, endIndex);
		} else {
			return "";
		}
	}

	private static boolean isSaleForWomanOnly() {
		//context.forWomanOnly
		return false;
	}

	@SuppressWarnings("all")
	private static List<Map<String, String>> selectSize(Select select, List<String> selectSizeList) {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>(); 

		List<WebElement> options = select.getOptions();

		for ( int i = 0; i < options.size(); i++) {
			WebElement option = options.get(i);

			String optionText = option.getText();

			//println("optionText = " + optionText + " in " + selectSizeList);

			if (listContains(selectSizeList, optionText)) {
				log("selected index " + i + ", " + optionText + ", " + selectSizeList);
				select.selectByIndex(i);
				//return optionText
				results.add(map(entry("name", optionText), entry("value", option.getAttribute("value"))));
				
				break; // first match
			}
		}

		return results;
	}

	private static boolean isJean (String articleInfo) {
		return articleInfo.contains("jean");
	}

	private static boolean isShoes (String articleInfo) {
		return listContains(
				list("chaussure", "basket", "sneaker", "derbie", 
					"richelieu", "moscassin", "botte", "bottine", "sandale", 
					"ballerine", "escarpin", "tong", "mule"), 
				articleInfo);
	}

	@SuppressWarnings("all")
	public static Map<String, Object> openExpressPurchaseWindow(final WebDriver driver, Map<String, String> articleElem) {
		long start = System.currentTimeMillis();
		try {
			
			String openNewWindow = articleElem.get("link");
			//println("openWindow = " + openNewWindow);
			
			// must activate javascript because will submit form using ajax
			HtmlUnitDriver htmlUnitDriver = (HtmlUnitDriver) driver;
			htmlUnitDriver.setJavascriptEnabled(true);
			
			htmlUnitDriver.navigate().to(openNewWindow);
			//sleep(500);
			return map(entry("ok", (Object) java.lang.Boolean.TRUE));
		} finally {
			long time = System.currentTimeMillis() - start;
			log(driver + " openExpressPurchaseWindow : " + time);
		}
	}

	/*private static boolean switchToExpressWindow(WebDriver driver) {
		try {
			switchToWindow(driver, "express");
			return true;
		} catch (Exception e){
			println (e);
			return false;
		}
	}*/

	/*private static String getArticleName(Map<String, String> category,WebElement  articleElem) {
		long start = System.currentTimeMillis();

		try {
			return articleElem.findElement(By.xpath("div[@class=\"infoArt\"]/div[@class=\"infoArtTitle\"]")).getText();
		} catch (Exception e) {
			throw new Error("Cannot find article name in category " + category.get("name") + " in element " + articleElem.getText(), e);
		} finally {
			long time = System.currentTimeMillis() - start;
			log("getArticleName : " + time);
		}
	}*/

	public static List<Map<String, String>> findAllArticlesInSubCategory(WebDriver driver, Map<String, String> category, Map<String, String> subCategory) {
		long start = System.currentTimeMillis();

		// go to subCategory page
		if (subCategory != null) {
			String link = subCategory.get("link");
			if (!link.startsWith("http")) {
				link = baseUrl + link;
			}
			driver.navigate().to(link);
		}

		//waitForElementReady("//ul[@class=\"artList\"]/script", 5000L, driver)
		sleep(1000);

		/*List<WebElement> scriptElems = driver.findElements(By.xpath("//ul[@class=\"artList\"]/script"));
		if (scriptElems.size() > 0) {
			if (scriptElems.get(0).getText() == "") {
				//println(driver.getPageSource())
			}
		} else {
			//println(driver.getPageSource())
			println("toto");
		}*/

		List<WebElement> articleElems = driver.findElements(By.xpath("//ul[@class=\"artList viewAllProduct\"]/li"));

		List<Map<String, String>> articles = new ArrayList<Map<String, String>>();
		if (articleElems.isEmpty()) {
			log(driver + " Cannot find class 'artList viewAllProduct'. Will parse Json to get article infos");
			
			String source = driver.getPageSource();

			int index = source.indexOf("JSon=");
			source = source.substring(index+5);

			index = source.indexOf("</script>");
			source = source.substring(0, index);

			index = source.lastIndexOf(";");
			source = source.substring(0, index);
			try {
				
				String currentUrl = driver.getCurrentUrl();
				int lastIndex = currentUrl.lastIndexOf("/");
				currentUrl = currentUrl.substring(0, lastIndex+1);
				
				JSONObject json = new JSONObject(source);
				JSONObject catalog = json.getJSONObject("catalog");
				JSONObject dataCatalog = catalog.getJSONObject("dataCatalog");
				JSONArray items = dataCatalog.getJSONArray("items");
				for (int i = 0; i < items.length(); i++) {
					Map<String, String> map = new HashMap<String, String>();
					JSONObject item = items.getJSONObject(i); 
					//System.out.println(item);
					map.put("name", item.getString("infoArtTitle"));
					map.put("link", currentUrl + "FEikId" + item.getString("artURL") + ".aspx");
					articles.add(map);
				}

			} catch (Exception e) {
				LOG.error(driver + " Error on parsing json " + source, e);
				//e.printStackTrace();
			}
			//println(driver.getPageSource())

		}

		// detecte woman|men articles
		String catSubCat = (category.get("name") + subCategory).toLowerCase();
		if (!catSubCat.contains("femme") && !catSubCat.contains("homme")) {
			String source = driver.getPageSource();
			if (source.contains("Tailles femme")) {
				subCategory.put("femme", "true");
			} else if (source.contains("Tailles homme")) {
				subCategory.put("homme", "true");
			}
		}
		
		long time = System.currentTimeMillis() - start;

		log(driver + " findAllArticlesInSubCategory (" + category.get("name") + "," + subCategory.get("name") + ") : size=" + articleElems.size() + " , " + time);

		//Collections.reverse(articles); // reverse order :)
		Collections.shuffle(articles); // random
		return articles;
	}

	private static void openCategory(WebDriver driver, Map<String, String> category) {
		long start = System.currentTimeMillis();
		
		String link = category.get("link");
		if (!link.startsWith("http")) {
			link = baseUrl + link;
		}
		driver.navigate().to(link);

		long time = System.currentTimeMillis() - start;
		log(driver + " openCategory (" + category.get("name") + ") : " + time);
	}

	@SuppressWarnings("all")
	public static List<Map<String, String>> findSubCategories(WebDriver driver, Map<String, String> category, Map<String, String> context) {
		long start = System.currentTimeMillis();

		openCategory(driver, category);

		List<WebElement> subCategoryElems = driver.findElements(By.xpath("//ul[@class=\"subMenuEV\"]/li/a"));
		if (subCategoryElems.isEmpty()) {
			LOG.debug("No sub categories found\n" + driver.getPageSource());
		}
		
		List<Map<String, String>> subCategories = new ArrayList<Map<String, String>>();
		for (WebElement elem : subCategoryElems) {
			if (isSelectedSubCateogory(elem, context)) { 
			  subCategories.add(
					  map(entry("name", elem.getText()),
					  entry("link", elem.getAttribute("href"))));
			}
		}

		ArrayList<Map<String, String>> manSubCats = new ArrayList<Map<String, String>>();
		ArrayList<Map<String, String>> womanSubCats = new ArrayList<Map<String, String>>();
		ArrayList<Map<String, String>> otherSubCats = new ArrayList<Map<String, String>>();
		for (Map<String, String> subCat : subCategories) {
			String name = subCat.get("name").toLowerCase();
			if(name.contains("homme")) {
				manSubCats.add(subCat);
			} else if (name.contains("femme")) {
				womanSubCats.add(subCat);
			} else {
				otherSubCats.add(subCat);
			}
		}
		
		// priority for man :)
		manSubCats.addAll(womanSubCats);
		manSubCats.addAll(otherSubCats);
		
		long time = System.currentTimeMillis() - start;
		log(driver + " findSubCategories (" + category.get("name") + ") : size=" + subCategories.size()+ " , " + time);


		return manSubCats;
	}
	
	private static boolean isSelectedSubCateogory(WebElement subCatElem, Map<String, String> context) {
		String text = subCatElem.getText().toLowerCase();
		List<String> ignoreSubCats = getIgnoreSubCategories(context);
		return !text.contains("produits disponibles") && // ignore page which contains all products
			(ignoreSubCats.isEmpty() || !listContains(ignoreSubCats, text));
	}
}
