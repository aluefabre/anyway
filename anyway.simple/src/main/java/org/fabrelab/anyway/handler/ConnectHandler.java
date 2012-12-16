package org.fabrelab.anyway.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.fabrelab.anyway.ProxyServlet;
import org.fabrelab.anyway.util.SimpleUtil;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

public class ConnectHandler {
	private static final Logger log = Logger.getLogger(ConnectHandler.class.getName());
	public static void doConnect(Request request, Response response) throws IOException {
		PrintStream outputStream = null;
		Socket sourceSocket = null;
		SSLSocket sourceSSLSocket = null;
		InputStream sourceSSLInputStream = null;
		PrintStream sourceSSLOutputStream = null;
		
		SSLSocket targetSSLSocket = null;
		InputStream targetSSLInputStream = null;
		PrintStream targetSSLOutputStream = null;
		try {
			outputStream = response.getPrintStream();
			response.setMinor(0);
			response.setCode(200);
			response.setText("Connection Established");
			outputStream.println();
			outputStream.flush();
			
			sourceSocket = request.getSocket();

			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

			String hostName = sourceSocket.getInetAddress().getHostName();
			int port = sourceSocket.getPort();
			sourceSSLSocket = (SSLSocket) factory.createSocket(sourceSocket, hostName, port, true);

			sourceSSLSocket.getChannel().configureBlocking(true);
			
			sourceSSLSocket.setUseClientMode(false);
			
			sourceSSLSocket.startHandshake();
			
			sourceSSLInputStream = sourceSSLSocket.getInputStream();
			sourceSSLOutputStream = new PrintStream(sourceSSLSocket.getOutputStream());
			String firstLine = getFirstLine(sourceSSLInputStream);
			String[] parts = firstLine.split(" ");
			String method = parts[0];
			String path = parts[1];
			String version = parts[2];
			if(path.startsWith("/")){
				path = "https://" + SimpleUtil.getAddress(request).getDomain() + path;
			}
			
			targetSSLSocket = (SSLSocket) factory.createSocket("localhost", 7877);
			targetSSLInputStream = targetSSLSocket.getInputStream();
			targetSSLOutputStream = new PrintStream(targetSSLSocket.getOutputStream());
			targetSSLOutputStream.println(method + " " + path + " " + version);
			
			sourceSSLSocket.setSoTimeout(1);
			byte[] buffer = new byte[8192];
			while(true){
				int count = -1;
				try {
					count = sourceSSLInputStream.read(buffer);
				} catch (SocketTimeoutException e) {
					break;
				}
				if(count==-1){
					break;
				}	
				targetSSLOutputStream.write(buffer, 0, count);
			}
			
			while(true){
				int count = targetSSLInputStream.read(buffer);
				if(count==-1){
					break;
				}	
				sourceSSLOutputStream.write(buffer, 0, count);
			}
			sourceSSLOutputStream.flush();
			sourceSSLOutputStream.close();
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			closeConnection(sourceSocket, sourceSSLSocket, targetSSLSocket);
		}
	}

	public static void closeConnection(Socket sourceSocket, SSLSocket sourceSSLSocket, SSLSocket targetSSLSocket) {
		try {
			sourceSocket.close();
			sourceSSLSocket.close();
			targetSSLSocket.close();
		} catch (IOException e) {
			log.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public static String getFirstLine(InputStream sourceSSLInputStream) throws IOException {
		String firstLine = "";
		while(true){
			int chint = sourceSSLInputStream.read();
			if(chint==-1){
				
				throw new IOException("bad request");
			}
			char ch = (char)chint;
			if(ch=='\r'){
				chint = sourceSSLInputStream.read();
				ch = (char)chint;
				if(ch=='\n'){
					break;
				}else{
					sourceSSLInputStream.close();
					throw new IOException("bad request");
				}
			}
			if(ch=='\n'){
				break;
			}
			firstLine += ch;
		}
		return firstLine;
	}

}
