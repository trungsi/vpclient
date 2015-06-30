/**
 * 
 */
package com.trungsi.vpclient;

/**
 * @author trungsi
 *
 */
public class SubCategorySelector {

	private SubCategory subCategory;
	private WebDriverPool wdProvider;

	public SubCategorySelector(SubCategory subCategory,
			WebDriverPool wdProvider) {
		this.setSubCategory(subCategory);
		this.setWdProvider(wdProvider);
	}

	public SubCategory getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(SubCategory subCategory) {
		this.subCategory = subCategory;
	}

	public WebDriverPool getWdProvider() {
		return wdProvider;
	}

	public void setWdProvider(WebDriverPool wdProvider) {
		this.wdProvider = wdProvider;
	}

}
