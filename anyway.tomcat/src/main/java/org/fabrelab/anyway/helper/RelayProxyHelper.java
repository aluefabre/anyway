package org.fabrelab.anyway.helper;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.message.BasicHeader;
import org.fabrelab.anyway.constant.ProxyConstant;

public class RelayProxyHelper implements ProxyHelper {
	
	
	/**
	 * Initialize the <code>ProxyServlet</code>
	 * @param servletConfig The Servlet configuration passed in by the servlet conatiner
	 */
	public RelayProxyHelper(ServletConfig servletConfig) {
		
	}
	
    public String getProxyHostAndPort(HttpServletRequest httpServletRequest) {
    	if(httpServletRequest.getServerPort() == 80) {
    		return httpServletRequest.getServerName();
    	} else {
    		return httpServletRequest.getServerName() + ":" + httpServletRequest.getServerPort();
    	}
	}

	// Accessors
    public String getProxyURL(HttpServletRequest httpServletRequest) {
    	if( httpServletRequest.getQueryString()==null){
    		return httpServletRequest.getRequestURL().toString();
    	}
		return httpServletRequest.getRequestURL().toString() + "?" + httpServletRequest.getQueryString();
    }

    /**
     * @param httpServletRequest
     * @param stringLocation
     * @return
     */
    public String replaceRealWithProxy(HttpServletRequest httpServletRequest, String stringHeaderName, String stringHeaderValue) {
        return stringHeaderValue;
    }
    
    public BasicHeader replaceProxyWithReal(HttpServletRequest httpServletRequest, String stringHeaderName, String stringHeaderValue) {
    	if(stringHeaderName.equalsIgnoreCase(ProxyConstant.STRING_PROXY_CONNECTION_HEADER_NAME)){
			stringHeaderName = ProxyConstant.STRING_CONNECTION_HEADER_NAME;
		}

		BasicHeader header = new BasicHeader(stringHeaderName, stringHeaderValue);
        return header;
    }
    
}
