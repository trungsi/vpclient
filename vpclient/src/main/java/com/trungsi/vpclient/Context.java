package com.trungsi.vpclient;

import static com.trungsi.vpclient.utils.CollectionUtils.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Context extends HashMap<String, String> {

	public static final String PWD = "pwd";
	public static final String USER = "user";
	
	public static final String HTML_UNIT = "HtmlUnit";
	public static final String DRIVER_NAME = "driverName";
	
	public static final String IGNORE_SUB_CATS = "ignoreSubCats";
	
	public static final String SELECTED_CATS = "selectedCats";
	//public static final String SELECTED_SALE = "selectedSale";
	
	public static final String WOMAN_JEAN_SIZES = "womanJeanSizes";
	public static final String WOMAN_SHOES_SIZES = "womanShoesSizes";
	public static final String WOMAN_LINGERIE_SIZES = "womanLingerieSizes";
	public static final String WOMAN_CLOTHING_SIZES = "womanClothingSizes";
	public static final String WOMAN_SHIRT_SIZES = "womanShirtSizes";
	
	public static final String GIRL_SHOES_SIZES = "girlShoesSizes";
	public static final String GIRL_CLOTHING_SIZES = "girlClothingSizes";
	public static final String BOY_SHOES_SIZES = "boyShoesSizes";
	public static final String BOY_CLOTHING_SIZES = "boyClothingSizes";
	
	public static final String MAN_JEAN_SIZES = "manJeanSizes";
	public static final String MAN_SHOES_SIZES = "manShoesSizes";
	public static final String MAN_COSTUME_SIZES = "manCostumeSizes";
	public static final String MAN_CLOTHING_SIZES = "manClothingSizes";
	public static final String MAN_SHIRT_SIZES = "manShirtSizes";
	
	//public static final String SELECTED_SALE_DATE = "selectedSaleDate";
	//public static final String SELECTED_SALE_LINK = "selectedSaleLink";
	
	public static final String NEW_INTERFACE = "NEW_INTERFACE";
	public static final String EXCLUSIVE_ARTICLES = "exclusiveArticles";
	public static final String CHROME = "CHROME";
	public static final String FIREFOX = "FIREFOX";
	public static final String SAFARI = "SAFARI";
	

	public void setNewInterface() {
		put(NEW_INTERFACE, "true");
	}
	
	public boolean isNewInterface() {
		return "true".equals(get(NEW_INTERFACE));
	}

	public List<String> getIgnoreSubCategories() {
		String ignoreSubCatsString = get(IGNORE_SUB_CATS);
		List<String> ignoreCats = ignoreSubCatsString != null && !ignoreSubCatsString.equals("") ? 
				list(ignoreSubCatsString.split("\\|")) : new ArrayList<String>();
		return ignoreCats;
	}

	public List<String> getSelectedCategories() {
		String selectedCatsString = get(SELECTED_CATS);
		if (selectedCatsString != null && !selectedCatsString.equals("")) {
			return list(selectedCatsString.split("\\|"));
		}
		
		return list();
	}
	
	public String get(String key, String def) {
		String value = get(key);
		return value != null ? value : def;
	}
}
