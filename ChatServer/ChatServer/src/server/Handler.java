package server;

/** 
Handler.java
@author Jace Maxfield && Sean Groathouse

This class handles each client connection.
 */

import java.io.*;
import java.net.*;


public class Handler implements Runnable {
	private Socket client;
	private String username; // This client's user's username

	public Handler(Socket clientSocket) {
		this.client = clientSocket;
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
			
			// Client has not yet logged in.  In this state, we only allow /Login
			// Break when the user is logged in and ready to do other commands
			// all commands besides /Login are bad syntax here
			while (true) { 
				// Get the client's request
				String request = fromClient.readLine();

				// so Eclipse won't be mad until we get actual code in here
				break;
			}

			// here the user is already logged in - we process /Public /Private /Users and /Close
			// a /Login here is bad syntax
			while (true) {

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
