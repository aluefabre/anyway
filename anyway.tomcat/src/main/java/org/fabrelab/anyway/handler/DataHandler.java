package org.fabrelab.anyway.handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.fabrelab.anyway.ProxyServlet;

public class DataHandler {
	
	private static final Logger log = Logger.getLogger(DataHandler.class.getName());
	/**
	 * The maximum size for uploaded files in bytes. Default value is 5MB.
	 */
	public static int intMaxFileUploadSize = 5 * 1024 * 1024;
	
	/**
     * The directory to use to temporarily store uploaded files
     */
    public static final File FILE_UPLOAD_TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));
    
	/**
	 * Sets up the given {@link PostMethod} to send the same multipart POST
	 * data as was sent in the given {@link HttpServletRequest}
	 * @param postMethodProxyRequest The {@link PostMethod} that we are
	 *                                configuring to send a multipart POST request
	 * @param httpServletRequest The {@link HttpServletRequest} that contains
	 *                            the mutlipart POST data to be sent via the {@link PostMethod}
	 * @throws IOException 
	 */
    @SuppressWarnings("unchecked")
	public static void handleMultipartPost(HttpPost postMethodProxyRequest, HttpServletRequest httpServletRequest)
    		throws ServletException, IOException {
    	// Create a factory for disk-based file items
    	DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
    	// Set factory constraints
    	diskFileItemFactory.setSizeThreshold(getMaxFileUploadSize());
    	diskFileItemFactory.setRepository(FILE_UPLOAD_TEMP_DIRECTORY);
    	// Create a new file upload handler
    	ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
    	// Parse the request
    	try {
    		// Get the multipart items as a list
    		List<FileItem> listFileItems = (List<FileItem>) servletFileUpload.parseRequest(httpServletRequest);
    		
    		MultipartEntity multipartRequestEntity = new MultipartEntity();

    		// Iterate the multipart items list
    		for(FileItem fileItemCurrent : listFileItems) {
    			// If the current item is a form field, then create a string part
    			if (fileItemCurrent.isFormField()) {
    				StringBody stringPart = new StringBody(
    						fileItemCurrent.getString()     // The field value
    				);
    				// Add the part to the list
    				multipartRequestEntity.addPart(fileItemCurrent.getFieldName(), stringPart);
    			} else {
    				// The item is a file upload, so we create a FilePart
    				InputStreamBody filePart = new InputStreamBody(
    						fileItemCurrent.getInputStream(), fileItemCurrent.getName()
    				);
    				multipartRequestEntity.addPart(fileItemCurrent.getFieldName(), filePart);
    			}
    		}
    		postMethodProxyRequest.setEntity(multipartRequestEntity);
    		// The current content-type header (received from the client) IS of
    		// type "multipart/form-data", but the content-type header also
    		// contains the chunk boundary string of the chunks. Currently, this
    		// header is using the boundary of the client request, since we
    		// blindly copied all headers from the client request to the proxy
    		// request. However, we are creating a new request with a new chunk
    		// boundary string, so it is necessary that we re-set the
    		// content-type string to reflect the new chunk boundary string
    		postMethodProxyRequest.setHeader(multipartRequestEntity.getContentType());
    	} catch (FileUploadException fileUploadException) {
    		throw new ServletException(fileUploadException);
    	}
    }
    
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
	public static void handleStandardPost(HttpPost postMethodProxyRequest, HttpServletRequest httpServletRequest) throws UnsupportedEncodingException {
		// Get the client POST data as a Map
		Map<String, String[]> mapPostParameters = (Map<String,String[]>) httpServletRequest.getParameterMap();
		// Create a List to hold the NameValuePairs to be passed to the PostMethod
		List<NameValuePair> listNameValuePairs = new ArrayList<NameValuePair>();
		// Iterate the parameter names
		for(String stringParameterName : mapPostParameters.keySet()) {
			// Iterate the values for each parameter name
			String[] stringArrayParameterValues = mapPostParameters.get(stringParameterName);
			for(String stringParamterValue : stringArrayParameterValues) {
				// Create a NameValuePair and store in list
				NameValuePair nameValuePair = new BasicNameValuePair(stringParameterName, stringParamterValue);
				listNameValuePairs.add(nameValuePair);
			}
		}
		String characterEncoding = httpServletRequest.getCharacterEncoding();
		UrlEncodedFormEntity initEntity = new UrlEncodedFormEntity(listNameValuePairs, characterEncoding);
		
		postMethodProxyRequest.setEntity(initEntity);
		
    }
  
	public static void setRealResponseData(HttpResponse proxyResponse, HttpServletResponse httpServletResponse) {
	    try {
			// Send the content to the client
			InputStream inputStreamProxyResponse = proxyResponse.getEntity().getContent();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStreamProxyResponse);
			ServletOutputStream outputStream = httpServletResponse.getOutputStream();
			int intNextByte;
	        while ( ( intNextByte = bufferedInputStream.read() ) != -1 ) {
	        	outputStream.write(intNextByte);
	        }
	        outputStream.flush();
	    	log.info("outputStream.flush();");
			httpServletResponse.flushBuffer();
			log.info("httpServletResponse.flushBuffer();");
		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public static int getMaxFileUploadSize() {
		return intMaxFileUploadSize;
	}
	
	public static void setMaxFileUploadSize(int intMaxFileUploadSizeNew) {
		intMaxFileUploadSize = intMaxFileUploadSizeNew;
	}
	
}
