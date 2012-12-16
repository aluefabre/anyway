package org.fabrelab.anyway;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.urlfetch.HTTPHeader;

public class TunnelOutProxyHelper implements ProxyHelper {

	/**
	 * Initialize the <code>ProxyServlet</code>
	 * @param servletConfig The Servlet configuration passed in by the servlet conatiner
	 */
	public TunnelOutProxyHelper(ServletConfig servletConfig) {
		
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
    	String realServerName = httpServletRequest.getParameter("realServerName");
    	String portParameter = httpServletRequest.getParameter("realServerPort");
		int realServerPort = Integer.parseInt(portParameter);

		String realRequestUrl = "";
		if(realServerPort == 80) {
			realRequestUrl += "http://" + realServerName;
    	}else if(realServerPort == 443){
    		realRequestUrl += "https://" + realServerName;
    	}else{
    		realRequestUrl += "http://" + realServerName + ":" + realServerPort;
    	}
		
		String pathInfo = httpServletRequest.getRequestURI();
		pathInfo = pathInfo.replaceAll("roooo0000t", "");
		
		realRequestUrl = realRequestUrl + pathInfo;

		String queryString = httpServletRequest.getQueryString();
		
		queryString = queryString.replace("&realServerName=" + realServerName, "");
		queryString = queryString.replace("realServerName=" + realServerName, "");
		queryString = queryString.replace("&realServerPort=" + realServerPort, "");
		
		if(queryString.length()>0){
			realRequestUrl = realRequestUrl+"?"+queryString;
		}
		
		return realRequestUrl;
    }

    public String replaceRealWithProxy(HttpServletRequest httpServletRequest, String stringHeaderName, String stringHeaderValue) {
        return stringHeaderValue;
    }
    
    public HTTPHeader replaceProxyWithReal(HttpServletRequest httpServletRequest, String stringHeaderName, String stringHeaderValue){
    	if(stringHeaderName.equalsIgnoreCase(ProxyConstant.STRING_HOST_HEADER_NAME)){
			stringHeaderValue = getRealHostAndPort(httpServletRequest);
		}
    	
		HTTPHeader header = new HTTPHeader(stringHeaderName, stringHeaderValue);
        return header;
    }

	private String getRealHostAndPort(HttpServletRequest httpServletRequest) {
		String realServerName = httpServletRequest.getParameter("realServerName");
    	int realServerPort = Integer.parseInt(httpServletRequest.getParameter("realServerPort"));
    	if(realServerPort == 80) {
    		return realServerName;
    	} else {
    		return realServerName + ":" + realServerPort;
    	}
	}

}
