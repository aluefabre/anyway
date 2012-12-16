package org.fabrelab.anyway;


import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.fabrelab.anyway.constant.ProxyConstant;
import org.fabrelab.anyway.handler.DataHandler;
import org.fabrelab.anyway.handler.HeaderHandler;
import org.fabrelab.anyway.helper.FacadeProxyHelper;
import org.fabrelab.anyway.helper.ProxyHelper;
import org.fabrelab.anyway.util.ProxyLoggerUtil;
import org.fabrelab.anyway.util.RealLoggerUtil;

public class ProxyServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(ProxyServlet.class.getName());
	
	/**
	 * Serialization UID.
	 */
	private static final long serialVersionUID = 1L;
	
	private static ProxyHelper proxyHelper = null;
	
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		//proxyHelper = new TunnelInProxyHelper(config);
		proxyHelper = new FacadeProxyHelper(config);
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

		RealLoggerUtil.logRealRequest(httpServletRequest, "GET");
    	
		// Create a GET request
		String proxyURL = proxyHelper.getProxyURL(httpServletRequest);
		
		HttpGet proxyRequest = new HttpGet(proxyURL); 
		
		// Forward the request headers
		HeaderHandler.setProxyRequestHeaders(httpServletRequest, proxyRequest, proxyHelper);
    	
		ProxyLoggerUtil.logProxyRequest(proxyRequest, "GET");
		
		// Execute the proxy request
		HttpResponse proxyResponse = executeProxyRequest(proxyRequest);
		
		ProxyLoggerUtil.logProxyResponse(proxyRequest, proxyResponse);
		StringBuffer logString = null;
		if(proxyResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_NOT_MODIFIED) {
			logString = handle304(httpServletResponse);
		}else{
			logString = HeaderHandler.setRealResponseHeaders(httpServletRequest, proxyResponse, httpServletResponse, proxyHelper);
			DataHandler.setRealResponseData(proxyResponse, httpServletResponse);
		}
	
		RealLoggerUtil.logRealResponse(httpServletRequest, httpServletResponse, logString);
		
		log.info("doGet done");
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

		RealLoggerUtil.logRealRequest(httpServletRequest, "POST");
    	
		String proxyURL = proxyHelper.getProxyURL(httpServletRequest);
    	// Create a standard POST request
    	HttpPost proxyRequest = new HttpPost(proxyURL);
        	
    	// Forward the request headers
    	HeaderHandler.setProxyRequestHeaders(httpServletRequest, proxyRequest, proxyHelper);
    	
    	// Check if this is a mulitpart (file upload) POST
    	if(ServletFileUpload.isMultipartContent(httpServletRequest)) {
    		DataHandler.handleMultipartPost(proxyRequest, httpServletRequest);
    	} else {
    		DataHandler.handleStandardPost(proxyRequest, httpServletRequest);
    	}

    	ProxyLoggerUtil.logProxyRequest(proxyRequest, "POST");

    	// Execute the proxy request
    	HttpResponse proxyResponse = executeProxyRequest(proxyRequest);

    	ProxyLoggerUtil.logProxyResponse(proxyRequest, proxyResponse);
		
    	StringBuffer logString = null;
		if(proxyResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_NOT_MODIFIED) {
			logString = handle304(httpServletResponse);
		}else{
			logString =  HeaderHandler.setRealResponseHeaders(httpServletRequest, proxyResponse, httpServletResponse, proxyHelper);
			DataHandler.setRealResponseData(proxyResponse, httpServletResponse);
		}
		
		RealLoggerUtil.logRealResponse(httpServletRequest, httpServletResponse, logString);
		
		log.info("doPost done");
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
	private HttpResponse executeProxyRequest(HttpUriRequest httpMethodProxyRequest) throws IOException, ServletException {
		// Create a default HttpClient
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.setRedirectHandler(new RedirectHandler(){

			public URI getLocationURI(HttpResponse arg0, HttpContext arg1) throws ProtocolException {
				return null;
			}

			public boolean isRedirectRequested(HttpResponse arg0, HttpContext arg1) {
				return false;
			}
		});
		HttpResponse response = httpclient.execute(httpMethodProxyRequest);
		return response;
	}
	

	// 304 needs special handling.  See:
	// http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
	// We get a 304 whenever passed an 'If-Modified-Since'
	// header and the data on disk has not changed; server
	// responds w/ a 304 saying I'm not going to send the
	// body because the file has not changed.
	private StringBuffer handle304(HttpServletResponse httpServletResponse){
		StringBuffer logString = new StringBuffer();
		httpServletResponse.setIntHeader(ProxyConstant.STRING_CONTENT_LENGTH_HEADER_NAME, 0);
		httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		logString.append("StatusCode: " + HttpServletResponse.SC_NOT_MODIFIED + "\n");
		return logString;
	}

}
