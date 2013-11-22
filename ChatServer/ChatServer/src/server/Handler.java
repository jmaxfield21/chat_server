package server;

/** 
Handler.java
@author Jace Maxfield && Sean Groathouse

This class handles each client connection.
 */

import java.io.*;
import java.net.*;
import protocol.Protocol;


public class Handler implements Runnable {
	private Socket client;
	private String username; // This client's user's username
	private Server chatServer; // to call the Server's various methods

	public Handler(Socket clientSocket, Server theChatServer) {
		this.client = clientSocket;
		this.chatServer = theChatServer;
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
				String request = fromClient.readLine(); // login commands are only 1 line, longer is bad syntax anyway
				String command = "";
				String requestedName = "";

				if (request == null) {
					continue;
				}

				// TODO verify the request is valid syntax - from here assumes syntax is good
				int firstSpace = request.indexOf(" ");
				if (firstSpace >= 0) {
					command = request.substring(0, firstSpace);
					// int termChar = request.indexOf("\r\n");
					requestedName = request.substring(firstSpace + 1, request.length());
				}
				else {
					// TODO send bad syntax
					continue;
				}

				if (command.equalsIgnoreCase(Protocol.CLIENT_LOGIN)) {
					if (Protocol.isValidUsername(requestedName)) {
						if (chatServer.addUser(requestedName, toClient)) {
							// user is now logged in
							String response = Protocol.SERVER_WELCOME + "\r\n";
							toClient.write(response.getBytes());
							toClient.flush();
							this.username = requestedName; // this is their real username now
							break; // go on to the logged-in loop
						}
						else {
							String response = Protocol.SERVER_USER_TAKEN + "\r\n";
							toClient.write(response.getBytes());
							toClient.flush();
						}
					}
					else {
						String response = Protocol.SERVER_BAD_SYNTAX + " Illegal username.\r\n";
						toClient.write(response.getBytes());
						toClient.flush();
					}
				}
				else if (command.equalsIgnoreCase(Protocol.CLIENT_CLOSE)) {
					// TODO close the connection
				}
				else {
					String response = Protocol.SERVER_BAD_SYNTAX + " You are not logged in, and may only send login requests.\r\n";
					toClient.write(response.getBytes());
					toClient.flush();
				}
			} // end while

			// here the user is already logged in - we process /Public /Private /Users and /Close
			// a /Login here is bad syntax
			while (true) {
				// Get the client's request
				String request = fromClient.readLine(); // most commands are only 1 line (get more if needed)

				if (request == null) {
					continue;
				}

				// TODO verify the request is valid syntax - from here assumes syntax is good
				int firstSpace = request.indexOf(" ");
				String command;
				if (firstSpace >= 0) {
					command = request.substring(0, firstSpace);
					// int termChar = request.indexOf("\r\n");
					//String parameter = request.substring(firstSpace + 1, request.length());
				}
				else {
					command = request;
				}

				if (command.equalsIgnoreCase(Protocol.CLIENT_LOGIN)) {
					String response = Protocol.SERVER_BAD_SYNTAX + " You are already logged in, and may not send login requests.\r\n";
					toClient.write(response.getBytes());
					toClient.flush();
				}
				else if (command.equalsIgnoreCase(Protocol.CLIENT_PUBLIC_MSG) || command.equalsIgnoreCase(Protocol.CLIENT_PRIVATE_MSG)) {
					String wholeCommand = request;

					String nextLine; 
					int msgLength = 0;

					while(true) {
						nextLine = fromClient.readLine();
						if (nextLine == null) {
							continue;
						}
						else {
							if ((!(nextLine.contains(Protocol.EOT))) && (msgLength < Protocol.MAX_MESSAGE_LENGTH)) {
								wholeCommand += "\r\n" + vnextLine;
								msgLength += nextLine.length();
							}
							else {
								break;
							}
						}
					}

					if (msgLength >= Protocol.MAX_MESSAGE_LENGTH) {
						// TODO send an error - message was too long, but we'll send the first part
					}

					String lastPart = nextLine.substring(0, nextLine.indexOf(Protocol.EOT));

					wholeCommand += "\r\n" + lastPart; // add the last line with the EOT

					chatServer.addMessage(this.username, wholeCommand);
				}
				else if (command.equalsIgnoreCase(Protocol.CLIENT_USER_REQUEST)) {
					String[] activeUsers = chatServer.getUsers();
					String response = Protocol.SERVER_ACTIVE_USERS + " ";
					if (activeUsers.length == 0) {
						response += "\r\n";
					}
					else {
						int i;
						for (i = 0; i < activeUsers.length-1; i++) {
							response += activeUsers[i] + ",";
						}
						response += activeUsers[i] + "\r\n";
					}

					toClient.write(response.getBytes());
					toClient.flush();
				}
				else if (command.equalsIgnoreCase(Protocol.CLIENT_CLOSE)) {
					// TODO close to the connection
				}
			}

		}
		catch (IOException ioe) {
			// TODO: handle this? Is this when the communication is closed for example?
			// TODO DO we need to have smaller try-catch to attempt to fix smaller issues?
			// or is an issue like this just too severe to reconcile...
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
