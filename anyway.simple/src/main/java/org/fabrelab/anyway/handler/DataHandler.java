package org.fabrelab.anyway.handler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.simpleframework.http.Form;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

public class DataHandler {
	/**
	 * Sets up the given {@link PostMethod} to send the same standard POST
	 * data as was sent in the given {@link Request}
	 * @param postMethodProxyRequest The {@link PostMethod} that we are
	 *                                configuring to send a standard POST request
	 * @param httpServletRequest The {@link Request} that contains
	 *                            the POST data to be sent via the {@link PostMethod}
	 * @throws IOException 
	 */    
    @SuppressWarnings("unchecked")
	public static void handleStandardPost(HttpPost postMethodProxyRequest, Request httpServletRequest) throws IOException {

    	// Create a List to hold the NameValuePairs to be passed to the PostMethod
		List<NameValuePair> listNameValuePairs = new ArrayList<NameValuePair>();
		// Get the client POST data as a Map
		Form form = httpServletRequest.getForm();
		// Iterate the parameter names
		for(String postParameterName : form.keySet()) {
			// Iterate the values for each parameter name
			String stringParamterValue = form.get(postParameterName);
			NameValuePair nameValuePair = new BasicNameValuePair(postParameterName, stringParamterValue);
			listNameValuePairs.add(nameValuePair);
		}
		
		UrlEncodedFormEntity initEntity = new UrlEncodedFormEntity(listNameValuePairs, HTTP.DEFAULT_CONTENT_CHARSET);
		
		postMethodProxyRequest.setEntity(initEntity);
		
    }
	

	public static void setRealResponseData(HttpResponse proxyResponse, Response realResponse) throws IOException {
		OutputStream outputStream = realResponse.getOutputStream();
		// Send the content to the client
		HttpEntity entity = proxyResponse.getEntity();
		if(entity!=null){
			InputStream inputStreamProxyResponse = entity.getContent();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStreamProxyResponse);
			int intNextByte;
			while ((intNextByte = bufferedInputStream.read()) != -1) {
				outputStream.write(intNextByte);
			}
		}
		outputStream.flush();
		outputStream.close();
	}
}
