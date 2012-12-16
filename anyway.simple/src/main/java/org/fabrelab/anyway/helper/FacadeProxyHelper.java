package org.fabrelab.anyway.helper;


import org.apache.http.message.BasicHeader;
import org.fabrelab.anyway.constant.ProxyConstant;
import org.fabrelab.anyway.util.SimpleUtil;
import org.simpleframework.http.Address;
import org.simpleframework.http.Request;

public class FacadeProxyHelper implements ProxyHelper {

	// Proxy host params
    /**
     * The host to which we are proxying requests
     */
	private  String stringProxyHost;
	/**
	 * The port on the proxy host to wihch we are proxying requests. Default value is 80.
	 */
	private  int intProxyPort = 80;
	/**
	 * The (optional) path on the proxy host to which we are proxying requests. Default value is "".
	 */
	private  String stringProxyPath = "";
	
	/**
	 * Initialize the <code>ProxyServlet</code>
	 * @param servletConfig The Servlet configuration passed in by the servlet conatiner
	 */
	public FacadeProxyHelper(String stringProxyHostNew, String stringProxyPortNew, String stringProxyPathNew) {
		// Get the proxy host
		if(stringProxyHostNew == null || stringProxyHostNew.length() == 0) { 
			throw new IllegalArgumentException("Proxy host not set, please set init-param 'proxyHost' in web.xml");
		}
		setProxyHost(stringProxyHostNew);
		// Get the proxy port if specified
		if(stringProxyPortNew != null && stringProxyPortNew.length() > 0) {
			setProxyPort(Integer.parseInt(stringProxyPortNew));
		}
		// Get the proxy path if specified
		if(stringProxyPathNew != null && stringProxyPathNew.length() > 0) {
			setProxyPath(stringProxyPathNew);
		}
	}
	
    public String getProxyHostAndPort(Request httpServletRequest) {
    	if(getProxyPort() == 80) {
    		return getProxyHost();
    	} else {
    		return getProxyHost() + ":" + getProxyPort();
    	}
	}
  

	// Accessors
    public String getProxyURL(Request httpServletRequest) {
		// Set the protocol to HTTP
		String stringProxyURL = "http://" + getProxyHostAndPort(httpServletRequest);
		// Check if we are proxying to a path other that the document root
		if(!getProxyPath().equalsIgnoreCase("")){
			stringProxyURL += getProxyPath();
		}
		// Handle the path given to the servlet
		stringProxyURL += httpServletRequest.getPath();
		// Handle the query string
		if(httpServletRequest.getQuery() != null) {
			stringProxyURL += "?" + httpServletRequest.getQuery();
		}
		return stringProxyURL;
    }
    
    /**
     * @param httpServletRequest
     * @param stringLocation
     * @return
     */
    public String replaceRealWithProxy(Request httpServletRequest, String stringHeaderName, String stringHeaderValue) {
        // Modify the redirect to go to this proxy servlet rather that the proxied host
        Address address = SimpleUtil.getAddress(httpServletRequest);
		String stringMyHostName = address.getDomain();
        if(address.getPort() != 80) {
        	stringMyHostName += ":" + address.getPort();
        }
        stringHeaderValue = stringHeaderValue.replace(getProxyHostAndPort(httpServletRequest) + getProxyPath(), stringMyHostName);
        return stringHeaderValue;
    }
    
    public BasicHeader replaceProxyWithReal(Request httpServletRequest, String stringHeaderName, String stringHeaderValue) {
    	// In case the proxy host is running multiple virtual servers,
		// rewrite the Host header to ensure that we get content from
		// the correct virtual server
		if(stringHeaderName.equalsIgnoreCase(ProxyConstant.STRING_HOST_HEADER_NAME)){
			stringHeaderValue = getProxyHostAndPort(httpServletRequest);
		}
    	
        // Modify the redirect to go to this proxy servlet rather that the proxied host
        Address address = SimpleUtil.getAddress(httpServletRequest);
		String stringMyHostName = address.getDomain();
        if(address.getPort() != 80) {
            stringMyHostName += ":" + address.getPort();
        }
        stringHeaderValue = stringHeaderValue.replace(stringMyHostName, getProxyHostAndPort(httpServletRequest) + getProxyPath());
        
		
		if(stringHeaderName.equalsIgnoreCase(ProxyConstant.STRING_PROXY_CONNECTION_HEADER_NAME)){
			stringHeaderName = ProxyConstant.STRING_CONNECTION_HEADER_NAME;
		}

		BasicHeader header = new BasicHeader(stringHeaderName, stringHeaderValue);
        return header;
    }


	private void setProxyPath(String stringProxyPathNew) {
 		this.stringProxyPath = stringProxyPathNew;
 	}

 	private void setProxyPort(int parseInt) {
 		this.intProxyPort = parseInt;
 	}

 	private void setProxyHost(String stringProxyHostNew) {
 		this.stringProxyHost = stringProxyHostNew;
 	}
 	  
	private int getProxyPort() {
		return intProxyPort;
	}

    private String getProxyPath() {
		return stringProxyPath;
	}

	private String getProxyHost() {
		return stringProxyHost;
	}

}
