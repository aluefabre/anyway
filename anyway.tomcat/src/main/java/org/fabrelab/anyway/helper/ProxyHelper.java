package org.fabrelab.anyway.helper;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.message.BasicHeader;

public interface ProxyHelper {

    public String getProxyURL(HttpServletRequest httpServletRequest);

    public String replaceRealWithProxy(HttpServletRequest httpServletRequest, String stringHeaderName, String stringHeaderValue);
    
    public BasicHeader replaceProxyWithReal(HttpServletRequest httpServletRequest, String stringHeaderName, String stringHeaderValue);

}
