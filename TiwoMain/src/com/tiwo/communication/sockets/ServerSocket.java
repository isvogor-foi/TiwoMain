/**
 * This class uses KryoNet project: 
 * https://github.com/EsotericSoftware/kryonet
 */

package com.tiwo.communication.sockets;

import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class ServerSocket {

	private static ServerSocket serverSocketInstance;
	private static Server serverInstance;

	private ServerSocket() {
		// set buffer sizes - important, without you get buffer overflow
		//https://code.google.com/p/kryonet/source/browse/trunk/kryonet/test/com/esotericsoftware/kryonet/InputStreamSenderTest.java
		serverInstance = new Server(131072, 131072);
	}

	public static ServerSocket getInstance() {
		if (serverSocketInstance == null)
			serverSocketInstance = new ServerSocket();
		return serverSocketInstance;
	}

	public void start() {
		serverInstance.start();
		try {
			serverInstance.bind(SocketSettings.serverTCPPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		serverInstance.addListener(serverListener);
		// register communication classes
		serverInstance.getKryo().register(byte[].class);
		serverInstance.getKryo().register(RpiExchangePackage.class);
	}
	
	public void stop(){
		serverInstance.stop();
		try {
			serverInstance.dispose();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessageToClient(String message){
		RpiExchangePackage response = new RpiExchangePackage();
		response.setMessage("Works fine...");
		serverInstance.getConnections()[0].sendTCP(response);
	}

	/**
	 * Handle what happens when server receives a message from a client
	 */
	private Listener serverListener = new Listener() {
		public void received(Connection connection, Object object) {
			if (object instanceof RpiExchangePackage) {
				RpiExchangePackage request = (RpiExchangePackage) object;
				System.out.println("Recieved: ");
				System.out.println(request.getMessage());
				System.out.println(request.getFpgaCommand());
			}
		}
	};

}
