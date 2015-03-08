package com.tiwo.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.html.ImageView;

import com.tiwo.communication.Commands;
import com.tiwo.communication.Commands.MOVEMENT;
import com.tiwo.communication.Serial;
import com.tiwo.communication.sockets.ClientSocket;
import com.tiwo.communication.sockets.RpiExchangePackage;
import com.tiwo.communication.sockets.ServerSocket;
import com.tiwo.keyboard.KeyDispatcher;

public class MainForm {

	public JFrame frame;
	private JButton btnSend;
	private JComboBox<Commands.MOVEMENT> cmbCommand;
	private JComboBox<Object> cmbPorts;
	private JButton btnConnect;
	private JButton btnEnableKeyboard;
	private JButton btnLocalTest;
	private JButton btnSendToClient;
	private JButton btnOpenFile;
	private static JPanel imageViewJpanel;
	private boolean listeningKeyEvents = false;
	
	// default
	private String openFileLocation = "/home/ivan/Dev/java/temp/cap2g.jpg";
	
	public MainForm() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		// initialize frame
		frame = new JFrame();
		frame.setBounds(100, 100, 568, 405);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);		
		
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(connectButtonAction);
		btnConnect.setBounds(10, 11, 89, 23);
		
		cmbPorts = new JComboBox<Object>(Serial.getInstance().getPorts().toArray());
		cmbPorts.setBounds(109,11,142,23);

		btnSend = new JButton("Send");
		btnSend.addActionListener(sendButtonAction);
		btnSend.setBounds(10, 45, 89, 23);
		btnSend.setEnabled(false);
		
		cmbCommand = new JComboBox<MOVEMENT>();
		cmbCommand.setBounds(109, 46, 142, 20);
		cmbCommand.setModel(new DefaultComboBoxModel<>(Commands.MOVEMENT.values()));
		cmbCommand.setEnabled(false);
		
		btnEnableKeyboard = new JButton("Enable keyboard");
		btnEnableKeyboard.setBounds(263, 45, 133, 22);
		btnEnableKeyboard.addActionListener(keyboardEnableAction);
		btnEnableKeyboard.setEnabled(false);
		
		// button for testing stuff
		
		btnLocalTest = new JButton("Test local server");
		btnLocalTest.setBounds(10, 95, 133, 23);
		btnLocalTest.addActionListener(btnLocalTestListener);
		
		btnSendToClient = new JButton("Send to client");
		btnSendToClient.setBounds(10, 130, 133, 23);
		btnSendToClient.addActionListener(btnSendToClientListener);
		
		imageViewJpanel = new JPanel();
		imageViewJpanel.setLocation(234, 117);
		imageViewJpanel.setSize(320, 240);
		imageViewJpanel.setBackground(Color.black);
		
		btnOpenFile = new JButton("Open file");		
		btnOpenFile.setBounds(10, 164, 89, 23);
		btnOpenFile.addActionListener(btnOpenFileListener);
		
		// add widgets to the frame 
		frame.getContentPane().add(btnSend);
		frame.getContentPane().add(cmbCommand);
		frame.getContentPane().add(cmbPorts);
		frame.getContentPane().add(btnConnect);
		frame.getContentPane().add(btnEnableKeyboard);
		frame.getContentPane().add(btnLocalTest);
		frame.getContentPane().add(btnSendToClient);
		frame.getContentPane().add(imageViewJpanel);
		frame.getContentPane().add(btnOpenFile);
	}
	
	public static void setImage(BufferedImage bi){
		ImageIcon image = new ImageIcon(bi);
		JLabel label = new JLabel();
		label.setIcon(new ImageIcon(bi));
		imageViewJpanel.add(label);
		imageViewJpanel.revalidate();
		imageViewJpanel.repaint();
	}
	
	/**
	 * Action listeners
	 */

	ActionListener connectButtonAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Serial serial = Serial.getInstance();
				serial.connect(serial.getPorts().get(0), 9600);
				
				btnSend.setEnabled(true);
				cmbCommand.setEnabled(true);
				btnEnableKeyboard.setEnabled(true);
				
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	};
	
	ActionListener sendButtonAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Serial serial = Serial.getInstance();
				//serial.connect(serial.getPorts().get(0), 9600);	
				serial.sendMessage(cmbCommand.getSelectedItem().toString());
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	};
	
	ActionListener keyboardEnableAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			listeningKeyEvents = !listeningKeyEvents;
			KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();;
			KeyDispatcher keyDispatcher = null;
			if(listeningKeyEvents){
				keyDispatcher =  new KeyDispatcher();
				manager.addKeyEventDispatcher( keyDispatcher );
				
				btnEnableKeyboard.setText("Disable keyboard");
			}
			else{
				if(manager != null || keyDispatcher != null) {
					manager.removeKeyEventDispatcher(keyDispatcher);
					
					btnEnableKeyboard.setText("Enable keyboard");					
				}
			}
		}
	};
	

	ActionListener btnLocalTestListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// test server locally
			ServerSocket server = ServerSocket.getInstance();
			server.start();
			
			ClientSocket client = ClientSocket.getInstance();
			client.startLocally();
			
			// read test image
			BufferedImage img = null;
			try{
				img = ImageIO.read(new File("/home/ivan/Dev/java/temp/cap1g.jpg"));
			} catch (IOException ex){
				ex.printStackTrace();
			}
			
			byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
			
			RpiExchangePackage packet = new RpiExchangePackage();
			packet.setFpgaCommand("sobel gauss");
			packet.img = pixels;
			packet.setMessage("Message size (" + pixels.length + ")");
			
			client.sendPackageToServer(packet);
			
			// release the resources
			client.stop();
			server.stop();
		}
	};
	
	ActionListener btnSendToClientListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			ClientSocket client = ClientSocket.getInstance();
			client.startRemote();
			
			// get image bytes
			BufferedImage rawImage = null;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] bytes = null;
		
			try {
				rawImage = ImageIO.read(new File(openFileLocation));
				
				ImageIO.write(rawImage, "jpg", baos);
				baos.flush();
				bytes = baos.toByteArray();
				baos.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			RpiExchangePackage packet = new RpiExchangePackage();
			packet.setFpgaCommand("gauss sobel");
			packet.img = bytes;
			packet.setMessage("Message size (" + bytes.length + ")");
			
			client.sendPackageToServer(packet);
			
		}
	};
	
	ActionListener btnOpenFileListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog((Component) e.getSource());
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				System.out.println(fc.getSelectedFile().getAbsolutePath());
				openFileLocation = fc.getSelectedFile().getAbsolutePath();
			}
		}
	};
	
}
