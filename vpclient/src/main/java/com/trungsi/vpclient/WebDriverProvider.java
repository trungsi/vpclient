package com.trungsi.vpclient;

import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: trungsi
 * Date: 29/07/13
 * Time: 21:55
 * To change this template use File | Settings | File Templates.
 */
public class WebDriverProvider {
    private final BlockingQueue<WebDriver> driverQueue = new LinkedBlockingQueue<WebDriver>();


    public List<VPTask> doWithWebDriver(WDTask wdTask) {
        WebDriver webDriver = null;
        try {
            webDriver = driverQueue.take();
            return wdTask.execute(webDriver);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (webDriver != null)
                add(webDriver);
        }
    }

    public void add(WebDriver webDriver) {
        driverQueue.add(webDriver);
    }
}
