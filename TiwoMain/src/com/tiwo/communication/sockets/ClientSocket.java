package com.tiwo.communication.sockets;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.tiwo.forms.MainForm;

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
				
				System.out.println(response.img.length);
				
				// reconstruct the image
				ByteArrayInputStream in = new ByteArrayInputStream(response.img);
				try{
					//BufferedImage bi = ImageIO.read(in);
					BufferedImage bi = getGrayscale(320, response.img);
					if(bi != null){
						MainForm.setImage(bi);
						ImageIO.write(bi, "jpg", new File("java-converted-from-jni.jpg"));
						System.out.println("Success!");
					}
					else{
						ImageIO.write(bi, "jpg", new File("java-converted-from-jni.jpg"));
						System.out.println("Fail!");
					}
					
				} catch (IOException ex){
					ex.printStackTrace();
				}
			}
		}
	};
	
	public BufferedImage getGrayscale(int width, byte[] buffer) { 
		System.out.println("Buffer lenght: " + buffer.length);
		int height = buffer.length / width;
	    ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
	    int[] nBits = { 8 };
	    ColorModel cm = new ComponentColorModel(cs, nBits, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
	    SampleModel sm = cm.createCompatibleSampleModel(width, height);
	    DataBufferByte db = new DataBufferByte(buffer, width * height);
	    WritableRaster raster = Raster.createWritableRaster(sm, db, null);
	    BufferedImage result = new BufferedImage(cm, raster, false, null);

	    return result;
	}

}
