/**
 * This class uses KryoNet project: 
 * https://github.com/EsotericSoftware/kryonet
 */

package com.tiwo.communication.sockets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class ServerSocket {

	private static ServerSocket serverSocketInstance;
	private static Server serverInstance;
	private SomeResponse response;
	private List<Connection> currentConnections;

	private ServerSocket() {
		serverInstance = new Server();
		response = new SomeResponse();
		currentConnections = new ArrayList<Connection>();
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
		serverInstance.getKryo().register(SomeRequest.class);
		serverInstance.getKryo().register(SomeResponse.class);
	}
	
	public void sendMessageToClient(String message){
		response.text = message;
		currentConnections.get(0).sendTCP(response);
		//server.send
	}

	/**
	 * Handle what happens when server receives a message from a client
	 */
	private Listener serverListener = new Listener() {
		public void received(Connection connection, Object object) {
			if (object instanceof SomeRequest) {
				
				SomeRequest request = (SomeRequest) object;
				System.out.println(request.text);
				
				if(!currentConnections.contains(connection))
					currentConnections.add(connection);
	
				System.out.println("Ok " + connection.getID() + " connections: " + currentConnections.size());
			}
		}
	};

}
