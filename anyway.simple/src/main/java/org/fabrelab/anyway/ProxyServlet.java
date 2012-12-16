package org.fabrelab.anyway;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.fabrelab.anyway.constant.ProxyConstant;
import org.fabrelab.anyway.handler.ConnectHandler;
import org.fabrelab.anyway.handler.HeaderHandler;
import org.fabrelab.anyway.handler.DataHandler;
import org.fabrelab.anyway.helper.FacadeProxyHelper;
import org.fabrelab.anyway.helper.ProxyHelper;
import org.fabrelab.anyway.util.ProxyLoggerUtil;
import org.fabrelab.anyway.util.RealLoggerUtil;
import org.fabrelab.anyway.util.SimpleUtil;
import org.simpleframework.http.Form;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

public class ProxyServlet implements Container {
	private static final Logger log = Logger.getLogger(ProxyServlet.class.getName());

	//private static ProxyHelper proxyHelper = new TunnelInProxyHelper("structuredwiki.appspot.com", "80", "");
	private static ProxyHelper proxyHelper = new FacadeProxyHelper("openapitest.appspot.com", "80", "");
	
	public void handle(Request request, Response response) {
		try {
			String method = request.getMethod();
			if ("CONNECT".equalsIgnoreCase(method)) {
				ConnectHandler.doConnect(request, response);
			}
			if ("GET".equalsIgnoreCase(method)) {
				doGet(request, response);
			}
			if ("POST".equalsIgnoreCase(method)) {
				doPost(request, response);
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	

	/**
	 * Performs an HTTP GET request
	 * 
	 * @param realRequest
	 *            The {@link Request} object passed in by the servlet engine
	 *            representing the client request to be proxied
	 * @param realResponse
	 *            The {@link Response} object by which we can send a proxied
	 *            response to the client
	 */
	public void doGet(Request realRequest, Response realResponse) throws IOException, Exception {

		RealLoggerUtil.logRealRequest(realRequest, "GET");

		// Create a GET request
		String proxyURL = proxyHelper.getProxyURL(realRequest);

		HttpGet proxyRequest = new HttpGet(proxyURL);

		// Forward the request headers
		HeaderHandler.setProxyRequestHeaders(realRequest, proxyRequest, proxyHelper);

		ProxyLoggerUtil.logProxyRequest(proxyRequest, "GET");

		// Execute the proxy request
		HttpResponse proxyResponse = executeProxyRequest(proxyRequest);

		ProxyLoggerUtil.logProxyResponse(proxyRequest, proxyResponse);
		
		if (proxyResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
			handle304(realResponse);
		}else{
			HeaderHandler.setRealResponseHeaders(realRequest, proxyResponse, realResponse, proxyHelper);

			DataHandler.setRealResponseData(proxyResponse, realResponse);

			realRequest.getSocket().close();
		}
		
		RealLoggerUtil.logRealResponse(realRequest, realResponse);
	}



	/**
	 * Performs an HTTP POST request
	 * 
	 * @param realRequest
	 *            The {@link Request} object passed in by the servlet engine
	 *            representing the client request to be proxied
	 * @param realResponse
	 *            The {@link Response} object by which we can send a proxied
	 *            response to the client
	 */
	public void doPost(Request realRequest, Response realResponse) throws IOException,
			Exception {

		RealLoggerUtil.logRealRequest(realRequest, "POST");

		String proxyURL = proxyHelper.getProxyURL(realRequest);
		// Create a standard POST request
		HttpPost proxyRequest = new HttpPost(proxyURL);

		// Forward the request headers
		HeaderHandler.setProxyRequestHeaders(realRequest, proxyRequest, proxyHelper);

		DataHandler.handleStandardPost(proxyRequest, realRequest);

		ProxyLoggerUtil.logProxyRequest(proxyRequest, "POST");

		// Execute the proxy request
		HttpResponse proxyResponse = executeProxyRequest(proxyRequest);

		ProxyLoggerUtil.logProxyResponse(proxyRequest, proxyResponse);
		
		if (proxyResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
			
			handle304(realResponse);
			
		}else{

			HeaderHandler.setRealResponseHeaders(realRequest, proxyResponse, realResponse, proxyHelper);

			DataHandler.setRealResponseData(proxyResponse, realResponse);
			
			realRequest.getSocket().close();
		}
		
		RealLoggerUtil.logRealResponse(realRequest, realResponse);
	}


	private HttpResponse executeProxyRequest(HttpUriRequest httpMethodProxyRequest) throws IOException,
			Exception {
		// Create a default HttpClient
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.setRedirectHandler(new RedirectHandler(){
			@Override
			public URI getLocationURI(HttpResponse arg0, HttpContext arg1) throws ProtocolException {
				return null;
			}
			@Override
			public boolean isRedirectRequested(HttpResponse arg0, HttpContext arg1) {
				return false;
			}
		});
		HttpResponse response = httpclient.execute(httpMethodProxyRequest);
		return response;
	}


	// 304 needs special handling. See:
	// http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
	// We get a 304 whenever passed an 'If-Modified-Since'
	// header and the data on disk has not changed; server
	// responds w/ a 304 saying I'm not going to send the
	// body because the file has not changed.
	private void handle304(Response realResponse) {
		realResponse.set(ProxyConstant.STRING_CONTENT_LENGTH_HEADER_NAME, 0);
		realResponse.setCode(HttpStatus.SC_NOT_MODIFIED);
	}
	
}
