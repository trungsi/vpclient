package com.trungsi.vpclient;

import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author dtran091109
 *
 */
class MyHtmlUnitDriver extends HtmlUnitDriver {

	private WebClient webClient;
	private static final AtomicInteger COUNTER = new AtomicInteger();
	private final int id;
	
	public MyHtmlUnitDriver() {
		super(BrowserVersion.FIREFOX_3_6);
		id = COUNTER.incrementAndGet();
	}

	protected WebClient modifyWebClient(WebClient client) {
		//client.setProxyConfig(new ProxyConfig("proxy.int.world.socgen", 8080));
	    //client.setThrowExceptionOnScriptError(false);
	    
	    //DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) client.getCredentialsProvider();
	    //credentialsProvider.addCredentials("duc-trung.tran", "TrungSi2009");
	    
		client.setThrowExceptionOnScriptError(false);

        this.webClient = client;

		return client;
	}
	
	public void postRequest(String url, String body) {
		try {
			WebRequest request = new WebRequest(new URL(url), HttpMethod.POST);
			request.setRequestBody(body);
			System.out.println(body);
			HtmlPage p = webClient.getPage(request);
			System.out.println(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Driver " + id;
	}
}