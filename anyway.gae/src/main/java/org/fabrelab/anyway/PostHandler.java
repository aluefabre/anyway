package org.fabrelab.anyway;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.urlfetch.HTTPRequest;


public class PostHandler {
	
	/**
	 * The maximum size for uploaded files in bytes. Default value is 5MB.
	 */
	public static int intMaxFileUploadSize = 5 * 1024 * 1024;

	/**
	 * Sets up the given {@link PostMethod} to send the same standard POST
	 * data as was sent in the given {@link HttpServletRequest}
	 * @param postMethodProxyRequest The {@link PostMethod} that we are
	 *                                configuring to send a standard POST request
	 * @param httpServletRequest The {@link HttpServletRequest} that contains
	 *                            the POST data to be sent via the {@link PostMethod}
	 * @throws UnsupportedEncodingException 
	 */    
    @SuppressWarnings("unchecked")
	public static void handleStandardPost(HTTPRequest postMethodProxyRequest, HttpServletRequest httpServletRequest) throws UnsupportedEncodingException {
		// Get the client POST data as a Map
		Map<String, String[]> mapPostParameters = (Map<String,String[]>) httpServletRequest.getParameterMap();
		

		String characterEncoding = httpServletRequest.getCharacterEncoding();
		if(characterEncoding==null){
			characterEncoding = "utf-8";
		}
		String payload = "";
		
		boolean first = true;
		// Iterate the parameter names
		for(String stringParameterName : mapPostParameters.keySet()) {
			if("realServerName".equals(stringParameterName)){
				continue;
			}
			if("realServerPort".equals(stringParameterName)){
				continue;
			}
			// Iterate the values for each parameter name
			String[] stringArrayParameterValues = mapPostParameters.get(stringParameterName);
			for(String stringParamterValue : stringArrayParameterValues) {
				if(first){
					payload += stringParameterName + "=" + URLEncoder.encode(stringParamterValue, characterEncoding);
					first = false;
				}else{
					payload += "&" + stringParameterName + "=" + URLEncoder.encode(stringParamterValue, characterEncoding);
				}
			}
		}
		postMethodProxyRequest.setPayload(payload.getBytes());
    }
    

	public static int getMaxFileUploadSize() {
		return intMaxFileUploadSize;
	}
	
	public static void setMaxFileUploadSize(int intMaxFileUploadSizeNew) {
		intMaxFileUploadSize = intMaxFileUploadSizeNew;
	}
}
