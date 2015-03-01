package com.tiwo.communication.sockets;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class ClientSocket {

	private static ClientSocket clientSocketInstance;
	private static Client clientInstance;

	private ClientSocket() {
		// set buffer size to avoid buffer overflow
		// check image size
		// if slow, deserialize...
		clientInstance = new Client(131072, 131072);
	}

	public static ClientSocket getInstance() {
		if (clientSocketInstance == null)
			clientSocketInstance = new ClientSocket();
		return clientSocketInstance;
	}
	
	public void startRemote(){
		start(SocketSettings.serverIP);
	}

	public void startLocally(){
		start("localhost");
	}
	
	private void start(String serverIP) {
		
		clientInstance.start();

		try {
			clientInstance.connect(SocketSettings.socketTimeout, serverIP, SocketSettings.serverTCPPort);
		} catch (IOException e) {
			e.printStackTrace();
		}

		clientInstance.addListener(clientListener);
		clientInstance.getKryo().register(byte[].class);
		clientInstance.getKryo().register(RpiExchangePackage.class);
	}
	
	public void stop(){
		clientInstance.stop();
		try {
			clientInstance.dispose();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendPackageToServer(RpiExchangePackage command){
		clientInstance.sendTCP(command);
	}
	
	
	/**
	 * Handle what happens when client receives a message from the server 
	 */

	private Listener clientListener = new Listener() {
		public void received(Connection connection, Object object) {
			if (object instanceof RpiExchangePackage) {
				RpiExchangePackage response = (RpiExchangePackage) object;
				System.out.println(response.getMessage());
				
				// reconstruct the image
				InputStream in = new ByteArrayInputStream(response.img);
				try{
					BufferedImage bi = ImageIO.read(in);
					ImageIO.write(bi, "jpg", new File("java-converted-from-jni.jpg"));
					System.out.println("Success!");
				} catch (IOException ex){
					ex.printStackTrace();
				}
			}
		}
	};

}
