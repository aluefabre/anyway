package org.fabrelab.anyway.util;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fabrelab.anyway.ProxyServlet;

public class RealLoggerUtil {
	private static final Logger log = Logger.getLogger(RealLoggerUtil.class.getName());
	

	@SuppressWarnings("unchecked")
	public static void logRealRequest(HttpServletRequest realRequest, String method) {

		StringBuffer logString = new StringBuffer();
		
		logString.append("method  " + method + "\n" );
		logString.append("getRequestURL()  " + realRequest.getRequestURL().toString() + "\n" );
		logString.append("getQueryString()  " + realRequest.getQueryString() + "\n" );
	
		Enumeration enumerationOfHeaderNames = realRequest.getHeaderNames();
		
		while(enumerationOfHeaderNames.hasMoreElements()) {
			
			String stringHeaderName = (String) enumerationOfHeaderNames.nextElement();
			
			// As per the Java Servlet API 2.5 documentation:
			//		Some headers, such as Accept-Language can be sent by clients
			//		as several headers each with a different value rather than
			//		sending the header as a comma separated list.
			// Thus, we get an Enumeration of the header values sent by the client
			Enumeration enumerationOfHeaderValues = realRequest.getHeaders(stringHeaderName);
			
			while(enumerationOfHeaderValues.hasMoreElements()) {
				String stringHeaderValue = (String) enumerationOfHeaderValues.nextElement();
				logString.append(stringHeaderName + " = " + stringHeaderValue + "\n" );
			}
		}
	
		if("POST".equals(method)){
			// Get the client POST data as a Map
			Map<String, String[]> mapPostParameters = (Map<String,String[]>) realRequest.getParameterMap();
			// Iterate the parameter names
			for(String stringParameterName : mapPostParameters.keySet()) {
				// Iterate the values for each parameter name
				String[] stringArrayParameterValues = mapPostParameters.get(stringParameterName);
				for(String stringParamterValue : stringArrayParameterValues) {
					logString.append(stringParameterName + " = " + stringParamterValue + "\n");
				}
			}
		}
		if(realRequest.getRequestURL().toString().contains(".htm")){	
			log.info("logRealRequest: \n " + logString);
		}else{
			log.warning("logRealRequest: \n " + logString);
		}
	}
	
	
	public static void logRealResponse(HttpServletRequest realRequest, HttpServletResponse realResponse, StringBuffer logString ) {
		StringBuffer before = new StringBuffer();
		before.append("getRequestURL()  " + realRequest.getRequestURL().toString() + "\n" );
		before.append("getQueryString()  " + realRequest.getQueryString() + "\n" );
		if(realRequest.getRequestURL().toString().contains(".htm")){
			log.info("logRealResponse: \n" + before + logString);
		}else{
			log.warning("logRealResponse: \n" + before + logString);
		}
	}
	
}

