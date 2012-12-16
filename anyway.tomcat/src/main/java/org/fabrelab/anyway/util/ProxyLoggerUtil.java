package org.fabrelab.anyway.util;

import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

public class ProxyLoggerUtil {
	
	private static final Logger log = Logger.getLogger(ProxyLoggerUtil.class.getName());
	
	public static void logProxyRequest(HttpRequestBase proxyRequest, String method) {
		StringBuffer logString = new StringBuffer();
		logString.append("method  " + method + "\n");
		logString.append("RequestLine: " + proxyRequest.getRequestLine().getUri() + "\n");

		for (Header header : proxyRequest.getAllHeaders()) {
			logString.append(header.getName() + " = " + header.getValue() + "\n");
		}

		if ("POST".equals(method)) {

		}
		if(proxyRequest.getRequestLine().getUri().contains(".htm")){
			log.info("logProxyRequest: \n" + logString);
		}else{
			log.warning("logProxyRequest: \n" + logString);
		}
	}
	
	public static void logProxyResponse(HttpRequestBase proxyRequest, HttpResponse proxyResponse) {
		StringBuffer logString = new StringBuffer();
		logString.append("RequestLine: " + proxyRequest.getRequestLine().getUri() + "\n");
		logString.append("StatusLine: " + proxyResponse.getStatusLine()  + "\n");
		// Pass response headers back to the client
		Header[] headerArrayResponse = proxyResponse.getAllHeaders();
		for (Header header : headerArrayResponse) {
			String value = header.getValue();
			logString.append(header.getName() + " = " + value + "\n");
		}
		if(proxyRequest.getRequestLine().getUri().contains(".htm")){
			log.info("logProxyResponse: \n" + logString);
		}else{
			log.warning("logProxyResponse: \n" + logString);
		}
	}

}

