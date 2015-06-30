package com.trungsi.vpclient;

import org.openqa.selenium.WebDriver;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: trungsi
 * Date: 29/07/13
 * Time: 21:51
 * To change this template use File | Settings | File Templates.
 */
public interface WDTask {

    List<VPTask> execute(WebDriver webDriver);
}
