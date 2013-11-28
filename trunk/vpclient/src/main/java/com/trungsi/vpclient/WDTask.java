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
public abstract class WDTask implements VPTask {

    private final WebDriverProvider provider;

    public WDTask(WebDriverProvider provider) {
        this.provider = provider;
    }

    @Override
    public List<VPTask> execute() {
        return provider.doWithWebDriver(this);  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected abstract List<VPTask> execute(WebDriver webDriver);
}
