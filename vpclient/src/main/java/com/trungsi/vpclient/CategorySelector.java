/**
 * 
 */
package com.trungsi.vpclient;

/**
 * @author trungsi
 *
 */
public class CategorySelector {

	private Category category;
	private WebDriverPool wdProvider;

	public CategorySelector(Category category, WebDriverPool wdProvider) {
		this.setCategory(category);
		this.setWdProvider(wdProvider);
	}

	public WebDriverPool getWdProvider() {
		return wdProvider;
	}

	public void setWdProvider(WebDriverPool wdProvider) {
		this.wdProvider = wdProvider;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

}
