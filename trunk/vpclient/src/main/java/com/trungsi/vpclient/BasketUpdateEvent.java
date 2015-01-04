/**
 * 
 */
package com.trungsi.vpclient;

/**
 * @author dtran091109
 *
 */
public class BasketUpdateEvent implements VPEvent {

	private final Basket basket;
	
	public BasketUpdateEvent(Basket basket) {
		this.basket = basket;
	}
	
	public Basket getBasket() {
		return basket;
	}

}
