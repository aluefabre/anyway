package org.fabrelab.anyway.handler;

import java.util.List;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.fabrelab.anyway.constant.ProxyConstant;
import org.fabrelab.anyway.helper.ProxyHelper;
import org.fabrelab.anyway.helper.RelayProxyHelper;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

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
	public static void setProxyRequestHeaders(Request httpServletRequest, HttpRequestBase proxyRequest, ProxyHelper proxyHelper) {
    	// Get an Enumeration of all of the header names sent by the client
    	List<String> enumerationOfHeaderNames = httpServletRequest.getNames();
		
    	for(String stringHeaderName : enumerationOfHeaderNames) {
			if(stringHeaderName.equalsIgnoreCase(ProxyConstant.STRING_CONTENT_LENGTH_HEADER_NAME)){
				continue;
			}
		
			// As per the Java Servlet API 2.5 documentation:
			//		Some headers, such as Accept-Language can be sent by clients
			//		as several headers each with a different value rather than
			//		sending the header as a comma separated list.
			// Thus, we get an Enumeration of the header values sent by the client
			List<String> enumerationOfHeaderValues = httpServletRequest.getValues(stringHeaderName);
			
			for(String stringHeaderValue : enumerationOfHeaderValues) {
				
				BasicHeader header = proxyHelper.replaceProxyWithReal(httpServletRequest, stringHeaderName, stringHeaderValue);
		
				// Set the same header on the proxy request
				proxyRequest.addHeader(header);
			}
		}
    }

	public static void setRealResponseHeaders(Request httpServletRequest, 
										HttpResponse proxyResponse,
                                        Response httpServletResponse, 
                                        ProxyHelper proxyHelper) {

        // Pass the response code back to the client
		httpServletResponse.setCode(proxyResponse.getStatusLine().getStatusCode());

        // Pass response headers back to the client
        Header[] headerArrayResponse = proxyResponse.getAllHeaders();
        for(Header header : headerArrayResponse) {
       		String value = proxyHelper.replaceRealWithProxy(httpServletRequest, header.getName(), header.getValue());
            httpServletResponse.add(header.getName(), value);
        }
        
    }
	
}
