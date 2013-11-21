package server;

/**
Server.java
@author Jace Maxfield && Sean Groathouse

This file runs the Chat Server itself, contains the data structures storing
the user information (with a way to talk to each user), and the queue for 
storing messages.  Upon initialization, the server creates the data structures
and establishes one copy of the Broadcast thread to handle the sending of 
messages.
 */

import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.Vector;


public class Server {
	public static final int DEFAULT_PORT = 4020; 
	private static final Executor exec = Executors.newCachedThreadPool();

	private static Vector<Tuple<String, OutputStream>> users; // Tuple<user's name:their output stream>
	private static BlockingQueue<Tuple<String, String>> messages; // Tuple<sender's user name:message>

	public Server() {
		initialize();
		getUsers();
		listen();
	} // end constructor

	private void initialize() {
		users = new Vector<Tuple<String, OutputStream>>();
		//The LinkedBlockingQueue is all-purpose and does not have a mandatory capacity
		messages = new LinkedBlockingQueue<Tuple<String, String>>();

		// Create the broadcast thread
		Runnable messenger = new Broadcast(users, messages);
		exec.execute(messenger);		
	}

	private void listen() {
		ServerSocket sock = null;

		try {
			sock = new ServerSocket(DEFAULT_PORT);
			
			// Listen for client connections and service them in a separate thread
			while (true) {
				Runnable task = new Handler(sock.accept(), this);
				exec.execute(task);
			}
		}
		catch (IOException ioe) {
			System.err.println("An IO error has occurred.");
		}
		catch (SecurityException se) {
			System.err.println("A security error has occurred.");
		}
		catch (IllegalArgumentException iae) {
			System.err.println("Invalid port number!  The port number needs to be between 0 and 65535.");
		}
		finally {
			if (sock != null) {
				try {
					sock.close();
				}
				catch (IOException ioe) {
					System.err.println("The socket may not have closed correctly...");
				}
			}
		} // end try-catch-finally
	}


	/** 
	This provides client threads an easy way to handle the /users command.
	*/
	public String[] getUsers() {
		Object[] userArray = new Object[users.size()*2 + 5]; // just to be safe
		users.copyInto(userArray);
		Tuple<String, OutputStream> entry;

		String[] activeUsers = new String[userArray.length];

		int count = 0;

		for (int i = 0; i < activeUsers.length; i++) {
			if (userArray[i] != null) {
				entry = (Tuple<String, OutputStream>) userArray[i];
				activeUsers[i] = entry.x;
				count++;
			}
		}

		String[] rv = new String[count];

		for (int i = 0; i < count; i++) {
			rv[i] = activeUsers[i];
		}

		return rv;
	}

	/** 
	Client threads should call this method to add the user to the vector.
	The parameters are the user's name and an OutputStream to reach them.
	Returns true if the user was added, and false if the user could not be
	added as the username was taken.
	*/
	public boolean addUser(String username, OutputStream toClient) {
		if (users.contains(new Tuple<String, OutputStream>(username, toClient))) {
			return false;
		}

		users.add(new Tuple<String, OutputStream>(username, toClient));

		Object[] userArray = new Object[users.size()*2 + 5]; // just to be safe
		users.copyInto(userArray);
		Tuple<String, OutputStream> entry;

		String response = "]Connected ";
		response += username;
		response += "\r\n";

		for (int i = 0; i < userArray.length; i++) {
			if (userArray[i] != null) {
				entry = (Tuple<String, OutputStream>) userArray[i];
				try {
					// entry.y yields an OutputStream that can talk to that client
					entry.y.write(response.getBytes());
					entry.y.flush();
				} 
				catch (IOException ioe) { 
					// We can't send a message to this user, but the others should still get it
				}
			}
		}
		
		return true;
	}

	/** 
	Client threads should call this method to remove their user from the 
	vector.  The parameters should be the same as when the user was added.
	*/
	public void removeUser(String username, OutputStream toClient) {
		// Note two tuples are defined to be equal (in the equals method there)
		// if they both are the same class and if their x values are equal
		// in this case the String usernames.
		users.remove(new Tuple<String, OutputStream>(username, toClient));

		Object[] userArray = new Object[users.size()*2 + 5]; // just to be safe
		users.copyInto(userArray);
		Tuple<String, OutputStream> entry;

		String response = "]Disconnected ";
		response += username;
		response += "\r\n";

		for (int i = 0; i < userArray.length; i++) {
			if (userArray[i] != null) {
				entry = (Tuple<String, OutputStream>) userArray[i];
				try {
					// entry.y yields an OutputStream that can talk to that client
					entry.y.write(response.getBytes());
					entry.y.flush();
				} 
				catch (IOException ioe) { 
					// We can't send a message to this user, but the others should still get it
				}
			}
		}
	}

	/** 
	Client threads should use this method to add a message as this operation 
	can stall.  The server creates a separate thread (a MessageAdder) for this
	operation to run and stall as needed.
	*/
	public void addMessage(String senderUserName, String entireCommand) {
		Runnable addMsgThread = new MessageAdder(messages, senderUserName, entireCommand);
		exec.execute(addMsgThread);
	}
	
	public static void main(String[] args) {
		Server chatServer = new Server();		
	} // end main
} // end Server
