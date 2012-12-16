package org.fabrelab.anyway.helper;


import org.apache.http.message.BasicHeader;
import org.simpleframework.http.Request;

public interface ProxyHelper {

    public String getProxyURL(Request httpServletRequest);

    public String replaceRealWithProxy(Request httpServletRequest, String stringHeaderName, String stringHeaderValue);
    
    public BasicHeader replaceProxyWithReal(Request httpServletRequest, String stringHeaderName, String stringHeaderValue);

}
