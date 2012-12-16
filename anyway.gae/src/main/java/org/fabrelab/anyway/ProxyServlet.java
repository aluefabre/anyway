package org.fabrelab.anyway;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sun.net.www.http.HttpClient;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class ProxyServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(ProxyServlet.class.getName());
	
	/**
	 * Serialization UID.
	 */
	private static final long serialVersionUID = 1L;
	
	private static ProxyHelper proxyHelper = null;
	
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		log.info("new TunnelOutProxyHelper");
		proxyHelper = new TunnelOutProxyHelper(config);
		super.init(config);
	}
	
	/**
	 * Performs an HTTP GET request
	 * @param httpServletRequest The {@link HttpServletRequest} object passed
	 *                            in by the servlet engine representing the
	 *                            client request to be proxied
	 * @param httpServletResponse The {@link HttpServletResponse} object by which
	 *                             we can send a proxied response to the client 
	 */
	public void doGet (HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    		throws IOException, ServletException {

    	logRequest(httpServletRequest, "GET");
    	
		// Create a GET request
		String proxyURL = proxyHelper.getProxyURL(httpServletRequest);
		
		HTTPRequest proxyRequest = new HTTPRequest(new URL(proxyURL), HTTPMethod.GET); 
		
		// Forward the request headers
		HeaderHandler.setProxyRequestHeaders(httpServletRequest, proxyRequest, proxyHelper);
    	
		logProxyRequest(proxyRequest, "GET");
		
		// Execute the proxy request
		HTTPResponse proxyResponse = executeProxyRequest(proxyRequest);
		
		if(handle304(httpServletResponse, proxyResponse)){
			return;
		}
		
		logProxyResponse(proxyResponse);
		HeaderHandler.setRealResponseHeaders(httpServletRequest, proxyResponse, httpServletResponse, proxyHelper);

		setRealResponseData(proxyResponse, httpServletResponse);
	}

	// 304 needs special handling.  See:
	// http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
	// We get a 304 whenever passed an 'If-Modified-Since'
	// header and the data on disk has not changed; server
	// responds w/ a 304 saying I'm not going to send the
	// body because the file has not changed.
	private boolean handle304(HttpServletResponse httpServletResponse, HTTPResponse proxyResponse){
		if(proxyResponse.getResponseCode() == HttpServletResponse.SC_NOT_MODIFIED) {
			httpServletResponse.setIntHeader(ProxyConstant.STRING_CONTENT_LENGTH_HEADER_NAME, 0);
			httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return true;
		}
		return false;
	}

	/**
	 * Performs an HTTP POST request
	 * @param httpServletRequest The {@link HttpServletRequest} object passed
	 *                            in by the servlet engine representing the
	 *                            client request to be proxied
	 * @param httpServletResponse The {@link HttpServletResponse} object by which
	 *                             we can send a proxied response to the client 
	 */
	public void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        	throws IOException, ServletException {

    	logRequest(httpServletRequest, "POST");
    	
		String proxyURL = proxyHelper.getProxyURL(httpServletRequest);
    	// Create a standard POST request
		HTTPRequest proxyRequest = new HTTPRequest(new URL(proxyURL), HTTPMethod.POST); 
        	
    	// Forward the request headers
    	HeaderHandler.setProxyRequestHeaders(httpServletRequest, proxyRequest, proxyHelper);
    	
    	logProxyRequest(proxyRequest,"POST");
 
    	PostHandler.handleStandardPost(proxyRequest, httpServletRequest);
    	
    	// Execute the proxy request
    	HTTPResponse proxyResponse = executeProxyRequest(proxyRequest);

		if(handle304(httpServletResponse, proxyResponse)){
			return;
		}

		logProxyResponse(proxyResponse);
		HeaderHandler.setRealResponseHeaders(httpServletRequest, proxyResponse, httpServletResponse, proxyHelper);

		
		setRealResponseData(proxyResponse, httpServletResponse);
    }


	/**
	 * Executes the {@link HttpMethod} passed in and sends the proxy response
	 * back to the client via the given {@link HttpServletResponse}
	 * @param httpMethodProxyRequest An object representing the proxy request to be made
	 * @param httpServletResponse An object by which we can send the proxied
	 *                             response back to the client
	 * @throws IOException Can be thrown by the {@link HttpClient}.executeMethod
	 * @throws ServletException Can be thrown to indicate that another error has occurred
	 */
	private HTTPResponse executeProxyRequest(HTTPRequest httpMethodProxyRequest) throws IOException, ServletException {
		// Create a default HttpClient
		httpMethodProxyRequest.getFetchOptions().doNotFollowRedirects();
		URLFetchService httpclient = URLFetchServiceFactory.getURLFetchService();
		HTTPResponse response = httpclient.fetch(httpMethodProxyRequest);
		return response;
	}

	private void setRealResponseData(HTTPResponse proxyResponse, HttpServletResponse httpServletResponse) throws IOException {
	    // Send the content to the client
	    InputStream inputStreamProxyResponse = new ByteArrayInputStream(proxyResponse.getContent());
	    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStreamProxyResponse);
	    OutputStream outputStreamClientResponse = httpServletResponse.getOutputStream();
	    int intNextByte;
	    while ( ( intNextByte = bufferedInputStream.read() ) != -1 ) {
	    	outputStreamClientResponse.write(intNextByte);
	    }
	    outputStreamClientResponse.flush();
	}

	private void logProxyResponse(HTTPResponse proxyResponse) {
		StringBuffer headerNames = new StringBuffer();
		  // Pass response headers back to the client
		List<HTTPHeader> headerArrayResponse = proxyResponse.getHeaders();
        for(HTTPHeader header : headerArrayResponse) {
       		String value = header.getValue();
       		headerNames.append(header.getName() + " = " + value + "\n");
        }
        log.info("logProxyResponse: \n " + headerNames);
	}



	@SuppressWarnings("unchecked")
	private void logRequest(HttpServletRequest httpServletRequest, String method) {
		StringBuffer logRequestString = new StringBuffer();
		
		logRequestString.append("method  " + method + "\n" );
		logRequestString.append("getPathInfo()  " + httpServletRequest.getPathInfo() + "\n" );
		logRequestString.append("getRequestURL()  " + httpServletRequest.getRequestURL().toString() + "\n" );
		logRequestString.append("getQueryString()  " + httpServletRequest.getQueryString() + "\n" );
		
		Enumeration enumerationOfHeaderNames = httpServletRequest.getHeaderNames();
		
		while(enumerationOfHeaderNames.hasMoreElements()) {
			
			String stringHeaderName = (String) enumerationOfHeaderNames.nextElement();
			
			// As per the Java Servlet API 2.5 documentation:
			//		Some headers, such as Accept-Language can be sent by clients
			//		as several headers each with a different value rather than
			//		sending the header as a comma separated list.
			// Thus, we get an Enumeration of the header values sent by the client
			Enumeration enumerationOfHeaderValues = httpServletRequest.getHeaders(stringHeaderName);
			
			while(enumerationOfHeaderValues.hasMoreElements()) {
				String stringHeaderValue = (String) enumerationOfHeaderValues.nextElement();
				logRequestString.append(stringHeaderName + " = " + stringHeaderValue + "\n" );
			}
		}

		
		if("POST".equals(method)){
			// Get the client POST data as a Map
			Map<String, String[]> mapPostParameters = (Map<String,String[]>) httpServletRequest.getParameterMap();
			// Iterate the parameter names
			for(String stringParameterName : mapPostParameters.keySet()) {
				// Iterate the values for each parameter name
				String[] stringArrayParameterValues = mapPostParameters.get(stringParameterName);
				for(String stringParamterValue : stringArrayParameterValues) {
					logRequestString.append(stringParameterName + " = " + stringParamterValue + "\n");
				}
			}
		}
		log.info("logRealRequest: \n " + logRequestString);
	}
	
	
	private void logProxyRequest(HTTPRequest proxyRequest, String method) {
		StringBuffer logRequestString = new StringBuffer();
		
		logRequestString.append("method  " + method+ "\n" );
		logRequestString.append("getURL().toExternalForm()  " + proxyRequest.getURL().toExternalForm()+ "\n" );
    	
		for(HTTPHeader header : proxyRequest.getHeaders()){
			logRequestString.append(header.getName() + " = " + header.getValue() + "\n" );
		}
		
		if("POST".equals(method)){
			if(proxyRequest.getPayload()!=null && proxyRequest.getPayload().length>0){
				String payload = new String(proxyRequest.getPayload());
				logRequestString.append("Post Payload " +payload + "\n" );
			}
		}
		
		log.info("logProxyRequest: \n " + logRequestString);
	}
}
