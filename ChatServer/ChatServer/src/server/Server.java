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
	
	public static void main(String[] args) {

		users = new Vector<Tuple<String, OutputStream>>();
		//TODO ArrayBlockingQueue? or other implementation?
		messages = new ArrayBlockingQueue<Tuple<String, String>>();

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
