package chatclient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import protocol.Protocol;
import chatclient.ChatClient;

/**
 * Client.java
 *  
 * Manages all the back-end work of 
 * parsing, error checking, and interacting with the
 * server. It will pass the needed information to the GUI.
 * 
 * @author Jace Maxfield && Sean Groathouse
 *
 */

public class Client {
	public static final int DEFAULT_PORT = 4020;
	public String error;
	public Socket sock;
	public ChatClient chatClient;
	public BufferedReader fromServer = null;
	public BufferedOutputStream toServer = null;
	
	public Client() {	}
	
	public void connect(String server) {
		try {
			sock = new Socket(server, DEFAULT_PORT);
						
			while(true) {
				
			}
			
		}
		catch (IOException ioe) {
			
		}
	}
	
	private void login() {
		
	}
	
	private void run() {
//		try {
//			fromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));
//			toServer = new BufferedOutputStream(new DataOutputStream(client.getOutputStream()));
//		}
//		catch (IOException ioe) {
//			
//		}
		
	}
	
	/**
	 * Boolean helper methods that will help verify
	 * certain setup steps
	 * 
	 * @return
	 */
	public boolean isValidServer() {
		boolean valid = false;
		
		return valid;
	}
	
	public boolean isValidLogin() {
		boolean valid = false;
		
		return valid;
	}
	
	public boolean isValidPrivateUser(String privUser) {
		boolean valid = false;
		
		return valid;
	}

}
