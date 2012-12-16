package org.fabrelab.anyway;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

public class Main {


   public static void main(String[] list) throws Exception {
      Container container = new ProxyServlet();
      
      ContainerServer server = new ContainerServer(container, 20);
      
      startHttpConn(server);
      
      startHttpsConn(server);
   
   }

	private static void startHttpConn(ContainerServer server) throws IOException {
		Connection httpConnection = new SocketConnection(server);

		SocketAddress httpAddress = new InetSocketAddress(80);

		httpConnection.connect(httpAddress);
		System.out.println("Http Connection Started on 80");
	}
	
	private static void startHttpsConn(ContainerServer server) throws IOException, NoSuchAlgorithmException {
		System.setProperty("javax.net.ssl.keyStore", "store/anyway.keystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "password");

		System.setProperty("javax.net.ssl.trustStore", "store/anyway.truststore");
		System.setProperty("javax.net.ssl.trustStorePassword", "password");
		
		Connection httpConnection = new SocketConnection(server);
		SocketAddress httpAddress = new InetSocketAddress(7877);
		SSLContext context = SSLContext.getDefault();
		httpConnection.connect(httpAddress, context);
		System.out.println("Https Connection Started on 7877");
	}
	
}