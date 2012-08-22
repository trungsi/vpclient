/**
 * 
 */
package com.trungsi.vpclient;

/**
 * @author dtran091109
 *
 */
public class AddArticleEvent implements VPEvent {

	private String text;
	
	public AddArticleEvent(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}

}
