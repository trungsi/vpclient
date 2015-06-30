/**
 * 
 */
package com.trungsi.vpclient;

/**
 * @author trungsi
 *
 */
public class ArticleSelector {

	private Article article;
	private WebDriverPool wdProvider;

	public ArticleSelector(Article article, WebDriverPool wdProvider) {
		this.setArticle(article);
		this.setWdProvider(wdProvider);
		
	}

	/**
	 * @return the article
	 */
	public Article getArticle() {
		return article;
	}

	/**
	 * @param article the article to set
	 */
	public void setArticle(Article article) {
		this.article = article;
	}

	/**
	 * @return the wdProvider
	 */
	public WebDriverPool getWdProvider() {
		return wdProvider;
	}

	/**
	 * @param wdProvider the wdProvider to set
	 */
	public void setWdProvider(WebDriverPool wdProvider) {
		this.wdProvider = wdProvider;
	}

}
