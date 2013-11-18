package server;

/** 
Handler.java
@author Jace Maxfield && Sean Groathouse

This class handles each client connection.
 */

import java.io.*;
import java.net.*;
import java.util.Date;

public class Handler implements Runnable {
	private Socket client;
	private Vector<Tuple<String, OutputStream>> users; // <username: their output stream>
	private BlockingQueue<Tuple<String, String>> messages; // <sender user name:original message from client>
	private String username; // This client's user's username

	public Handler(Socket clientSocket, Vector<Tuple<String, OutputStream>> initUsers, BlockingQueue<Tuple<String, String>>
	 initMessages) {
		this.client = clientSocket;
		this.users = initUsers;
		this.messages = initMessages;
	}

	/**
	 * This method is invoked in a separate thread.
	 */
	public void run() {
		// we need a reader/writer for the client and a file-reader
		BufferedReader fromClient = null; 
		BufferedOutputStream toClient = null;
		
		try {
			// Get connections to and from the client
			fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			toClient = new BufferedOutputStream(new DataOutputStream(client.getOutputStream()));
			
			// Process commands until the client disconnects
			while (true) { 
				// Get the client's request
				String request = fromClient.readLine();
			}

		}
		catch (IOException ioe) {
			// TODO: handle this?
		}
		finally {
			// Attempt to close all sockets and connections which aren't currently null
			if (fromClient != null) {
				try {
					fromClient.close();
				}
				catch (IOException ioe) {
					System.err.println("The client reader may not have closed correctly...");
				}
			}
			if (toClient != null) {
				try {
					toClient.close();
				}
				catch (IOException ioe) {
					System.err.println("The client writer may not have closed correctly...");
				}
			}
			if (client != null) {
				try {
					client.close();
				}
				catch (IOException ioe) {
					System.err.println("The socket may not have closed correctly...");
				}
			}
		} // end try-catch-finally
	} // end process
} // end Handler
