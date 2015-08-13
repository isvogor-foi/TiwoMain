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

import com.tiwo.communication.Commands;
import com.tiwo.communication.Commands.MOVEMENT;
import com.tiwo.communication.Serial;
import com.tiwo.communication.sockets.ClientSocket;
import com.tiwo.communication.sockets.RpiExchangePackage;
import com.tiwo.communication.sockets.ServerSocket;
import com.tiwo.keyboard.KeyDispatcher;
import com.tiwo.main.Measurement;
import com.tiwo.mission.MissionManagerMain;

import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import java.awt.SystemColor;

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
	private JButton btnTestScript; 
	
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
		frame.setBounds(100, 100, 814, 527);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);		
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 0, 800, 491);
		frame.getContentPane().add(tabbedPane);
		
		// Actuator panel
		
		actuatorPanel = new JPanel();
		actuatorPanel.setLayout(null);
		
		btnConnect = new JButton("Connect");
		btnConnect.setBounds(48, 33, 92, 25);
		btnConnect.addActionListener(connectButtonAction);
		
		cmbPorts = new JComboBox<Object>(Serial.getInstance().getPorts().toArray());
		cmbPorts.setBounds(170, 33, 118, 24);
		
		btnEnableKeyboard = new JButton("Enable keyboard");
		btnEnableKeyboard.setBounds(48, 154, 153, 25);
		btnEnableKeyboard.addActionListener(keyboardEnableAction);
		btnEnableKeyboard.setEnabled(false);
	
		cmbCommand = new JComboBox<MOVEMENT>();
		cmbCommand.setBounds(48, 96, 106, 24);
		cmbCommand.setModel(new DefaultComboBoxModel<>(Commands.MOVEMENT.values()));
		cmbCommand.setEnabled(false);
		
		btnSend = new JButton("Send");
		btnSend.setBounds(170, 97, 89, 23);
		btnSend.addActionListener(sendButtonAction);
		btnSend.setEnabled(false);
		
		JLabel lblConnectToArtuino = new JLabel("Connect to Arduino");
		lblConnectToArtuino.setBounds(12, 12, 162, 15);
		actuatorPanel.add(lblConnectToArtuino);
		
		JLabel lblSendCommands = new JLabel("Send commands");
		lblSendCommands.setBounds(12, 69, 177, 15);
		actuatorPanel.add(lblSendCommands);
		
		JLabel lblEnableKeyboardCommands = new JLabel("Enable keyboard commands");
		lblEnableKeyboardCommands.setBounds(12, 127, 209, 15);
		actuatorPanel.add(lblEnableKeyboardCommands);
		
		actuatorPanel.add(cmbCommand);
		actuatorPanel.add(cmbPorts);
		actuatorPanel.add(btnConnect);
		actuatorPanel.add(btnEnableKeyboard);
		actuatorPanel.add(btnSend);

		tabbedPane.addTab("Actuator control", null, actuatorPanel, null);
		
		// FPGA panel
		
		fpgaPanel = new JPanel();		
		tabbedPane.addTab("FPGA communication", null, fpgaPanel, null);
		fpgaPanel.setLayout(null);
		
		imageViewJpanel = new JPanel();
		imageViewJpanel.setBounds(218, 33, 320, 240);
		imageViewJpanel.setBackground(Color.black);
		
		btnOpenFile = new JButton("Open local image ...");
		btnOpenFile.setBounds(12, 45, 163, 25);
		
		btnSendToClient = new JButton("Start with FPGA");
		btnSendToClient.setBackground(Color.GREEN);
		btnSendToClient.setBounds(12, 139, 163, 25);
		
		// button for testing stuff
		
		btnLocalTest = new JButton("Test local server");
		btnLocalTest.setBounds(12, 93, 163, 25);
		
		fpgaPanel.add(btnOpenFile);
		fpgaPanel.add(imageViewJpanel);
		fpgaPanel.add(btnSendToClient);
		fpgaPanel.add(btnLocalTest);
		

		btnLocalTest.addActionListener(btnLocalTestListener);
		btnSendToClient.addActionListener(btnSendToClientListener);
		btnOpenFile.addActionListener(btnOpenFileListener);
		
		JPanel missionPanel = new JPanel();
		tabbedPane.addTab("Mission execution", null, missionPanel, null);
		missionPanel.setLayout(null);
		
		// Mission panel
		
		btnTestScript = new JButton("Start with FPGA");
		missionPanel.add(btnTestScript);
		
		btnTestScript.setBackground(Color.GREEN);
		btnTestScript.setBounds(23, 31, 163, 25);
		btnTestScript.addActionListener(btnTestScriptClickListener);
	}
	
	public static void setImage(BufferedImage bi){
		ImageIcon image = new ImageIcon(bi);
		JLabel label = new JLabel();
		label.setIcon(new ImageIcon(bi));
		imageViewJpanel.add(label);
		imageViewJpanel.revalidate();
		imageViewJpanel.repaint();
	}
	
	public BufferedImage loadImage(String filename){
		// read test image
		BufferedImage img = null;
		try{
			img = ImageIO.read(new File(filename));
		} catch (IOException ex){
			ex.printStackTrace();
		}
		return img;
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
	
	public ActionListener sendButtonAction = new ActionListener() {
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
	
	public ActionListener keyboardEnableAction = new ActionListener() {
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
	

	public ActionListener btnLocalTestListener = new ActionListener() {
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
			//packet.setFpgaCommand("sobel gauss");
			//packet.setFpgaCommand("gauss");
			packet.img = pixels;
			packet.setMessage("Message size (" + pixels.length + ")");
			
			client.sendPackageToServer(packet);
			
			// release the resources
			client.stop();
			server.stop();
		}
	};
	
	public ActionListener btnSendToClientListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			ClientSocket client = ClientSocket.getInstance();
			client.startRemote();
			
			// get image bytes
			BufferedImage rawImage = null;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] bytes = null;
		
			try {
				///home/ivan/Dev/C/workspace/cimgs/beagle_qvga.jpg
				//openFileLocation
				rawImage = ImageIO.read(new File(openFileLocation));
				
				ImageIO.write(rawImage, "jpg", baos);
				baos.flush();
				bytes = baos.toByteArray();
				baos.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			RpiExchangePackage packet = new RpiExchangePackage();
			packet.setFpgaCommand("sobel gauss erode dilate");
			//packet.setFpgaCommand("gauss sobel erode hyst");
			packet.img = bytes;
			packet.setMessage("Message size (" + bytes.length + ")");
			//client.sendPackageToServer(packet);

			
			for( int i = 0; i <= 550; i++){
				Measurement.startMeasuring();
				System.out.println("At: " + i);
				client.sendPackageToServer(packet);
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
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
	
	public ActionListener btnTestScriptClickListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			MissionManagerMain mm = new MissionManagerMain("/home/ivan/git/MissionManager/MissionManager/src/com/tiwo/missions/demo.mis");
		}
		
	};
	
	private JTabbedPane tabbedPane;
	private JPanel actuatorPanel;
	private JPanel fpgaPanel;
}
