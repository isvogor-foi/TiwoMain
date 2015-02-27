package com.tiwo.communication.sockets;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class ClientSocket {

	private static ClientSocket clientSocketInstance;
	private static Client clientInstance;
	private SomeRequest request;

	private ClientSocket() {
		clientInstance = new Client();
		request = new SomeRequest();
	}

	public static ClientSocket getInstance() {
		if (clientSocketInstance == null)
			clientSocketInstance = new ClientSocket();
		return clientSocketInstance;
	}

	public void start() {
		clientInstance.start();

		try {
			clientInstance.connect(SocketSettings.socketTimeout,
					SocketSettings.serverIP, SocketSettings.serverTCPPort);
		} catch (IOException e) {
			e.printStackTrace();
		}

		clientInstance.addListener(clientListener);

		clientInstance.getKryo().register(SomeRequest.class);
		clientInstance.getKryo().register(SomeResponse.class);
	}
	
	public void sendMessageToServer(String message){
		request.text = message;
		clientInstance.sendTCP(request);
	}
	
	
	/**
	 * Handle what happens when client receives a message from the server 
	 */

	private Listener clientListener = new Listener() {
		public void received(Connection connection, Object object) {
			if (object instanceof SomeResponse) {
				SomeResponse response = (SomeResponse) object;
				System.out.println(response.text);
			}
		}
	};

}
