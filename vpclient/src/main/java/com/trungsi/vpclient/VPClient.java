package com.trungsi.vpclient;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.htmlunit.HtmlUnitWebElement;
import org.openqa.selenium.support.ui.Select;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.trungsi.vpclient.utils.DateRange;

import java.lang.reflect.Method;
import java.net.URLEncoder;
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
    public static final String NEW_INTERFACE = "NEW_INTERFACE";
    public static final String EXCLUSIVE_ARTICLES = "exclusiveArticles";


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
		// XXX : copy cookies seems not to work anymore
        // Vente-privee BUG ??? : when get baseUrl (http://fr.vente-privee.com) page,
        // user session if any will be expired and then user has to logon again
        // redirect to https://secure.fr.vente-privee.com/xxxxxx.
        // So the trick here is to get url ${baseUrl}/vp4/_sales/ which as of now, will not be redirected
        // Then the cookies from domain fr.vente-privee.com will be copied correctly
        //newDriver.get(baseUrl);
        newDriver.get(baseUrl +"/vp4/_sales/");

        for (Cookie cookie : driver.manage().getCookies()) {
			newDriver.manage().addCookie(cookie);
            //System.out.println("new cookie added " + cookie);
        }

        return newDriver;

        //return loadDriver(context);
	}
	private static WebDriver newDriver(Map<String, String> context) {
		WebDriver driver = null;
		String driverName = getDefault(DRIVER_NAME, context, HTML_UNIT);

		//println(driverName);
		
		if (driverName.equals(HTML_UNIT)) {
			driver = new MyHtmlUnitDriver();
		}
		return driver;
	}

	private static void sleep (long timeout) {
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private final static String baseUrl = "http://fr.vente-privee.com";
	private final static String homePage = "/vp4/Home/fr/Default.aspx";
	private final static String vpLoungeHomePage = "/vp4/Home/VpLoungeHome.aspx";
	
	private static boolean login(WebDriver driver, Map<String, String> context) {
		driver.get(baseUrl + "/vp4/Login/Portal.ashx");
		sleep (20);

		WebElement emailElem = driver.findElement(By.id("txtEmail"));
		emailElem.sendKeys(context.get(USER));
		sleep(20);

		WebElement pwdElem = driver.findElement(By.id("txtPassword"));
		pwdElem.sendKeys(context.get(PWD));
		sleep(20);

		WebElement submitElem = driver.findElement(By.id("btSubmit"));

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
            log(openDate.toString());
			Date currentDate = currentDate();
			if (!openDate.containsDate(currentDate)) {
				long sleep = openDate.getNextTo(currentDate).getTimestamp();
				log("Sale not yet opened, sleep " + sleep);
				sleep(sleep);
			}
			
			/*List<WebElement> currentSalesElem = findCurrentSaleList(driver);
			WebElement selectedElem = getSelectedSaleFromList(currentSalesElem, context);
	
			selectedElem.click();*/
			goToLink(driver, context.get(SELECTED_SALE_LINK));
	

            try {
			    waitForElementReady("//div[@class=\"obj_menuEV\"]", 5000L, driver);
            } catch (Exception e) {
                // sometimes, it's not a div but a nav, HTML5 ???
                waitForElementReady("//nav[@class=\"obj_menuEV\"]", 5000L, driver);
                // so new interface
                context.put(NEW_INTERFACE, "true");
            }
			
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
		//List<WebElement> currentSalesElem = driver.findElements(By.xpath("//ul[@class=\"currentlySales\"]//a[@id=\"linkSale\"]"));
        List<WebElement> currentSalesElem = driver.findElements(By.xpath("//ul[@class=\"currentlySales\"]//a[@class=\"linkAccess\"]"));
		if (currentSalesElem.isEmpty() && !driver.getCurrentUrl().contains(vpLoungeHomePage)) {
			log("No item in sale\n" + driver.getPageSource());
			//throw new Error("No item in sale\n" + driver.getPageSource());
		}

		long time = System.currentTimeMillis() - start;
		log(driver + " findCurrentSaleList : " + time);

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

	private static void addSalesToList(List<Map<String, String>> list,
			List<WebElement> elems) {
		for (WebElement elem : elems) {
			Map<String, String> saleInfos = getSaleInfos(elem);
			
			list.add(saleInfos);
		}
	}

	private static Map<String, String> getSaleInfos(WebElement elem) {
        //System.out.println(elem.getTagName() + "aaa");

        WebElement allElem = elem.findElement(By.xpath("h4"));
        String name = getTextOfHiddenElement(allElem);
        //System.out.println(name + "aaa");

        String link = elem.getAttribute("href");


        String dateSales = null;
        if (name.startsWith("One Day")) {
            dateSales = new Date().toString();
        } else {
            try {
                dateSales = elem.findElement(By.xpath("./..//p[@class=\"dateSales\"]")).getText();
            } catch (NoSuchElementException e) {
                // some wine sale operations don't have sale date ???
                System.out.println("No date sale found for " + name + " : " + elem.findElement(By.xpath("..")).getAttribute("data-opcategory"));
                dateSales = "";
            }
            if (dateSales.isEmpty()) {
                dateSales = new Date().toString();
            }
        }

        return map(entry("name", name),
                entry("link", link),
                entry("dateSales", dateSales));

	}
	
	private static List<WebElement> findSoonSaleList(WebDriver driver) {
		long start = System.currentTimeMillis();
		List<WebElement> currentSalesElem = driver.findElements(By.xpath("//ul[@class=\"soonSales\"]/li/div/a[@class='linkAccess']"));
		/*if (currentSalesElem.isEmpty()) {
			throw new Error("No item in sale\n" + driver.getPageSource());
		}*/
        // additional processing for "les 3 jours rose et sucrée" : 3 days after christmas
        // like summer camp
        // have hidden div containing mark list
        // so they are to be removed
        /*for (Iterator<WebElement> iter = currentSalesElem.iterator();iter.hasNext();) {
            WebElement elem = iter.next();
            System.out.println(elem + " " + elem.isDisplayed());
            //if (!elem.isDisplayed()) { always return true when javascript is disabled
            //if ("none".equals(elem.getCssValue("display"))) { does not work when javascript is disabled
            if (elem.getAttribute("style").contains("display: none")) {
                iter.remove();
            }
        }*/

		long time = System.currentTimeMillis() - start;
		log(driver + " findSoonSaleList : " + time);

		return currentSalesElem;
	}

	/**
	 * Bug in HtmlUnit ??? <br/>
	 * getText() of hidden element returns nothing.<br/>
	 * Workaround : call HtmlElement.getTextContent() directly
	 * 
	 * @param allElem
	 * @return
	 */
	private static String getTextOfHiddenElement(WebElement allElem) {
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

		throw new ElementNotReadyException("Cannot find element " + xpathOrClosure + " after " + timeout + " in \n " + driver.getCurrentUrl() + driver.getPageSource());
	}

    public static int getBasketSize(WebDriver driver) {
        goToLink(driver, "/cart/");
        String pageSource = driver.getPageSource();

        String startPattern = "require([\"Command/CartManager\"], function(manager){";

        int startPos = pageSource.indexOf(startPattern);
        if (startPos <= 0) {
            throw new RuntimeException("Cannot decode basket info in page source\n" + pageSource);
        }

        String substring = pageSource.substring(startPos+startPattern.length()).trim();
        if (!substring.startsWith("manager(")) {
            throw new RuntimeException("substring doesn't start with manager( : " + substring);
        }

        int nextPos = substring.indexOf("\n");
        substring = substring.substring(0, nextPos).trim();
        if (!substring.endsWith(",")) {
            throw new RuntimeException("substring doesn't end with , : " + substring);
        }

        substring = substring.substring(8, substring.length() - 1);

        try {
            JSONObject json = new JSONObject(substring);
            //System.out.println(json);
            int size = 0;
            JSONArray cartOperations = json.getJSONArray("CartOperations");
            for (int i = 0; i < cartOperations.length(); i++) {
                JSONObject cartOperation = cartOperations.getJSONObject(i);
                System.out.println(cartOperation);
                JSONArray cartDetails = cartOperation.getJSONArray("CartDetails");
                size += cartDetails.length();
            }
            return size;
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new RuntimeException("cannot parse json : " + substring);
        }
        //return driver.findElements(By.xpath("//table[@id='commandTable']//tr[contains(@class, 'cartdetail')]")).size();
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

	private static void log(String msg) {
		LOG.info(msg);
	}

	private static void println(Object obj) {
		LOG.debug(obj);
	}

	
	@SuppressWarnings("all")
	public static List<Category> findAllCategories(WebDriver driver, Map<String, String> context) {
		long start = System.currentTimeMillis();

		openSelectedSale(driver, context);

		List<WebElement> catElems = null;
        if (isNewInterface(context)) {
            catElems = driver.findElements(By.xpath("//ul[@class=\"menuEV_Container\"]/li/a/span/..")); // car premier <a> sans <span> n'est pas intÃ©ressant
        } else {
            catElems = driver.findElements(By.xpath("//ul[@class=\"menuEV\"]/li/a/span/..")); // car premier <a> sans <span> n'est pas intÃ©ressant
        }

		if (catElems.isEmpty()) {
			throw new Error("No category found for marque " + context.get(SELECTED_SALE) + "\n" + driver.getPageSource());
		}

		List<Category> categories = filterCategories(catElems, context);

		log(categories.size() + " categories found : \n" + categories);

		long time = System.currentTimeMillis() - start;
		log(driver + " findAllCategories : " + time);

		return categories;
	}

    private static boolean isNewInterface(Map<String, String> context) {
        return context.get(NEW_INTERFACE) != null;
    }

    @SuppressWarnings("all")
	private static List<Category> filterCategories(
			List<WebElement> catElems, Map<String, String> context) {
		
		List<Category> categories = new ArrayList<>();
		List<String> selectedCats = getSelectedCategories(context);
		
		for (WebElement catElem : catElems) {
			if (isSelectedCategory(selectedCats, catElem)) {
				categories.add(
						new Category(catElem.getText(), catElem.getAttribute("href")));
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

	public static boolean addArticle(WebDriver driver, Article article, Map<String, String> context) {
		long start = System.currentTimeMillis();

		boolean added = false;
		//String mainWindowHandle = driver.getWindowHandle();
		try {
			Map<String, Object> result = openExpressPurchaseWindow(driver, article);
			if ((Boolean)result.get("ok")) {
				added = addArticleToCart(driver, article, context);
			} else {
				log(" Cannot add article $articleName.\nCause :" + result.get("message"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			//println(driver.getPageSource)
		} finally {
			// en cas de HtmlUnit, il faut re-dÃ©sactiver javascript et ne pas fermer la fenÃªtre
			if (driver instanceof HtmlUnitDriver) {
                // no need anymore because javascript is no longer required to add article to cart
                // AddToCart service url is directly called now
				//((HtmlUnitDriver)driver).setJavascriptEnabled(false);
			} else {
				driver.close();
				println ("window close");
				sleep (50);
			}

			//switchToWindow(driver, mainWindowHandle);
		}

		long time = System.currentTimeMillis() - start;
		log(driver + " addArticle " + (added ? "successful" : "failed") + ": " + time);
		
		return added;
	}

	private static boolean addArticleToCart (WebDriver driver, Article article, Map<String, String> context) {
		long start = System.currentTimeMillis();
		String info = getArticleInfo(driver, article);
		try {
			/*List<WebElement> addToCartBt = driver.findElements(By.id("addToCart"));
			if (addToCartBt.isEmpty() || !addToCartBt.get(0).isDisplayed()) {
				//println(driver.getPageSource());
				List<WebElement> elems = driver.findElements(By.id("product_pUnavailable"));
				if (!elems.isEmpty()) {
					log(info + elems.get(0).getText());

                    // id=ctl00_altColumnContent_product_pnlPrice, class=detailBlocPrice
                    // id=ctl00_altColumnContent_product_pnlInfo, class=detailBlocChoice
                    JavascriptExecutor executor = (JavascriptExecutor) driver;
                    executor.executeScript("document.getElementById('product_pnlInfo').style.display='block';");
                    addToCartBt = driver.findElements(By.id("addToCart"));
                    //log("addToCart button is displayed : " + addToCartBt.get(0).isDisplayed());
                    log("try to use hidden form to add article " + info);
                    //log(driver.getPageSource());

				} else {
					log(info + "No addToCart button found, the article must be sold\n" + driver.getPageSource());
                    return false;
				}

			}*/


            List<Map<String, String>> selectableSizes = selectSize(driver, article, context, info);

			if (selectableSizes == null || selectableSizes.size() >= 1) {
				if (selectableSizes != null) {
					log(info + selectableSizes.get(selectableSizes.size()-1));
				} else {
					log(info + " has no size");
										
				}

                String pageSource = driver.getPageSource();
                //log(pageSource);

                String familyId = getFamilyId(pageSource);
                String productId = getSelectedProductId(pageSource);
                log("familyId:" + familyId +", productId:" + productId);


                String result = ((MyHtmlUnitDriver) driver).postRequest(baseUrl + "/cart/CartServices/AddToCartOrCanBeReopened", "productFamilyId=" + URLEncoder.encode(familyId) + "&productId=" + URLEncoder.encode(productId) + "&quantity=1");
                log(result);
                return result.contains("\"ReturnCode\":0");

                /*JavascriptExecutor executor = (JavascriptExecutor) driver;
                Map addToCartResult = (Map) executor.executeAsyncScript(
                        "var callback = arguments[arguments.length-1];" +
                                "var productId = '" + productId + "';\n" +
                                "    var quantity = 1;\n" +
                                "    var familyId = '" + familyId + "';\n" +
                                "    var params = { productId: productId, productFamilyId: familyId, quantity: quantity };\n" +
                                "\n" +
                                "    if (!params.quantity) {\n" +
                                "        params.quantity = 1;\n" +
                                "    }\n" +
                                "    \n" +
                                "    $.ajax({\n" +
                                "            //TODO GET URI \n" +
                                "            url: '/cart/CartServices/AddToCartOrCanBeReopened',\n" +
                                "            type: 'POST',\n" +
                                "            data: params\n" +
                                "\n" +
                                "        }).done(function(result) {callback(result);});");
                log(info + " addToCart result : \n" + addToCartResult);

                return "0".equals(addToCartResult.get("ReturnCode"));*/



                //System.out.println("add to cart button is displayed : " + addToCartBt.get(0).isDisplayed());
				/*addToCartBt.get(0).click();
				
				sleep(100);

				List<WebElement> resultBlocs = driver.findElements(By.xpath("//p[@id=\"resultBloc\"]"));

				if (!resultBlocs.isEmpty() && resultBlocs.get(0).isDisplayed()) {
					log(resultBlocs.get(0).getText());
				}

				List<WebElement> validResultBlocs = driver.findElements(By.xpath("//p[@id=\"validResultBloc\"]"));
				if (validResultBlocs.isEmpty() || !validResultBlocs.get(0).isDisplayed()) {
					log(info + "No confirmation after add article to cart");
                    return false;
				} else {
					log(info +" ADDED----------------------------------------------------------------------------");
                    return true;
				}*/


				
			} else {
				log(info + " No appropriate size");
				return false;
			}

		} finally {
			long time = System.currentTimeMillis() - start;
			println(" addArticleToCart : " + info + time);
		}
	}

    public static List<Map<String, String>> selectSize(WebDriver driver, Article article, Map<String, String> context, String info) {
        List<Map<String, String>> selectableSizes = null;
        List<WebElement> selectElems = driver.findElements(By.id("productId"));
        //sleep(500);
        if (selectElems.isEmpty() || !selectElems.get(0).getTagName().equals("select")) {
            log(info + " No model/size found. The article must not have this info");
            List<WebElement> productSize = driver.findElements(By.xpath("//p[@id='product_pUniqueModelRow']/span"));
            if (!productSize.isEmpty()) {
                String sizeText = productSize.get(0).getText();
                //println("sizeText=" + sizeText);
                List<String> preferedSize = getPreferedSize(driver, article, context, null);
                boolean match = listContains(preferedSize, sizeText);
                log("sizeText=" + sizeText + " match (" + match + ") in " + preferedSize);
                if (sizeText.contains("T.")
                        && !match) {
                    log(info + " size " + sizeText + " not in " + preferedSize);
                    //return false;
                }
            }
            LOG.debug("Unique size model ??? \n" + driver.getPageSource());
        } else {
            selectableSizes = getMostAppropriateSizes(driver, article, context, selectElems.get(0));
        }
        return selectableSizes;
    }

    public static String getSelectedProductId(String pageSource) {
        String prefix = "<input type=\"hidden\" id=\"productId\" name=\"productId\" value=\"";
        String subfix = "\"/>";

        try {
            return substring(pageSource, prefix, subfix);  //To change body of created methods use File | Settings | File Templates.
        } catch (RuntimeException e) { // no hidden input for productId, must have a select
            String optionsText = substring(pageSource, "<select name=\"productId\" id=\"productId\">", "</select>");
            String[] optionsTextArray = optionsText.split("\\n");
            for (String text : optionsTextArray) {
                if (text.contains("selected=\"selected\"")) {
                    return substring(text, "value=\"", "\"");
                }
            }

            throw new RuntimeException("No selected option component found in " + optionsText);
        }

    }

    public static String getFamilyId(String pageSource) {
        String startPrefix = "<input type=\"hidden\" name=\"familyId\" id=\"familyId\" value=\"";
        String subfix = "\"/>";

        return substring(pageSource, startPrefix, subfix);
    }

    private static String substring(String source, String prefix, String subfix) {
        //String startPrefix = "<input type=\"hidden\" name=\"familyId\" id=\"familyId\" value=\"";
        int index = source.indexOf(prefix);
        if (index <= 0) {
            throw new RuntimeException("Prefix " + prefix + " not found in \n" + source);
        }

        int endIndex = source.indexOf(subfix, index + prefix.length());
        if (endIndex <= 0) {
            throw new RuntimeException("Subfix "+ subfix + " not found in \n" + source);
        }

        return source.substring(index+prefix.length(), endIndex);
    }

    private static List<Map<String, String>> getMostAppropriateSizes(WebDriver driver, Article article,
			Map<String, String> context, WebElement selectElem) {
        //log(driver.getPageSource());
		Select select = new Select(selectElem);
		
		return selectMostAppropriateSizes(driver, article, context, select);
		
		//return selected;

	}

	/*private static void switchToWindow(WebDriver driver, String mainWindowHandle) {
		println ("switch to " + mainWindowHandle);
		driver.switchTo().window(mainWindowHandle);
	}*/

	private static List<Map<String, String>> selectMostAppropriateSizes (WebDriver driver,
			Article article, Map<String, String> context, Select select) {
		//log("toto");
        List<String> preferedSize = getPreferedSize(driver, article, context, select);
		if (preferedSize.isEmpty()) {
            log("No prefered size found for " + getArticleInfo(driver, article));
			return new ArrayList<Map<String, String>>();
		} else {
            //log(driver.getPageSource());
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
	
	private static List<String> getPreferedSize(WebDriver driver, Article article, Map<String, String> context, Select select) {
		String articleInfo = getArticleInfo(driver, article);

        if (isWomanArticle(articleInfo)) {
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
		} else if (isManArticle(articleInfo)) {
            if (isJean(articleInfo)) {
                if (articleInfo.contains("edwin")) {
                    return list("29|30 (US)|W30|T.30 (US)|40 (FR)|T.40|T. 40| 30".split("\\|"));
                }
                return getManJeanSizes(context);
            } else if (isShoes(articleInfo)) {
                return getManShoesSizes(context);
            } else if (isCostume(articleInfo)) {
                if (articleInfo.contains("windsor")) { // special size for Windsor
                    return list("T. 46 (FR)|Veste T. 46|T. 46|T. 46".split("\\|"));
                }

                if (!(articleInfo.contains("slim") || articleInfo.contains("cintré") || articleInfo.contains("ajusté"))) { // T. 46 for coupe droite
                    return list("Veste T. 46|Pantalon T. 38|T. 46|T. 38".split("\\|"));
                }

                return getManCostumeSizes(context);
            } else if (isShirt(articleInfo)) { // chemise
                return getManShirtSizes(context);
            } else if (isPants(articleInfo)) {
                if (articleInfo.contains("hackett")) {
                    return list("T. 32 (UK)|T. 31|31");
                }
                return getManPantsSize(context);
            } else {
                List<String> sizes = getManClothingClothingSizes(context);
                if(articleInfo.contains("hackett")) {
                    List<String> newSizes = list(" S ", "T. S");
                    newSizes.addAll(sizes);
                    return newSizes;
                }
                return sizes;
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

            if (containsManSize(select)) {
                if (isJean(articleInfo)) {
                    return getManJeanSizes(context);
                } else if (isShoes(articleInfo)) {
                    return getManShoesSizes(context);
                } else if (isCostume(articleInfo)) {
                    if (articleInfo.contains("windsor")) { // special size for Windsor
                        return list("T. 46 (FR)|Veste T. 46|T. 46|T. 46".split("\\|"));
                    }

                    if (!(articleInfo.contains("slim") || articleInfo.contains("cintré") || articleInfo.contains("ajusté"))) { // T. 46 for coupe droite
                        return list("Veste T. 46|Pantalon T. 38|T. 46|T. 38".split("\\|"));
                    }

                    // T. 48 for slim or cintrée
                    return getManCostumeSizes(context);
                } else {
                    return getManClothingClothingSizes(context);
                }
            } else if (/*
                for jupe, robe,... it's not sure if it's about woman or girl.
                So have to check further
                 */
                    (listContains(list("jupe", "robe",
                            "body", "bodies",
                            "collant", "legging"), articleInfo)) ||
                            containsWomanSize(select)) {
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
            } else {
                LOG.error("Cannot determine article type from " + articleInfo);

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
	}

    private static List<String> getManPantsSize(Map<String, String> context) {
        return list("T. 40 (FR)|T. 30 (US)|T. 40|T. 30".split("\\|"));
    }

    private static boolean isPants(String articleInfo) {
        return articleInfo.contains("pantalon");
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
        return listContains(list("calçon", "boxer", "costume"), articleInfo) ||
                (articleInfo.contains("homme") && !articleInfo.contains("femme"));
	}

    private static boolean containsManSize(Select select) {
        List<String> manSizes = list("T. 42", "T. 44", "T. 46", "T. 48", "T. 50", "T. 34 (US)");
        return containsSize(select, manSizes);
    }

    private static boolean containsSize(Select select, List<String> sizes) {
        if (select != null) {
            for (WebElement option : select.getOptions()) {
                if (listContains(sizes, getOptionText(option)))  {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isSoutienGorge(String articleInfo) {
		return articleInfo.contains("soutien");
	}

	private static boolean isCostume(String articleInfo) {
		return listContains(list("costume", "veste"), articleInfo);
	}

	private static String getArticleInfo(WebDriver driver, Article article) {
        SubCategory subCategory = article.getSubCategory();
        Category category = subCategory.getCategory();

		return (category.getName() + "|" + subCategory.getName()
				/*+ (subCategory.getAttribute("femme") != null ? "femme" :
					subCategory.getAttribute("homme" != null ? "homme" : ""))*/
				+ "|" + article.getName() /*+ "|" + getArticleDetail(driver)*/).toLowerCase();
	}

	private static boolean isWomanArticle(String articleInfo) {
		return isSaleForWomanOnly() || articleInfo.contains("farrutx") ||
                listContains(list(
                                    "soutien", "lingerie",
                                    "chemisier", "ballerine", "robe", "jupe", "escarpin",
                                    "compens", "sandale", "talon", "cuissard", "culotte", "top", "pantacourt"), articleInfo) ||
                (articleInfo.contains("femme") && !articleInfo.contains("homme"));
	}

    private static boolean containsWomanSize(Select select) {
        List<String> womanSizes = list("T. 25 (US)", "T. 26 (US)", "T. 27 (US)", "T. 34 (FR)", "T. 32/34", "T. 34/36");
        return containsSize(select, womanSizes);
    }

    private static boolean isKidArticle(String articleInfo, Select select) {
        return articleInfo.contains("enfant") || articleInfo.contains("kid") ||
                /*
                if size contains 'ans', it's sure for kids
                 */
                containsSize(select, list(" ans"));
    }

	private static boolean isGirlArticle(String articleInfo) {
		return articleInfo.contains("fille");
	}

	private static boolean isBoyArticle(String articleInfo) {
		return articleInfo.contains("garçon");
	}

	/*private static String getArticleDetail(WebDriver driver) {
		String source = driver.getPageSource();
		int index = source.indexOf("xtpage = \"") + 10;
		if (index >= 10) {
			source = source.substring(index);
			int endIndex = source.indexOf("\"");
			return source.substring(0, endIndex);
		} else {
			return "";
		}
	}*/

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

			String optionText = getOptionText(option);

			log("optionText = " + optionText + " in " + selectSizeList);

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

    private static String getOptionText(WebElement option) {
        // option may be hidden here
        return getTextOfHiddenElement(option);
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
	public static Map<String, Object> openExpressPurchaseWindow(final WebDriver driver, Article articleElem) {
		long start = System.currentTimeMillis();
		try {
			
			// must activate javascript because will submit form using ajax
			HtmlUnitDriver htmlUnitDriver = (HtmlUnitDriver) driver;
			//htmlUnitDriver.setJavascriptEnabled(true);
						
			String openNewWindow = articleElem.getLink();
            int time = 0;

			do {
                if(time > 0) {
                    sleep(50);
                    log("retry to open purchase windows " + (time+1) + " times");
                }
                goToLink(driver, openNewWindow);

            } while (!driver.getCurrentUrl().contains(openNewWindow) && ++time <= 10);

            log("openExpressPurchaseWindow:" + articleElem + " -> " + driver.getCurrentUrl());

			//sleep(500);
            if (driver.getCurrentUrl().contains(openNewWindow))
			    return map(entry("ok", (Object) Boolean.TRUE));
            else
                return map(entry("ok", (Object) Boolean.FALSE));
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

	static void goToLink(WebDriver driver, String link) {
		if (!link.startsWith("http")) {
			link = baseUrl + link;
		}
		driver.navigate().to(link);
        //assert driver.getCurrentUrl().equals(link) : driver.getCurrentUrl() + " <> " + link;
	}
	
	@SuppressWarnings("all")
	public static List<Article> findAllArticlesInSubCategory(WebDriver driver, SubCategory subCategory, Map<String, String> context) {
		long start = System.currentTimeMillis();

		// go to subCategory page
		/*if (subCategory != null && !subCategory.isEmpty()) {
			String link = subCategory.getLink();
            log("goto subcategory " + subCategory);
			goToLink(driver, link);
		} else {
			// reload category page, because driver is shared by many workers
            log("goto category " + category);
			goToLink(driver, category.getLink());
		}*/
        goToLink(driver, subCategory.getLink());

		sleep(500);

        //log(driver.getPageSource());
		List<Article> articles = new ArrayList<Article>();
		
		// normally, there is <ul class="artList">
		// but in Summer Camp, there is <ul class="artList viewAllProduct">
		List<WebElement> articleElems = driver.findElements(
				By.xpath("//ul[starts-with(@class,\"artList\")]/li"));
		if (articleElems.isEmpty()) {
			log(" Cannot find ul with class 'artList viewAllProduct'. Will parse Json to get article infos");
			articles.addAll(findAllArticlesByParsingJson(driver, subCategory));


		} else {
			for (WebElement articleElem : articleElems) {
				//List<WebElement> elems = articleElem.findElements(By.xpath(".//*"));
				//System.out.println(elems);
                //log("articleElem: " + articleElem.getText() + category + subCategory);
				String name = articleElem.findElement(By.xpath(".//div[@class=\"infoArtTitle\"]")).getText();
				// Achat Express link could not exist
				// have to treate this case by using Fiche Produit
				try {
					String link = null;
                    if (isNewInterface(context)) {
                        link = articleElem.findElement(By.xpath(".//a[@class=\"infoExpressBt\"]")).getAttribute("href");
                    } else {
                        link = articleElem.findElement(By.xpath(".//a[@class=\"btStoreXpress\"]")).getAttribute("onclick");
                        int firstIndex = link.indexOf("'");
                        int lastIndex = link.lastIndexOf("'");
                        link = link.substring(firstIndex+1, lastIndex);
                    }

					articles.add(new Article(name, link, subCategory));
				} catch (NoSuchElementException e) {
					LOG.error("Cannot find 'Achat express' button for " + name + " in " + articleElem.findElements(By.xpath(".//a[@class=\"btStoreXpress\"]")) + articleElem.findElements(By.xpath(".//span[@class=\"artState\"]")));
				}
			}
		}

		// detecte woman|man articles
		// do not work correctly when man,woman,kids are mixted
		/*String catSubCat = subCategory.getInfo().toLowerCase();
		if (!catSubCat.contains("femme") && !catSubCat.contains("homme")) {
			String source = driver.getPageSource();
			if (source.contains("Tailles femme")) {
				subCategory.addAttribute("femme", "true");
			} else if (source.contains("Tailles homme")) {
				subCategory.addAttribute("homme", "true");
			}
		}*/
		
		long time = System.currentTimeMillis() - start;

		log(driver + " findAllArticlesInSubCategory " + subCategory.getInfo() + " : size=" + articleElems.size() + " , " + time);

		Collections.shuffle(articles); // random :)
		return articles;
	}

    private static List<Article> findAllArticlesByParsingJson(WebDriver driver, SubCategory subCategory) {
        ArrayList<Article> articles = new ArrayList<Article>();
        long start = System.currentTimeMillis();

        String source = driver.getPageSource();

        int index = source.indexOf("JSon=");
        if (index == -1) {
            throw new RuntimeException("cannot find json article info in " + subCategory.getInfo() + "\n" + source);
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
                articles.add(new Article(item.getString("infoArtTitle"),
                        currentUrl + "FEikId" + item.getString("artURL") + ".aspx", subCategory));
            }
        } catch (Exception e) {
            LOG.error(driver + " Error on parsing json " + source, e);
        } finally {
            log("parsing JSON found " + articles.size() + " articles : " + (System.currentTimeMillis() - start));
        }

        return articles;
    }

    private static void openCategory(WebDriver driver, Category category) {
		long start = System.currentTimeMillis();
		
		String link = category.getLink();
		goToLink(driver, link);
		
		long time = System.currentTimeMillis() - start;
		log(driver + " openCategory (" + category.getName() + ") : " + time);
	}

	@SuppressWarnings("all")
	public static List<SubCategory> findSubCategories(WebDriver driver, Category category, Map<String, String> context) {
		long start = System.currentTimeMillis();

		openCategory(driver, category);

		List<WebElement> subCategoryElems = driver.findElements(By.xpath("//ul[@class=\"subMenuEV\"]/li/a"));

        List<SubCategory> subCategories = new ArrayList<>();

        if (subCategoryElems.isEmpty()) {
			log("No sub categories found in " + category.getName());
            //log(driver.getPageSource());
            log("Create fake SubCategory");
            subCategories.add(SubCategory.fake(category));
		} else {
            log(subCategoryElems.size() + " sub categories found in " + category.getName());

            for (WebElement elem : subCategoryElems) {
                try {
                    if (isSelectedSubCateogory(elem, context)) {
                        try {
                            subCategories.add(
                                    new SubCategory(elem.getText(), elem.getAttribute("href"), category));
                        } catch (StaleElementReferenceException e) {
                            log("staleElementException:\n" + driver.getCurrentUrl() + driver.getPageSource());
                            throw new RuntimeException(e);
                        }
                    }
                } catch (StaleElementReferenceException e) {
                    log("staleElementException:\n" + driver.getCurrentUrl() + "\n" + driver.getPageSource());
                    throw e;
                }
            }

        }
		
        // XXX : only man sub categories ??? ;)
		ArrayList<SubCategory> manSubCats = new ArrayList<>();
		ArrayList<SubCategory> womanSubCats = new ArrayList<>();
		ArrayList<SubCategory> otherSubCats = new ArrayList<>();
		for (SubCategory subCat : subCategories) {
			String name = subCat.getName().toLowerCase();
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
		println(driver + " findSubCategories (" + category.getName() + ") : size=" + subCategories.size()+ " , " + time);


		return manSubCats;
	}
	
	private static boolean isSelectedSubCateogory(WebElement subCatElem, Map<String, String> context) {
		String text = subCatElem.getText().toLowerCase();
		List<String> ignoreSubCats = getIgnoreSubCategories(context);
		return !text.contains("produits disponibles") && // ignore page which contains all products
			(ignoreSubCats.isEmpty() || !listContains(ignoreSubCats, text));
	}

    public static void goToViewOrders(WebDriver driver) {
        goToLink(driver, "/vp4/MemberAccount/ViewOrders.aspx");
    }

    public static List<Map<String, String>> getOrdersInPage(WebDriver driver) {
        List<Map<String, String>> orders = new ArrayList<>();

        List<WebElement> orderLineElements = driver.findElements(By.xpath("//table[@id='commandChoiceTable']/tbody/tr"));
        for (WebElement orderLineElem : orderLineElements) {
            List<WebElement> orderTDElements = orderLineElem.findElements(By.xpath("td"));

            Map<String, String> order = new HashMap<>();
            order.put("name", orderTDElements.get(0).getText());
            order.put("id", orderTDElements.get(1).getText());
            order.put("date", orderTDElements.get(2).getText());
            order.put("amount", orderTDElements.get(3).getText());

            List<WebElement> orderLinkElements = orderTDElements.get(4).findElements(By.xpath("a"));
            order.put("orderDetailLink", orderLinkElements.get(0).getAttribute("href"));
            if (orderLinkElements.size() == 3) { // normal order
                order.put("followUpLink", orderLinkElements.get(1).getAttribute("href"));
                order.put("helpAndContactLink", orderLinkElements.get(2).getAttribute("href"));
            } else { // return order, no followUp link
                order.put("helpAndContactLink", orderLinkElements.get(1).getAttribute("href"));
            }
            orders.add(order);
        }
        return orders;
    }

    public static String getViewOrdersNextPageLink(WebDriver driver) {
        List<WebElement> nexButton = driver.findElements(By.xpath("//a[@class='accNavLink' and text()='Suivant']"));
        return nexButton.isEmpty() ? null : nexButton.get(0).getAttribute("href");
    }

    public static List<Map<String, String>> getAllOrders(WebDriver driver) {
        ArrayList<Map<String, String>> orders = new ArrayList<>();

        goToViewOrders(driver);

        String nextOrderLink = null;

        do {
            orders.addAll(getOrdersInPage(driver));
            nextOrderLink = getViewOrdersNextPageLink(driver);
            if (nextOrderLink != null) {
                goToLink(driver, nextOrderLink);
            }
        } while (nextOrderLink != null);

        return orders;
    }

    public static List<Map<String, String>> getOrderDetail(WebDriver driver, Map<String, String> order) {
        ArrayList<Map<String, String>> orderDetail = new ArrayList<>();

        goToLink(driver, order.get("orderDetailLink"));

        List<WebElement> elements = driver.findElements(By.xpath("//tr[contains(@id,'_mainColumnContent_repOrderDetails_')]"));
        //System.out.println(elements.size());

        for (WebElement  elem : elements) {
            List<WebElement> details = elem.findElements(By.tagName("td"));
            HashMap<String, String> detailMap = new HashMap<>();
            detailMap.put("articleName", details.get(0).getText());
            detailMap.put("quantity", details.get(1).getText());
            detailMap.put("price", details.get(3).getText());

            orderDetail.add(detailMap);
        }
        return orderDetail;
    }

    public static void filterExclusiveArticles(WebDriver webDriver, List<Article> articleElems, Map<String,String> context) {
        String exclusiveArticles = context.get(EXCLUSIVE_ARTICLES);
        if (exclusiveArticles != null && !exclusiveArticles.equals("")) {
            Iterator<Article> iter = articleElems.iterator();
            while (iter.hasNext()) {
                Article articleElem = iter.next();
                String articleInfo = getArticleInfo(webDriver, articleElem);
                if (!articleInfo.contains(exclusiveArticles)) {
                    iter.remove();
                }
            }
        }

        log("Exclusive articles " + exclusiveArticles + " : " + articleElems);

    }
// http://fr.vente-privee.com/wsinventory/Products/ProductFamily/4793686
// http://fr.vente-privee.com/wsinventory/Catalog/Universe/2479197
}
