package com.tiwo.forms;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.tiwo.communication.Commands;
import com.tiwo.communication.Commands.MOVEMENT;
import com.tiwo.communication.Serial;
import com.tiwo.communication.sockets.ClientSocket;
import com.tiwo.communication.sockets.ServerSocket;
import com.tiwo.communication.sockets.SomeRequest;
import com.tiwo.communication.sockets.SomeResponse;
import com.tiwo.keyboard.KeyDispatcher;

public class MainForm {

	public JFrame frame;
	private JButton btnSend;
	private JComboBox<Commands.MOVEMENT> cmbCommand;
	private JComboBox<Object> cmbPorts;
	private JButton btnConnect;
	private JButton btnEnableKeyboard;
	private JButton btnTest;
	private JButton btnTest2;
	private boolean listeningKeyEvents = false;
	
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
		
		btnTest = new JButton("Server");
		btnTest.setBounds(10, 95, 133, 23);
		btnTest.addActionListener(btnTestListener);
		
		btnTest2 = new JButton("Client");
		btnTest2.setBounds(10, 130, 133, 23);
		btnTest2.addActionListener(btnTest2Listener);

		
		// add widgets to the frame 
		frame.getContentPane().add(btnSend);
		frame.getContentPane().add(cmbCommand);
		frame.getContentPane().add(cmbPorts);
		frame.getContentPane().add(btnConnect);
		frame.getContentPane().add(btnEnableKeyboard);
		frame.getContentPane().add(btnTest);
		frame.getContentPane().add(btnTest2);
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
	
	ClientSocket client;
	ServerSocket server;
	ActionListener btnTestListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		   //ServerSocket
			server = ServerSocket.getInstance();
			server.start();
	
			
			client = ClientSocket.getInstance();
			client.start();
			client.sendMessageToServer("Poruka...");

		}
	};
	
	ActionListener btnTest2Listener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {

			server.sendMessageToClient("Client? - U mad?");
			
		}
	};
}
