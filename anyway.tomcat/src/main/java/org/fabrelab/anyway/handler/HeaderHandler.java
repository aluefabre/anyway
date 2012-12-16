package org.fabrelab.anyway.handler;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.fabrelab.anyway.constant.ProxyConstant;
import org.fabrelab.anyway.helper.ProxyHelper;

public class HeaderHandler {
	
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
	public static void setProxyRequestHeaders(HttpServletRequest httpServletRequest, HttpRequestBase proxyRequest, ProxyHelper proxyHelper) {
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
				
				
				BasicHeader header = proxyHelper.replaceProxyWithReal(httpServletRequest, stringHeaderName, stringHeaderValue);
		
				// Set the same header on the proxy request
				proxyRequest.addHeader(header);
			}
		}
    }

	public static StringBuffer setRealResponseHeaders(HttpServletRequest httpServletRequest, 
										HttpResponse proxyResponse,
                                        HttpServletResponse httpServletResponse, 
                                        ProxyHelper proxyHelper) {
		StringBuffer logString = new StringBuffer();
        // Pass the response code back to the client
		int statusCode = proxyResponse.getStatusLine().getStatusCode();
		httpServletResponse.setStatus(statusCode);
		logString.append("StatusCode: " + statusCode + "\n");
        // Pass response headers back to the client
        Header[] headerArrayResponse = proxyResponse.getAllHeaders();
        for(Header header : headerArrayResponse) {
       		String headerName = header.getName();
        	if(headerName.equalsIgnoreCase(ProxyConstant.STRING_TRANSFER_ENCODING_HEADER_NAME)){
				continue;
			}
			String value = proxyHelper.replaceRealWithProxy(httpServletRequest, headerName, header.getValue());
            httpServletResponse.addHeader(headerName, value);
        	logString.append(headerName + " = " + value + "\n");
        }
        
        return logString;
    }
	
}
