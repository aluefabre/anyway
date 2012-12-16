package org.fabrelab.anyway.util;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.fabrelab.anyway.ProxyServlet;
import org.simpleframework.http.Form;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

public class RealLoggerUtil {
	private static final Logger log = Logger.getLogger(RealLoggerUtil.class.getName());
	
	@SuppressWarnings("unchecked")
	public static void logRealRequest(Request realRequest, String method) throws IOException {
		StringBuffer logString = new StringBuffer();
	
		logString.append("method  " + method + "\n");
		logString.append("getTarget()  " + realRequest.getTarget() + "\n");
		logString.append("getQuery()  " + realRequest.getQuery() + "\n");
	
		List<String> enumerationOfHeaderNames = realRequest.getNames();
	
		for (String stringHeaderName : enumerationOfHeaderNames) {
			// As per the Java Servlet API 2.5 documentation:
			// Some headers, such as Accept-Language can be sent by clients
			// as several headers each with a different value rather than
			// sending the header as a comma separated list.
			// Thus, we get an Enumeration of the header values sent by the
			// client
			List<String> enumerationOfHeaderValues = realRequest.getValues(stringHeaderName);
	
			for (String stringHeaderValue : enumerationOfHeaderValues) {
				logString.append(stringHeaderName + " = " + stringHeaderValue + "\n");
			}
		}
	
		if ("POST".equals(method)) {
			// Get the client POST data as a Map
			Form form = realRequest.getForm();
			// Iterate the parameter names
			for (String postParameterName : form.keySet()) {
				// Iterate the values for each parameter name
				String stringParamterValue = form.get(postParameterName);
				logString.append(postParameterName + " = " + stringParamterValue + "\n");
			}
		}
		if(realRequest.getTarget().contains(".htm")){
			log.info("logRealRequest: \n" + logString);
		}
		
	}
	
	public static void logRealResponse(Request realRequest, Response realResponse) {
		StringBuffer logString = new StringBuffer();
	
		logString.append("getTarget()  " + realRequest.getTarget() + "\n");
		logString.append("getQuery()  " + realRequest.getQuery() + "\n");
		logString.append("getCode()  " + realResponse.getCode()+"\n");
		logString.append("getContentLength()  " + realResponse.getContentLength()+"\n");
		
		for(String name : realResponse.getNames()){
			for(String value : realResponse.getValues(name)){
				logString.append(name + " = " + value + "\n");
			}
		}
		
		if(realRequest.getTarget().contains(".htm")){
			log.info("logRealResponse: \n" + logString);
		}
	}
	
}

