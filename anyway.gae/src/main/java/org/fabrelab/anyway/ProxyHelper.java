package org.fabrelab.anyway;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.urlfetch.HTTPHeader;

public interface ProxyHelper {

    public String getProxyURL(HttpServletRequest httpServletRequest);

    public String replaceRealWithProxy(HttpServletRequest httpServletRequest, String stringHeaderName, String stringHeaderValue);
    
    public HTTPHeader replaceProxyWithReal(HttpServletRequest httpServletRequest, String stringHeaderName, String stringHeaderValue);

}
