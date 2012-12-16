package org.fabrelab.anyway.helper;

import org.apache.http.message.BasicHeader;
import org.fabrelab.anyway.constant.ProxyConstant;
import org.fabrelab.anyway.util.SimpleUtil;
import org.simpleframework.http.Request;

public class RelayProxyHelper implements ProxyHelper {
	
	
	/**
	 * Initialize the <code>ProxyServlet</code>
	 * @param servletConfig The Servlet configuration passed in by the servlet conatiner
	 */
	public RelayProxyHelper() {
		
	}
	
    public String getProxyHostAndPort(Request httpServletRequest) {
    	if(SimpleUtil.getAddress(httpServletRequest).getPort() == 80) {
    		return SimpleUtil.getAddress(httpServletRequest).getDomain();
    	} else {
    		return SimpleUtil.getAddress(httpServletRequest).getDomain() + ":" + SimpleUtil.getAddress(httpServletRequest).getPort();
    	}
	}

	// Accessors
    public String getProxyURL(Request httpServletRequest) {
		return SimpleUtil.getAddress(httpServletRequest).toString();
    }

    /**
     * @param httpServletRequest
     * @param stringLocation
     * @return
     */
    public String replaceRealWithProxy(Request httpServletRequest, String stringHeaderName, String stringHeaderValue) {
        return stringHeaderValue;
    }
    
    public BasicHeader replaceProxyWithReal(Request httpServletRequest, String stringHeaderName, String stringHeaderValue) {
    	if(stringHeaderName.equalsIgnoreCase(ProxyConstant.STRING_PROXY_CONNECTION_HEADER_NAME)){
			stringHeaderName = ProxyConstant.STRING_CONNECTION_HEADER_NAME;
		}

		BasicHeader header = new BasicHeader(stringHeaderName, stringHeaderValue);
        return header;
    }
    
}
