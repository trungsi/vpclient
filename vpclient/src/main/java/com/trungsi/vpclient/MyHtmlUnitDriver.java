package com.trungsi.vpclient;

import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author dtran091109
 *
 */
class MyHtmlUnitDriver extends HtmlUnitDriver {

	public static final String HTTP_PROXY_PASSWORD = "httpProxyPassword";
	public static final String HTTP_PROXY_USERNAME = "httpProxyUsername";
	public static final String HTTP_PROXY_PORT = "httpProxyPort";
	public static final String HTTP_PROXY_HOST = "httpProxyHost";
	
	private WebClient webClient;
	private static final AtomicInteger COUNTER = new AtomicInteger();
	private final int id;
	
	public MyHtmlUnitDriver() {
		super(BrowserVersion.FIREFOX_3_6);
		id = COUNTER.incrementAndGet();
	}

	protected WebClient modifyWebClient(WebClient client) {
		String httpProxyHost = System.getProperty(HTTP_PROXY_HOST);
		if (httpProxyHost != null && !httpProxyHost.equals("")) {
			int httpProxyPort = Integer.parseInt(System.getProperty(HTTP_PROXY_PORT, "8080"));
			client.setProxyConfig(new ProxyConfig(httpProxyHost, httpProxyPort));
			
			String httpProxyUsername = System.getProperty(HTTP_PROXY_USERNAME);
			if (httpProxyUsername != null && !httpProxyHost.equals("")) {
				String httpProxyPassword = System.getProperty(HTTP_PROXY_PASSWORD);
				DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) client.getCredentialsProvider();
			    credentialsProvider.addCredentials(httpProxyUsername, httpProxyPassword);
			}
		}
		
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