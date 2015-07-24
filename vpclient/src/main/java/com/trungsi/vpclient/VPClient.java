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
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.trungsi.vpclient.utils.DateRange;

import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static com.trungsi.vpclient.utils.CollectionUtils.*;

/**
 * @author trungsi
 *
 */
public class VPClient {

    static final Logger LOG = Logger.getLogger(VPClient.class);
	
	public static WebDriver loadDriver(Context context) {
		WebDriver driver = newDriver(context);
		
		//int count = 1;
		boolean loggedin = false;
		//do {
			loggedin = login(driver, context);
		//} while (!loggedin && count++ <= 2);

		if (!loggedin) {
			throw new RuntimeException("cannot log in after 2 tentatives " + driver.getPageSource());
		}
		
		return driver;
	}

	public static WebDriver cloneDriver(WebDriver driver, Context context) {
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
	private static WebDriver newDriver(Context context) {
		WebDriver driver = null;
		String driverName = context.get(Context.DRIVER_NAME, Context.HTML_UNIT);
		
		if (driverName.equals(Context.HTML_UNIT)) {
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
	private final static String homePage = "/vp4/home/default.aspx";
	private final static String vpLoungeHomePage = "/home/fr/Lounge";
	
	private static boolean login(WebDriver driver, Context context) {
		driver.get(baseUrl);// + "/vp4/Login/Portal.ashx");
		sleep (20);

		WebElement emailElem = driver.findElement(By.id("txtEmail"));
		emailElem.sendKeys(context.get(Context.USER));
		sleep(20);

		WebElement pwdElem = driver.findElement(By.id("txtPassword"));
		pwdElem.sendKeys(context.get(Context.PWD));
		sleep(20);

		WebElement submitElem = driver.findElement(By.id("btSubmit"));

		submitElem.click();

		sleep(20);

		return checkLoggedIn(driver);
	}

	private static boolean checkLoggedIn(WebDriver driver) {
		return driver.findElements(By.xpath("//ul[@class=\"currentlySales\"]")).size() == 1;
	}

	static interface VPSupplier<T> {
		T get() throws Exception;
	}
	
	private static <T> T watch(WebDriver webDriver, String name, VPSupplier<T> s) {
		long start = System.currentTimeMillis();
		try {
			return s.get();
		} catch (Exception e) {
			throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
		} finally {
			LOG.debug(webDriver + "==========" + name + " : " + (System.currentTimeMillis() - start));
		}
	}
	
	
	private static List<Sale> findCurrentSaleList(WebDriver driver) {
		return watch(driver, "findCurrentSaleList", () -> {
			//List<WebElement> currentSalesElem = driver.findElements(By.xpath("//ul[@class=\"currentlySales\"]//a[@id=\"linkSale\"]"));
	        List<WebElement> currentSalesElem = driver.findElements(By.xpath("//ul[@class=\"currentlySales\"]//a[@class=\"linkAccess\"]"));

			return toSaleList(currentSalesElem);
		});
		
	}

	public static List<Sale> getSalesList(WebDriver driver) {
		return watch(driver, "getSalesList", () -> {
			List<Sale> sales = getSalesList(driver, baseUrl + homePage);
			sales.addAll(getSalesList(driver, baseUrl + vpLoungeHomePage));
			
			return sales;
		});
		
	}

	private static List<Sale> getSalesList(WebDriver driver, String link) {
		goToLink(driver, link);
		
		List<Sale> sales = findCurrentSaleList(driver);
		sales.addAll(findSoonSaleList(driver));
		
		return sales;
	}

	private static List<Sale> toSaleList(List<WebElement> elems) {
		return elems.stream().map(elem -> getSaleInfos(elem)).collect(Collectors.toList());
	}

	private static Sale getSaleInfos(WebElement elem) {
        
        WebElement allElem = elem.findElement(By.xpath("h4"));
        String name = getTextOfHiddenElement(allElem);
        
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

        return new Sale(name, link, dateSales);

	}
	
	private static List<Sale> findSoonSaleList(WebDriver driver) {
		return watch(driver, "findSoonSaleList", () -> {
			List<WebElement> saleElems = driver.findElements(By.xpath("//ul[@class=\"soonSales\"]/li/div/a[@class='linkAccess']"));
			
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

			return toSaleList(saleElems);

		});
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

	private static boolean waitForElementReady(String xpath, long timeout, WebDriver driver) {
		long wait = 0;
		long interval = 200;
		while (wait < timeout) {
			if (evaluateXpath(xpath, driver)) {
				return true;
			}

			sleep(interval);
			wait += interval;
		}

		return false;
	}

        	
	private static boolean evaluateXpath (String xpathOrClosure,WebDriver driver) {
		return !driver.findElements(By.xpath(xpathOrClosure)).isEmpty();
	}

	private static void openSelectedSale(WebDriver driver, Sale selectedSale, Context context) {
		if (Thread.currentThread().isInterrupted()) return;
		VPSupplier<Void> s = () -> {
			//try {
				DateRange openDate = DateRange.parse(selectedSale.getDatesSale());
	            LOG.info(openDate.toString());
				Date currentDate = new Date();
				if (!openDate.containsDate(currentDate)) {
					long sleep = openDate.getNextTo(currentDate).getTimestamp();
					LOG.info("Sale not yet opened, sleep " + sleep);
					sleep(sleep);
				}
				
				goToLink(driver, selectedSale.getLink());
		
	
	            //try { // new interface is checked first. It seems that new interface is default now.
	            // sometimes, it's not a div but a nav, HTML5 ???
	            if(waitForElementReady("//nav[@class=\"obj_menuEV\"]", 5000L, driver)) {
	                // so new interface
	                context.setNewInterface();
	            } else if(!waitForElementReady("//div[@class=\"obj_menuEV\"]", 5000L, driver)) {
	            	// check adult age
	    			List<WebElement> accessLinks = driver.findElements(By.xpath("//div[@class=\"accessLink\"]/a"));
	    			if (!accessLinks.isEmpty()) {
	    				accessLinks.get(0).click();
	    				sleep(100);
	    			} else {
	    				throw new RuntimeException("Cannot find menu in seleted sale");
	    			}
	            	// old interface
	            }
				
	            return null;
			/*} catch (Error e) {
				if (driver.getPageSource().contains(selectedSale.getName())) {
					log(driver + " cannot open selectedSale, try to refresh");
					driver.navigate().refresh();
					openSelectedSale(driver, selectedSale, context);
				} else {
					throw e;
				}
			}*/
		};
		watch(driver, "openSelectedSale", s);
	}

	@SuppressWarnings("all")
	public static List<Category> findAllCategories(WebDriver driver, Sale selectedSale, Context context) {
		return watch(driver, "findAllCategories", () -> {
			openSelectedSale(driver, selectedSale, context);

			List<WebElement> catElems = null;
	        if (context.isNewInterface()) {
	            catElems = driver.findElements(By.xpath("//ul[@class=\"menuEV_Container\"]/li/a/span/..")); // car premier <a> sans <span> n'est pas intÃ©ressant
	        } else {
	            catElems = driver.findElements(By.xpath("//ul[@class=\"menuEV\"]/li/a/span/..")); // car premier <a> sans <span> n'est pas intÃ©ressant
	        }

			if (catElems.isEmpty()) {
				throw new Error("No category found for sale " + selectedSale.getName() + "\n" + driver.getPageSource());
			}

			List<Category> categories = filterCategories(catElems, context);

			LOG.info(categories.size() + " categories found : \n" + categories);

			return categories;

		});
	}

    @SuppressWarnings("all")
	private static List<Category> filterCategories(
			List<WebElement> catElems, Context context) {
		
		List<String> selectedCats = context.getSelectedCategories();
		return catElems.stream()
			.filter(catElem -> isSelectedCategory(selectedCats, catElem))
			.map(catElem -> new Category(catElem.getText(), catElem.getAttribute("href")))
			.collect(Collectors.toList());
	}

	private static boolean isSelectedCategory(List<String> selectedCats, WebElement catElem) {
		String text = catElem.getText().toLowerCase();
		return (!text.contains("produits disponibles") && !text.contains("tous les produits")) &&
				(selectedCats.isEmpty() || (listContains(selectedCats, text)));
	}

	public static boolean addArticle(WebDriver driver, Article article, Context context) {
		return watch(driver, "addArticle", () -> {
			boolean added = false;
			//String mainWindowHandle = driver.getWindowHandle();
			try {
				Map<String, Object> result = openExpressPurchaseWindow(driver, article);
				if ((Boolean)result.get("ok")) {
					added = addArticleToCart(driver, article, context);
				} else {
					LOG.info(" Cannot add article $articleName.\nCause :" + result.get("message"));
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
					LOG.debug("window close");
					sleep (50);
				}

				//switchToWindow(driver, mainWindowHandle);
			}

			LOG.info(driver + " article (" + article.getInfo() + ") added " + (added ? "successful" : "failed"));
			
			return added;

		});
	}

	private static boolean addArticleToCart (WebDriver driver, Article article, Context context) {
		return watch(driver, "addArticleToCart", () -> {
			
            boolean selected = selectSize(driver, article, context);

            LOG.info("selected" + selected);
			if (selected) {
                return submitAddToCartAction(driver);
			} else {
				LOG.info(article.getInfo() + " No appropriate size");
				return false;
			}
		});
					
	}

    private static boolean submitAddToCartAction(WebDriver driver) {
        String[] familyAndProductId = getFamilyAndProductId(driver);
        String familyId = familyAndProductId[0];
        String productId = familyAndProductId[1];


        LOG.info("familyId:" + familyId +", productId:" + productId);
        if (familyId.equals(productId)) {
            throw new RuntimeException("familyId is same as productId");
        }

        String result = ((MyHtmlUnitDriver) driver).postRequest(baseUrl + "/cart/CartServices/AddToCartOrCanBeReopened",
                        "pfId=" + URLEncoder.encode(familyId) +
                        "&pId=" + URLEncoder.encode(productId) +
                        "&q=1");
        //LOG.info(result);
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
    }

    private static boolean isSingleSizeProduct(WebDriver driver) {
    	return !driver.findElements(By.id("singleProduct")).isEmpty();
    }
    
    public static String[] getFamilyAndProductId(WebDriver driver) {
       /*
        single product

        <span id="singleProduct" class="productName" productfamilyid="5932388" productid="203725639" availablequantity="277" productidencrypted="9xcHtQIxG6p6jTvAczCraA==" productfamilyencryptedid="n+I00p1i4i5+Balr7DC0Zw==">15800/7</span>
         */

        if (isSingleSizeProduct(driver)) {
        	WebElement singleProductElem = driver.findElement(By.id("singleProduct"));
            return new String[] {
                    singleProductElem.getAttribute("productfamilyid"),
                    singleProductElem.getAttribute("productid")};
        } else { // not single product
            /*
            <select id="model" class="model">
						<option value="5876281" productfamilyencryptedid="mvyYrj1v96cDNH5fZnST7w==" class="defaultOption">- Choisissez -</option>
							<option value="197495462" productidencrypted="PZ/rrHsI4AVLuR1IcphDlg==" availablequantity="1">
								T. 40
								- dispo.
							</option>
							<option value="197495463" productidencrypted="lxLsChnULegIFF/If7aGqw==" availablequantity="0">
								T. 41
								- épuisé
							</option>
							<option value="197495464" productidencrypted="YfIbqEuRFTD9igbi1G7Pzg==" availablequantity="0">
								T. 42
								- épuisé
							</option>
							<option value="197495465" productidencrypted="QljhGrP+6u5LpIqmSQ4qdA==" availablequantity="0">
								T. 43
								- épuisé
							</option>
							<option value="197495466" productidencrypted="U7BQ52Xk9Cyc+Czay0fVaw==" availablequantity="0">
								T. 44
								- épuisé
							</option>
							<option value="197495467" productidencrypted="iiofRPyrKS+sQXpQDxVGbA==" availablequantity="0">
								T. 45
								- épuisé
							</option>
					</select>
             */
            WebElement selectElem = driver.findElement(By.id("model"));
            Select select = new Select(selectElem);
            return new String[] {
                    select.getOptions().get(0).getAttribute("value"), // first element contains familyId
                    select.getAllSelectedOptions().get(0).getAttribute("value")};
        }
    }

    /*
    @see: getFamilyAndProductId()
     */
    public static boolean selectSize(WebDriver driver, Article article, Context context) {
        if (isSingleSizeProduct(driver)) {
            LOG.info(article.getInfo() + " No model/size found. The article must not have this info");
            
            WebElement productSize = driver.findElement(By.id("singleProduct"));
            
            String sizeText = productSize.getText();
            //println("sizeText=" + sizeText);
            
            List<String> preferedSize = SizeHelper.getPreferedSize(article, context, null);
            boolean match = listContains(preferedSize, sizeText);
            
            LOG.info("sizeText=" + sizeText + " match (" + match + ") in " + preferedSize);
            if (sizeText.contains("T.")
                    && !match) {
                LOG.info(article.getInfo() + " size " + sizeText + " not in " + preferedSize);
                //return false;
            }
            
            LOG.debug("Unique size model ??? \n" + driver.getPageSource());
            return true;
        } else {
        	WebElement selectElems = driver.findElement(By.id("model"));
            return getMostAppropriateSizes(article, context, selectElems);
        }
    }

    private static boolean getMostAppropriateSizes(Article article,
			Context context, WebElement selectElem) {
        //log(driver.getPageSource());
		Select select = new Select(selectElem);
		
		return selectMostAppropriateSizes(article, context, select);
		
		//return selected;

	}

	private static boolean selectMostAppropriateSizes (Article article, Context context, Select select) {
		
		List<String> availableSizes = select.getOptions().stream().map(option -> getOptionText(option)).collect(Collectors.toList());
        List<String> preferedSize = SizeHelper.getPreferedSize(article, context, availableSizes);
		if (preferedSize.isEmpty()) {
            LOG.info("No prefered size found for " + article.getInfo());
			return false;
		} else {
            //log(driver.getPageSource());
			LOG.info("preferedSize " + preferedSize + " selected for article " + article.getInfo());
			return selectSize(select, preferedSize);
		}
	}
	
	private static boolean selectSize(Select select, List<String> selectSizeList) {
		
		List<WebElement> options = select.getOptions();

		for ( int i = 0; i < options.size(); i++) {
			WebElement option = options.get(i);

			String optionText = getOptionText(option);

			if (listContains(selectSizeList, optionText)) {
				LOG.info("selected index " + i + ", " + optionText + ", " + selectSizeList);
				select.selectByIndex(i);
				//return optionText
				return true;
			} else {
				LOG.info("optionText = " + optionText + " NOT FOUND in " + selectSizeList);
			}
		}
		
		return false;
	}

    static String getOptionText(WebElement option) {
        // option may be hidden here
        return getTextOfHiddenElement(option);
    }

	@SuppressWarnings("all")
	public static Map<String, Object> openExpressPurchaseWindow(WebDriver driver, Article article) {
		long start = System.currentTimeMillis();
		try {
			
			// must activate javascript because will submit form using ajax
			//HtmlUnitDriver htmlUnitDriver = (HtmlUnitDriver) driver;
			//htmlUnitDriver.setJavascriptEnabled(true);
						
			String openNewWindow = article.getLink();
            int time = 0;

			do {
                if(time > 0) {
                    sleep(50);
                    LOG.info("retry to open purchase windows " + (time+1) + " times");
                }
                goToLink(driver, openNewWindow);

            } while (!driver.getCurrentUrl().contains(openNewWindow) && ++time <= 10);

            LOG.info("openExpressPurchaseWindow:" + article + " -> " + driver.getCurrentUrl());

			//sleep(500);
            if (driver.getCurrentUrl().contains(openNewWindow))
			    return map(entry("ok", (Object) Boolean.TRUE));
            else
                return map(entry("ok", (Object) Boolean.FALSE));
		} finally {
			long time = System.currentTimeMillis() - start;
			LOG.info(driver + " openExpressPurchaseWindow : " + time);
		}
	}

	static void goToLink(WebDriver driver, String link) {
		if (!link.startsWith("http")) {
			link = baseUrl + link;
		}
		driver.navigate().to(link);
        //assert driver.getCurrentUrl().equals(link) : driver.getCurrentUrl() + " <> " + link;
	}
	
	@SuppressWarnings("all")
	public static List<Article> findAllArticlesInSubCategory(WebDriver driver, SubCategory subCategory, Context context) {
		long start = System.currentTimeMillis();

		// go to subCategory page
        goToLink(driver, subCategory.getLink());

		//sleep(500);

        //log(driver.getPageSource());
		List<Article> articles = new ArrayList<Article>();
		
		// normally, there is <ul class="artList">
		// but in Summer Camp, there is <ul class="artList viewAllProduct">
		List<WebElement> articleElems = driver.findElements(
				By.xpath("//ul[starts-with(@class,\"artList\")]/li"));
		
		LOG.info("after findArticleElements " + articleElems.size() + ", "+ (System.currentTimeMillis() - start));
		if (articleElems.isEmpty()) {
			LOG.info(" Cannot find ul with class 'artList viewAllProduct'. Will parse Json to get article infos");
			articles.addAll(findAllArticlesByParsingJson(driver, subCategory));


		} else {
			for (WebElement articleElem : articleElems) {
				//List<WebElement> elems = articleElem.findElements(By.xpath(".//*"));
				//System.out.println(elems);
                //log("articleElem: " + articleElem.getText() + category + subCategory);
				String name = articleElem.findElement(By.xpath(".//div[@class=\"infoArtTitle\"]")).getText();
				// Achat Express link could not exist
				// have to treate this case by using Fiche Produit
				//try {
					String link = null;
                    if (context.isNewInterface()) {
                        List<WebElement> xPressBts = articleElem.findElements(By.xpath(".//a[@class=\"infoExpressBt\"]"));
                        if (xPressBts.isEmpty()) {
                        	LOG.error("Cannot find 'Achat express' button for " + name);
                        	continue;
                        } else {
                        	link = xPressBts.get(0).getAttribute("href");
                        }
                    } else {
                    	List<WebElement> xPressBts = articleElem.findElements(By.xpath(".//a[@class=\"btStoreXpress\"]"));
                        if (xPressBts.isEmpty()) {
                        	LOG.error("Cannot find 'Achat express' button for " + name);
                        	continue;
                        } else {
	                        link = xPressBts.get(0).getAttribute("onclick");
	                        int firstIndex = link.indexOf("'");
	                        int lastIndex = link.lastIndexOf("'");
	                        link = link.substring(firstIndex+1, lastIndex);
                        }
                    }

					articles.add(new Article(name, link, subCategory));
				/*} catch (NoSuchElementException e) {
					LOG.error("Cannot find 'Achat express' button for " + name + " in " + articleElem.findElements(By.xpath(".//a[@class=\"btStoreXpress\"]")) + articleElem.findElements(By.xpath(".//span[@class=\"artState\"]")));
				}*/
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

		LOG.info(driver + " findAllArticlesInSubCategory " + subCategory.getInfo() + " : size=" + articleElems.size() + " , " + time);

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
            LOG.info("parsing JSON found " + articles.size() + " articles : " + (System.currentTimeMillis() - start));
        }

        return articles;
    }

    private static void openCategory(WebDriver driver, Category category) {
		long start = System.currentTimeMillis();
		
		String link = category.getLink();
		goToLink(driver, link);
		
		long time = System.currentTimeMillis() - start;
		LOG.info(driver + " openCategory (" + category.getName() + ") : " + time);
	}

	@SuppressWarnings("all")
	public static List<SubCategory> findSubCategories(WebDriver driver, Category category, Context context) {
		long start = System.currentTimeMillis();

		openCategory(driver, category);

		List<WebElement> subCategoryElems = driver.findElements(By.xpath("//ul[@class='menuEV_Container']/li[@class='open']//ul[@class=\"subMenuEV\"]/li/a"));

        List<SubCategory> subCategories = new ArrayList<>();

        if (subCategoryElems.isEmpty()) {
			LOG.info("No sub categories found in " + category.getName());
            //log(driver.getPageSource());
            LOG.info("Create fake SubCategory");
            subCategories.add(SubCategory.fake(category));
		} else {
            LOG.info(subCategoryElems.size() + " sub categories found in " + category.getName());

            for (WebElement elem : subCategoryElems) {
                try {
                    if (isSelectedSubCateogory(elem, context)) {
                        try {
                            subCategories.add(
                                    new SubCategory(elem.getText(), elem.getAttribute("href"), category));
                        } catch (StaleElementReferenceException e) {
                            LOG.info("staleElementException:\n" + driver.getCurrentUrl() + driver.getPageSource());
                            throw new RuntimeException(e);
                        }
                    }
                } catch (StaleElementReferenceException e) {
                    LOG.info("staleElementException:\n" + driver.getCurrentUrl() + "\n" + driver.getPageSource());
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
		LOG.debug(driver + " findSubCategories (" + category.getName() + ") : size=" + subCategories.size()+ " , " + time);


		return manSubCats;
	}
	
	private static boolean isSelectedSubCateogory(WebElement subCatElem, Context context) {
		String text = subCatElem.getText().toLowerCase();
		List<String> ignoreSubCats = context.getIgnoreSubCategories();
		return (!text.contains("produits disponibles") && !text.contains("tous les produits")) && // ignore page which contains all products
			(ignoreSubCats.isEmpty() || !listContains(ignoreSubCats, text));
	}

    public static void goToViewOrders(WebDriver driver) {
        HtmlUnitDriver htmlUnitDriver = (HtmlUnitDriver) driver;
        htmlUnitDriver.setJavascriptEnabled(true);


        goToLink(driver, "/memberaccount/order");
        sleep(20000);

        //System.out.println(htmlUnitDriver.executeScript("return $._data($('.pagination a.navRight')[0], 'events')"));
    }

    public static List<Map<String, String>> getOrdersInPage(WebDriver driver) {
        List<Map<String, String>> orders = new ArrayList<>();

        List<WebElement> orderLineElements = driver.findElements(By.xpath("//table[@id='ordersTable']/tbody/tr[@class='tableLine1']"));
        for (WebElement orderLineElem : orderLineElements) {
            List<WebElement> orderTDElements = orderLineElem.findElements(By.xpath("td"));

            Map<String, String> order = new HashMap<>();
            order.put("name", orderTDElements.get(0).getText().replaceAll("\n", ""));
            //order.put("id", orderTDElements.get(1).getText());
            //System.out.println(orderTDElements.get(0));
            String id = orderTDElements.get(0).getAttribute("id").substring("orderCmd_".length()+1);
            order.put("id", id);
            order.put("date", orderTDElements.get(1).getText());
            order.put("amount", orderTDElements.get(2).getText());
            order.put("status", orderTDElements.get(3).getText());

            List<WebElement> orderLinkElements = orderTDElements.get(0).findElements(By.xpath("a"));
            order.put("orderDetailLink", orderLinkElements.get(0).getAttribute("href"));
            /*if (orderLinkElements.size() == 3) { // normal order
                order.put("followUpLink", orderLinkElements.get(1).getAttribute("href"));
                order.put("helpAndContactLink", orderLinkElements.get(2).getAttribute("href"));
            } else { // return order, no followUp link
                order.put("helpAndContactLink", orderLinkElements.get(1).getAttribute("href"));
            }*/
            orders.add(order);
        }
        return orders;
    }


    public static boolean nextOrderPage(WebDriver driver) {
        //System.out.println(driver.getPageSource());
        List<WebElement> nextButton = //driver.findElements(
                //By.cssSelector(".pagination a.navRight"));
                //By.xpath("//div[@class='pagination']/a[@class='navRight']"));
                driver.findElements(By.xpath("//span[@class='PageNumbers']/following-sibling::*"));
        if (nextButton.isEmpty()) return false;

        System.out.println(nextButton.get(0).getText());
        nextButton.get(0).click();
        sleep(5000);

        //System.out.println(driver.getPageSource());

        return true;
    }

    public static List<Map<String, String>> getAllOrders(WebDriver driver) {
        ArrayList<Map<String, String>> orders = new ArrayList<>();

        goToViewOrders(driver);

        boolean nextOrderLink = false;

        do {
            orders.addAll(getOrdersInPage(driver));
            nextOrderLink = nextOrderPage(driver);
        } while (nextOrderLink);

        return orders;
    }

    public static List<Map<String, String>> getOrderDetail(WebDriver driver, Map<String, String> order) {
        ArrayList<Map<String, String>> orderDetail = new ArrayList<>();

        goToLink(driver, order.get("orderDetailLink"));

        HashMap<String, String> detailMap = new HashMap<>();
        WebElement totalPriceRecapElem = driver.findElement(By.id("TotalPriceRecap"));
        detailMap.put("totalAmount", totalPriceRecapElem.getText());

        List<WebElement> totalReturnElem = driver.findElements(By.xpath("//table[@id='commandReturnTable']//tr[2]/td[@class='td3']"));
        if (!totalReturnElem.isEmpty()) {
            detailMap.put("returnAmount", totalReturnElem.get(0).getText());
        } else {
            detailMap.put("returnAmount", "0 €");
        }

        orderDetail.add(detailMap);

        return orderDetail;
    }

    public static boolean matchFilterArticle(WebDriver webDriver, Article article, Context context) {
    	String exclusiveArticles = context.get(Context.EXCLUSIVE_ARTICLES);
    	return Strings.isNullOrEmpty(exclusiveArticles) ||
    			article.getInfo().toLowerCase().contains(exclusiveArticles);
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
        LOG.info(substring);

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

        //sleep(5000);
        //log(driver.getPageSource());
        //return driver.findElements(By.xpath("//table[@id='commandTable']//tr[contains(@class, 'cartdetail')]")).size();
    }


// http://fr.vente-privee.com/wsinventory/Products/ProductFamily/4793686
// http://fr.vente-privee.com/wsinventory/Catalog/Universe/2479197
}
