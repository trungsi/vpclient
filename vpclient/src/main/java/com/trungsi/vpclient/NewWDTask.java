/**
 * 
 */
package com.trungsi.vpclient;

import org.openqa.selenium.WebDriver;

/**
 * @author trungsi
 *
 */
public interface NewWDTask<T> {

	T execute(WebDriver webDriver);
}
