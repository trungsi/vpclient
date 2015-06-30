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
public class WebDriverPool {
    private final BlockingQueue<WebDriver> driverQueue = new LinkedBlockingQueue<WebDriver>();


    public WebDriverPool() {}
    
    public WebDriverPool(List<WebDriver> drivers) {
    	this.driverQueue.addAll(drivers);
    }
    
    /*public List<VPTask> doWithWebDriver(WDTask wdTask) {
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
    }*/

    public void add(WebDriver webDriver) {
        driverQueue.add(webDriver);
    }

	public <R> R doWithWebDriver2(NewWDTask<R> task) {
		WebDriver webDriver = null;
        try {
            webDriver = driverQueue.take();
            return task.execute(webDriver);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (webDriver != null)
                add(webDriver);
        }
	}

	public WebDriver getWebDriver() {
		try {
			return driverQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void releaseWebDriver(WebDriver webDriver) {
		add(webDriver);
	}
}
