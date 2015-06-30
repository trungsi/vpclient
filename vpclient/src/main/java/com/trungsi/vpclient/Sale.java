/**
 * 
 */
package com.trungsi.vpclient;

/**
 * @author trungsi
 *
 */
public class Sale {

	private String name;
	private String link;
	private String datesSale;
	
	
	public Sale(String name, String link, String datesSale) {
		super();
		this.name = name;
		this.link = link;
		this.datesSale = datesSale;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getDatesSale() {
		return datesSale;
	}
	public void setDatesSale(String datesSale) {
		this.datesSale = datesSale;
	}
	
}
