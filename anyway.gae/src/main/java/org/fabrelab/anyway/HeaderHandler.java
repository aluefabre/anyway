package org.fabrelab.anyway;

import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;

public class HeaderHandler {
	private static final Logger log = Logger.getLogger(HeaderHandler.class.getName());
	
	 /**
     * Retreives all of the headers from the servlet request and sets them on
     * the proxy request
     * 
     * @param httpServletRequest The request object representing the client's
     *                            request to the servlet engine
     * @param proxyRequest The request that we are about to send to
     *                                the proxy host
     */
    @SuppressWarnings("unchecked")
	static void setProxyRequestHeaders(HttpServletRequest httpServletRequest, HTTPRequest proxyRequest, ProxyHelper proxyHelper) {
    	// Get an Enumeration of all of the header names sent by the client
		Enumeration enumerationOfHeaderNames = httpServletRequest.getHeaderNames();
		
		while(enumerationOfHeaderNames.hasMoreElements()) {
			
			String stringHeaderName = (String) enumerationOfHeaderNames.nextElement();
			if(stringHeaderName.equalsIgnoreCase(ProxyConstant.STRING_CONTENT_LENGTH_HEADER_NAME)){
				continue;
			}
		
			// As per the Java Servlet API 2.5 documentation:
			//		Some headers, such as Accept-Language can be sent by clients
			//		as several headers each with a different value rather than
			//		sending the header as a comma separated list.
			// Thus, we get an Enumeration of the header values sent by the client
			Enumeration enumerationOfHeaderValues = httpServletRequest.getHeaders(stringHeaderName);
			
			while(enumerationOfHeaderValues.hasMoreElements()) {
				String stringHeaderValue = (String) enumerationOfHeaderValues.nextElement();
				

				HTTPHeader header = proxyHelper.replaceProxyWithReal(httpServletRequest, stringHeaderName, stringHeaderValue);
				
				// Set the same header on the proxy request
				proxyRequest.addHeader(header);
			}
		}
    }

	public static void setRealResponseHeaders(HttpServletRequest httpServletRequest, 
										HTTPResponse proxyResponse,
                                        HttpServletResponse httpServletResponse, 
                                        ProxyHelper proxyHelper) {
		StringBuffer headerNames = new StringBuffer();

        // Pass the response code back to the client
		httpServletResponse.setStatus(proxyResponse.getResponseCode());

        // Pass response headers back to the client
		List<HTTPHeader> headerArrayResponse = proxyResponse.getHeaders();
        for(HTTPHeader header : headerArrayResponse) {
       		String value = proxyHelper.replaceRealWithProxy(httpServletRequest, header.getName(), header.getValue());
            httpServletResponse.addHeader(header.getName(), value);
            headerNames.append(header.getName() + " = " + value + "\n");
        }
        
        log.info("logRealResponse: \n " + headerNames);
    }
	
}
