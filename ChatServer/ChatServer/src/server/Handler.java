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
	private BufferedReader fromClient;
	private BufferedWriter toClient;

	public Handler(Socket clientSocket, Server theChatServer) {
		this.client = clientSocket;
		this.chatServer = theChatServer;
		this.username = "";
		fromClient = null;
		toClient = null;
	}

	/**
	 * This method is invoked in a separate thread.
	 */
	public void run() {
		try {
			// Get connections to and from the client
			fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			toClient = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

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

				// Check if the first line is malformed, return an error if it is
				if (!(Protocol.isProperFirstLine(request + "\r\n"))) {
					String response = Protocol.SERVER_BAD_SYNTAX + " Malformed request.\r\n";
					toClient.write(response);
					toClient.flush();
					continue;
				}

				// there should be a space here since only login requests are allowed
				int firstSpace = request.indexOf(" ");
				if (firstSpace >= 0) {
					command = request.substring(0, firstSpace);
					// int termChar = request.indexOf("\r\n");
					requestedName = request.substring(firstSpace + 1, request.length());
				}
				else {
					String response = Protocol.SERVER_BAD_SYNTAX + " Malformed or illegal request.\r\n";
					toClient.write(response);
					toClient.flush();
					continue;
				}

				// process only login requests
				if (command.equalsIgnoreCase(Protocol.CLIENT_LOGIN)) {
					if (Protocol.isValidUsername(requestedName)) {
						if (chatServer.addUser(requestedName, toClient)) {
							// user is now logged in
							String response = Protocol.SERVER_WELCOME + "\r\n";
							toClient.write(response);
							toClient.flush();
							this.username = requestedName; // this is their real username now
							break; // go on to the logged-in loop
						}
						else {
							String response = Protocol.SERVER_USER_TAKEN + "\r\n";
							toClient.write(response);
							toClient.flush();
						}
					}
					else {
						String response = Protocol.SERVER_BAD_SYNTAX + " Illegal username.\r\n";
						toClient.write(response);
						toClient.flush();
					}
				}
				else if (command.equalsIgnoreCase(Protocol.CLIENT_CLOSE)) {
					// the user has not connected to the server yet, so basically just close this thread
					throw new IOException(); // go to the catch 
				}
				else {
					String response = Protocol.SERVER_BAD_SYNTAX + " You are not logged in, and may only send login requests.\r\n";
					toClient.write(response);
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

				// check if the first line is malformed and return an error if so
				if (!(Protocol.isProperFirstLine(request + "\r\n"))) {
					String response = Protocol.SERVER_BAD_SYNTAX + " Malformed request.\r\n";
					toClient.write(response);
					toClient.flush();
					continue;
				}

				// there may or may not be a space in the first line depending on the particular command
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

				// logins are syntax errors here
				if (command.equalsIgnoreCase(Protocol.CLIENT_LOGIN)) {
					String response = Protocol.SERVER_BAD_SYNTAX + " You are already logged in, and may not send login requests.\r\n";
					toClient.write(response);
					toClient.flush();
				}
				else if (command.equalsIgnoreCase(Protocol.CLIENT_PUBLIC_MSG) || command.equalsIgnoreCase(Protocol.CLIENT_PRIVATE_MSG)) {
					String wholeCommand = request + "\r\n";

					String msgBody = ""; 
					int msgLength = 0;

					// Get the whole message since this is a message
					char serv;
					while ((serv = (char)fromClient.read()) != Protocol.EOT_CHAR && (msgLength < Protocol.MAX_MESSAGE_LENGTH)) {
						msgBody += serv;
						msgLength++;
					}
										
					wholeCommand += msgBody;
					wholeCommand += Protocol.EOT; // add the last line with the EOT


					if (msgLength >= Protocol.MAX_MESSAGE_LENGTH) 
					{
						String response = Protocol.SERVER_BAD_SYNTAX + " Message too long.\r\n";
						toClient.write(response);
						toClient.flush();
						continue;
					}

					if (!(Protocol.isProperCommand(wholeCommand))) {
						String response = Protocol.SERVER_BAD_SYNTAX + " Malformed Request.\r\n";
						toClient.write(response);
						toClient.flush();
						continue;
					}

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

					toClient.write(response);
					toClient.flush();
				}
				else if (command.equalsIgnoreCase(Protocol.CLIENT_CLOSE)) {
					throw new IOException();
				} // end else if
			} // end while
		} //end try
		catch (IOException ioe) {
			if (this.username.equals("")) {
				// the user isn't logged in, no need to contact the server
			}
			else {
				chatServer.removeUser(username, toClient);
			}
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
