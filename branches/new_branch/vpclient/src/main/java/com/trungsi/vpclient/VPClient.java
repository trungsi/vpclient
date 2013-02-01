package com.trungsi.vpclient;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.htmlunit.HtmlUnitWebElement;
import org.openqa.selenium.support.ui.Select;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.trungsi.vpclient.utils.DateRange;

import java.lang.reflect.Method;
import java.util.*;

import static com.trungsi.vpclient.utils.CollectionUtils.*;

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
	public static final String WOMAN_SHIRT_SIZES = "womanShirtSizes";
	
	public static final String GIRL_SHOES_SIZES = "girlShoesSizes";
	public static final String GIRL_CLOTHING_SIZES = "girlClothingSizes";
	public static final String MAN_JEAN_SIZES = "manJeanSizes";
	public static final String MAN_SHOES_SIZES = "manShoesSizes";
	public static final String MAN_COSTUME_SIZES = "manCostumeSizes";
	public static final String MAN_CLOTHING_SIZES = "manClothingSizes";
	public static final String MAN_SHIRT_SIZES = "manShirtSizes";

	public static final String SELECTED_SALE_DATE = "selectedSaleDate";
	public static final String SELECTED_SALE_LINK = "selectedSaleLink";
	

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
		String driverName = getDefault(DRIVER_NAME, context, HTML_UNIT);

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
	static String homePage = "/vp4/Home/fr/Default.aspx";
	static String vpLoungeHomePage = "/vp4/Home/VpLoungeHome.aspx";
	
	private static boolean login(WebDriver driver, Map<String, String> context) {
		driver.get(baseUrl + "/vp4/Login/Portal.ashx");
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
		
		long start = System.currentTimeMillis();
		
		try {
			
			DateRange openDate = DateRange.parse(context.get(SELECTED_SALE_DATE));
            log(openDate.from +  " " + openDate.to);
			Date currentDate = currentDate();
			if (!openDate.containsDate(currentDate)) {
				long sleep = openDate.from.getTime() - currentDate.getTime();
				log("Sale not yet opened, sleep " + sleep);
				Thread.sleep(sleep);
			}
			
			/*List<WebElement> currentSalesElem = findCurrentSaleList(driver);
			WebElement selectedElem = getSelectedSaleFromList(currentSalesElem, context);
	
			selectedElem.click();*/
			goToLink(driver, context.get(SELECTED_SALE_LINK));
	
			
			waitForElementReady("//div[@class=\"obj_menuEV\"]", 5000L, driver);
			
		} catch (Error e) {
			if (driver.getPageSource().contains(context.get(SELECTED_SALE))) {
				log(driver + " cannot open selectedSale, try to refresh");
				driver.navigate().refresh();
				openSelectedSale(driver, context);
			} else {
				throw e;
			}
		} catch (ElementNotReadyException e) {
			// check adult age
			List<WebElement> accessLinks = driver.findElements(By.xpath("//div[@class=\"accessLink\"]/a"));
			if (!accessLinks.isEmpty()) {
				accessLinks.get(0).click();
				sleep(100);
			} else {
				throw e;
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			long time = System.currentTimeMillis() - start;
			println(driver + " goToSelectedSale : " + time);
		}
	}

	private static Date currentDate() {
		return new Date();
	}

	private static List<WebElement> findCurrentSaleList(WebDriver driver) {
		long start = System.currentTimeMillis();
		List<WebElement> currentSalesElem = driver.findElements(By.xpath("//ul[@class=\"currentlySales\"]//a[@id=\"linkSale\"]"));
		if (currentSalesElem.isEmpty()) {
			log("No item in sale\n" + driver.getPageSource());
			//throw new Error("No item in sale\n" + driver.getPageSource());
		}

		long time = System.currentTimeMillis() - start;
		println(driver + " findCurrentSaleList : " + time);

		return currentSalesElem;
	}

	/*private static WebElement getSelectedSaleFromList(List<WebElement> currentSaleList,Map<String, String> context) {
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
		println("getSelectedSaleFromList : " + time);

		if (selectedElem == null) {
			throw new Error("No sale " + context.get(SELECTED_SALE) + " found\n" + currentSaleList);
		}

		return selectedElem;
	}*/

	public static List<Map<String, String>> getSalesList(WebDriver driver) {
		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		
		addSalesList(driver, baseUrl + homePage, list);
		addSalesList(driver, baseUrl + vpLoungeHomePage, list);
		
		return list;
	}

	private static void addSalesList(WebDriver driver, String link,
			List<Map<String, String>> list) {
		goToLink(driver, link);
		
		addSalesToList(list, findCurrentSaleList(driver));
		addSalesToList(list, findSoonSaleList(driver));
	}

	public static void addSalesToList(List<Map<String, String>> list,
			List<WebElement> elems) {
		for (WebElement elem : elems) {
			Map<String, String> saleInfos = getSaleInfos(elem);
			
			list.add(saleInfos);
		}
	}

	public static Map<String, String> getSaleInfos(WebElement elem) {
        WebElement allElem = elem.findElement(By.xpath("h4"));
        //System.out.println(allElem.getText());

        String name = getTextOfH4(allElem);
        String link = elem.getAttribute("href");
        String dateSales = elem.findElement(By.xpath("./p[@class=\"dateSales\"]")).getText();
        if (dateSales.isEmpty()) {
            dateSales = new Date().toString();
        }

        Map<String, String> saleInfos = map(entry("name", name),
                entry("link", link),
                entry("dateSales", dateSales));
        return saleInfos;
	}
	
	private static List<WebElement> findSoonSaleList(WebDriver driver) {
		long start = System.currentTimeMillis();
		List<WebElement> currentSalesElem = driver.findElements(By.xpath("//ul[@class=\"soonSales\"]/li/div"));
		/*if (currentSalesElem.isEmpty()) {
			throw new Error("No item in sale\n" + driver.getPageSource());
		}*/
        // additional processing for "les 3 jours rose et sucrée" : 3 days after christmas
        // like summer camp
        // have hidden div containing mark list
        // so they are to be removed
        for (Iterator<WebElement> iter = currentSalesElem.iterator();iter.hasNext();) {
            WebElement elem = iter.next();
            System.out.println(elem + " " + elem.isDisplayed());
            //if (!elem.isDisplayed()) { always return true when javascript is disabled
            //if ("none".equals(elem.getCssValue("display"))) { does not work when javascript is disabled
            if (elem.getAttribute("style").contains("display: none")) {
                iter.remove();
            }
        }

		long time = System.currentTimeMillis() - start;
		println(driver + " findSoonSaleList : " + time);

		return currentSalesElem;
	}

	/**
	 * Bug in HtmlUnit ??? <br/>
	 * h4 element is seen as hidden, so getText() returns nothing.<br/>
	 * Workaround : call HtmlElement.getTextContent() directly
	 * 
	 * @param allElem
	 * @return
	 */
	private static String getTextOfH4(WebElement allElem) {
		try {
			return getElement(allElem).getTextContent();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private static HtmlElement getElement(WebElement allElem) throws Exception {
		HtmlUnitWebElement webElem = (HtmlUnitWebElement) allElem;
		Method[] methods = HtmlUnitWebElement.class.getDeclaredMethods();
		for (Method m : methods) {
			if (m.getName().equals("getElement") && m.getReturnType().equals(HtmlElement.class)) {
				m.setAccessible(true);
				return (HtmlElement) m.invoke(webElem, new Object[0]);
			}
		}
		
		throw new NoSuchMethodException("Cannot find method getElement in class " + HtmlUnitWebElement.class);
	}

	private static void waitForElementReady(Object xpathOrClosure, long timeout, WebDriver driver) {
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
	
	private static boolean evaluateXpathOrClosure (Object xpathOrClosure,WebDriver driver) {
		//println("xpathOrClosure " + xpathOrClosure.getClass())
		//if (xpathOrClosure instanceof String) {
			return !driver.findElements(By.xpath(xpathOrClosure.toString())).isEmpty();
		//} else {
			// Function[?, ?] ???
			//return ((Function)xpathOrClosure).apply();
		//}

	}

	/*public static interface Function {
		boolean apply();
	}*/

	public static void log(String msg) {
		LOG.info(msg);
	}

	public static void println(Object obj) {
		LOG.debug(obj);
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

		log(categories.size() + " categories found : \n" + categories);

		long time = System.currentTimeMillis() - start;
		println(driver + " findAllCategories : " + time);

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
		return !catElem.getText().contains("produits disponibles") && 
				(selectedCats.isEmpty() || (listContains(selectedCats, catElem.getText().toLowerCase())));
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
				log(" Cannot add article $articleName in category " + category.get("name") + ".\nCause :" + result.get("message"));
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
		println(driver + " addArticle : " + time);
		
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
					log(info + elems.get(0).getText());
				} else {
					log(info + "No addToCart button found, the article must be sold");
				}
				return false;
			}

			List<Map<String, String>> selectableSizes = null;
			List<WebElement> selectElems = driver.findElements(By.id("productId"));
			//sleep(500);
			if (selectElems.isEmpty() || !selectElems.get(0).getTagName().equals("select")) {
				log(info + " No model/size found. The article must not have this info");
				List<WebElement> productSize = driver.findElements(By.xpath("//p[@id='product_pUniqueModelRow']/span"));
				if (!productSize.isEmpty()) {
					String sizeText = productSize.get(0).getText();
					//println("sizeText=" + sizeText);
					List<String> preferedSize = getPreferedSize(driver, category, subCategory, article, context);
					boolean match = listContains(preferedSize, sizeText);
					log("sizeText=" + sizeText + " match (" + match + ") in " + preferedSize);
					if (sizeText.contains("T.") 
							&& !match) {
						log(info + " size " + sizeText + " not in " + preferedSize);
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
					log(info + selectableSizes.get(selectableSizes.size()-1));
					//MyHtmlUnitDriver htmlUnitDriver = (MyHtmlUnitDriver) driver;
					//htmlUnitDriver.postRequest(
					//		baseUrl + "/vp4/Catalog/WebServices/Cart.asmx/AddProduct", 
					//		"{\"productId\" : \"" + selectableSizes.get(selectableSizes.size()-1).get("value") + "\", \"familyId\" : \"" + driver.findElement(By.id("familyId")).getValue() + "\", \"quantity\" : \"1\"}");
				} else {
					log(info + " has no size");
										
				}
				
				addToCartBt.get(0).click();
				
				sleep(100);

				List<WebElement> resultBlocs = driver.findElements(By.xpath("//p[@id=\"resultBloc\"]"));

				if (!resultBlocs.isEmpty() && resultBlocs.get(0).isDisplayed()) {
					log(resultBlocs.get(0).getText());
				}

				List<WebElement> validResultBlocs = driver.findElements(By.xpath("//p[@id=\"validResultBloc\"]"));
				if (validResultBlocs.isEmpty() || !validResultBlocs.get(0).isDisplayed()) {
					log(info + "No confirmation after add article to cart");
				} else {
					log(info +" ADDED");
				}

				return true;
				
			} else {
				log(info + " No appropriate size");
				return false;
			}

		} finally {
			long time = System.currentTimeMillis() - start;
			println(" addArticleToCart : " + info + time);
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
			} else if (isShirt(articleInfo)) { // chemise
				return getManShirtSizes(context);
			} else {
				return getManClothingClothingSizes(context);
			}
		} else if (isWomanArticle(articleInfo)) {
			if (isJean(articleInfo)) {
				return getWomanJeanSizes(context);
			} else if (isShoes(articleInfo)) {
				return getWomanShoesSizes(context) ;
			} else if (isSoutienGorge(articleInfo)) {
				return getWomanLingerieSizes(context);
			} else if (isShirt(articleInfo)) {
				return getWomanShirtSizes(context);
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
		} else { // by default, man too :)
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
	
	private static List<String> getWomanShirtSizes(Map<String, String> context) {
		return list(getDefault(WOMAN_SHIRT_SIZES, context, "T. 36|T.36| XS ").split("\\|"));
	}

	private static List<String> getManShirtSizes(Map<String, String> context) {
		return list(getDefault(MAN_SHIRT_SIZES, context, "T. 39|T.39| M ").split("\\|"));
	}

	private static boolean isShirt(String articleInfo) {
		return articleInfo.contains("chemis");
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
			
			// must activate javascript because will submit form using ajax
			HtmlUnitDriver htmlUnitDriver = (HtmlUnitDriver) driver;
			htmlUnitDriver.setJavascriptEnabled(true);
						
			String openNewWindow = articleElem.get("link");
			goToLink(htmlUnitDriver, openNewWindow);
			
			//sleep(500);
			return map(entry("ok", (Object) java.lang.Boolean.TRUE));
		} finally {
			long time = System.currentTimeMillis() - start;
			println(driver + " openExpressPurchaseWindow : " + time);
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

	private static void goToLink(WebDriver driver, String link) {
		if (!link.startsWith("http")) {
			link = baseUrl + link;
		}
		driver.navigate().to(link);
	}
	
	@SuppressWarnings("all")
	public static List<Map<String, String>> findAllArticlesInSubCategory(WebDriver driver, Map<String, String> category, Map<String, String> subCategory) {
		long start = System.currentTimeMillis();

		// go to subCategory page
		if (subCategory != null && !subCategory.isEmpty()) {
			String link = subCategory.get("link");
			goToLink(driver, link);
		} else {
			// reload category page, because driver is shared by many workers
			goToLink(driver, category.get("link"));
		}

		sleep(1000);

		List<Map<String, String>> articles = new ArrayList<Map<String, String>>();
		
		// normally, there is <ul class="artList">
		// but in Summer Camp, there is <ul class="artList viewAllProduct">
		List<WebElement> articleElems = driver.findElements(
				By.xpath("//ul[starts-with(@class,\"artList\")]/li"));
		if (articleElems.isEmpty()) {
			log(" Cannot find ul with class 'artList viewAllProduct'. Will parse Json to get article infos");
			
			String source = driver.getPageSource();

			int index = source.indexOf("JSon=");
			if (index == -1) {
				throw new RuntimeException("cannot find json article info in " + category.get("name") + "|" + subCategory.get("name") + "\n" + source);
			}
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
					JSONObject item = items.getJSONObject(i); 
					//System.out.println(item);
					articles.add(map(entry("name", item.getString("infoArtTitle")), 
							entry("link", currentUrl + "FEikId" + item.getString("artURL") + ".aspx")));
				}
			} catch (Exception e) {
				LOG.error(driver + " Error on parsing json " + source, e);
			}
		} else {
			for (WebElement articleElem : articleElems) {
				//List<WebElement> elems = articleElem.findElements(By.xpath(".//*"));
				//System.out.println(elems);
				String name = articleElem.findElement(By.xpath(".//div[@class=\"infoArtTitle\"]")).getText();
				// Achat Express link could not exist
				// have to treate this case by using Fiche Produit
				try {
					String link = articleElem.findElement(By.xpath(".//a[@class=\"btStoreXpress\"]")).getAttribute("onclick");
					int firstIndex = link.indexOf("'");
					int lastIndex = link.lastIndexOf("'");
					link = link.substring(firstIndex+1, lastIndex);
					articles.add(map(entry("name", name), entry("link", link)));
				} catch (NoSuchElementException e) {
					LOG.error("Cannot find 'Achat express' button for " + name + " in " + articleElem.findElements(By.xpath(".//a[@class=\"btStoreXpress\"]")) + articleElem.findElement(By.xpath(".//span[@class=\"artState\"]")).getText());
				}
			}
		}

		// detecte woman|man articles
		// do not work correctly when man,woman,kids are mixted
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

		println(driver + " findAllArticlesInSubCategory (" + category.get("name") + "," + subCategory.get("name") + ") : size=" + articleElems.size() + " , " + time);

		Collections.shuffle(articles); // random :)
		return articles;
	}

	private static void openCategory(WebDriver driver, Map<String, String> category) {
		long start = System.currentTimeMillis();
		
		String link = category.get("link");
		goToLink(driver, link);
		
		long time = System.currentTimeMillis() - start;
		println(driver + " openCategory (" + category.get("name") + ") : " + time);
	}

	@SuppressWarnings("all")
	public static List<Map<String, String>> findSubCategories(WebDriver driver, Map<String, String> category, Map<String, String> context) {
		long start = System.currentTimeMillis();

		openCategory(driver, category);

		List<WebElement> subCategoryElems = driver.findElements(By.xpath("//ul[@class=\"subMenuEV\"]/li/a"));
		if (subCategoryElems.isEmpty()) {
			LOG.debug("No sub categories found in " + category.get("name") + "\n" + driver.getPageSource());
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
		println(driver + " findSubCategories (" + category.get("name") + ") : size=" + subCategories.size()+ " , " + time);


		return manSubCats;
	}
	
	private static boolean isSelectedSubCateogory(WebElement subCatElem, Map<String, String> context) {
		String text = subCatElem.getText().toLowerCase();
		List<String> ignoreSubCats = getIgnoreSubCategories(context);
		return !text.contains("produits disponibles") && // ignore page which contains all products
			(ignoreSubCats.isEmpty() || !listContains(ignoreSubCats, text));
	}
}
