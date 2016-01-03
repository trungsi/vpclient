package com.trungsi.vpclient;

import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import com.gargoylesoftware.htmlunit.util.WebClientUtils;

import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.Page;

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
		super(BrowserVersion.CHROME);
		//setJavascriptEnabled(true);
		id = COUNTER.incrementAndGet();
	}

	protected WebClient modifyWebClient(WebClient client) {
		String httpProxyHost = System.getProperty(HTTP_PROXY_HOST);
		if (httpProxyHost != null && !httpProxyHost.equals("")) {
			int httpProxyPort = Integer.parseInt(System.getProperty(HTTP_PROXY_PORT, "8080"));
			//client.setProxyConfig(new ProxyConfig(httpProxyHost, httpProxyPort));
			this.setProxy(httpProxyHost, httpProxyPort);

			String httpProxyUsername = System.getProperty(HTTP_PROXY_USERNAME);
			if (httpProxyUsername != null && !httpProxyHost.equals("")) {
				String httpProxyPassword = System.getProperty(HTTP_PROXY_PASSWORD);
				DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) client.getCredentialsProvider();
			    credentialsProvider.addCredentials(httpProxyUsername, httpProxyPassword);
			}
		}
		client.waitForBackgroundJavaScript(15000);
		client.getOptions().setThrowExceptionOnScriptError(false);
		//client.getOptions().setCssEnabled(true);
        //WebClientUtils.attachVisualDebugger(client);
		client.setCssErrorHandler(new ErrorHandler() {

			@Override
			public void warning(CSSParseException exception)
					throws CSSException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void error(CSSParseException exception) throws CSSException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void fatalError(CSSParseException exception)
					throws CSSException {
				// TODO Auto-generated method stub
				
			}});
        this.webClient = client;

		return client;
	}
	
	public String postRequest(String url, String body) {
		try {
			WebRequest request = new WebRequest(new URL(url), HttpMethod.POST);
			request.setRequestBody(body);
			System.out.println(body);
            //System.out.println(request.getAdditionalHeaders());
			Page p = webClient.getPage(request);
            //System.out.println(p.getWebResponse().getResponseHeaders());
			return p.getWebResponse().getContentAsString();
		} catch (Exception e) {
			e.printStackTrace();
            return e.toString();
		}
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Driver " + id;
	}

	public String getRequest(String url) {
		try {
			System.out.println("get url = " + url);
			WebRequest request = new WebRequest(new URL(url), HttpMethod.GET);
			//request.setRequestBody(body);
			//System.out.println(body);
            //System.out.println(request.getAdditionalHeaders());
			Page p = webClient.getPage(request);
            //System.out.println(p.getWebResponse().getResponseHeaders());
			return p.getWebResponse().getContentAsString();
		} catch (Exception e) {
			e.printStackTrace();
            return e.toString();
		}
	}
}