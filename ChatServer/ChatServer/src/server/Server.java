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

import protocol.Protocol;

public class Server {
	private Protocol protocol;
	public static final int DEFAULT_PORT = 4020; 
	private static final Executor exec = Executors.newCachedThreadPool();

	private static Vector<Tuple<String, OutputStream>> users; // Tuple<user's name:their output stream>
	private static BlockingQueue<Tuple<String, String>> messages; // Tuple<sender's user name:message>

	/** 
	This provides client threads an easy way to handle the /users command.
	*/
	public Vector<Tuple<String, OutputStream>> getUsers() {
		return users;
	}

	/** 
	Client threads should call this method to add the user to the vector.
	The parameters are the user's name and an OutputStream to reach them.
	*/
	public void addUser(String username, OutputStream toClient) {
		users.add(new Tuple<String, OutputStream>(username, toClient));
		// TODO here is where we need to send all users the ]Connected command with this username
		// we may want to invoke this in a separate thread like MessageAdder but could probably
		// do it here as well.
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

		// TODO here is where we need to send all users the ]Disconnected command with this username
		// we may want to invoke this in a separate thread like MessageAdder but could probably
		// do it here as well.
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

		users = new Vector<Tuple<String, OutputStream>>();
		//The LinkedBlockingQueue is all-purpose and does not have a mandatory capacity
		messages = new LinkedBlockingQueue<Tuple<String, String>>();

		// Create the broadcast thread
		Runnable messenger = new Broadcast(users, messages);
		exec.execute(messenger);
	
		ServerSocket sock = null;
		
		try {
			sock = new ServerSocket(DEFAULT_PORT);
			
			// Listen for client connections and service them in a separate thread
			while (true) {
				Runnable task = new Handler(sock.accept(), users, messages);
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
	} // end main
} // end Server
