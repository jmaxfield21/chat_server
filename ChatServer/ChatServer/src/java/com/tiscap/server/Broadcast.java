package com.tiscap.server;

/**
Broadcast.java
@author Jace Maxfield && Sean Groathouse

This is the broadcast thread which pulls from the message queue in the
server.  The server will initiate a single instance of this class
upon its initialization, and will pass a copy of the users:BufferedWriters
vector as well as the queue of messages.

Once it pulls a message from the queue, it parses the contents
to determine if the message is public or private and get the message
body.  Then using the vector of users:BufferedWriters on the server, it 
creates the appropriate responses to the recipients and sends the 
message to their respective BufferedWriters.
 */

import java.util.concurrent.*;
import java.util.Vector;
import java.io.BufferedWriter;
import java.io.IOException;

import com.tiscap.protocol.Protocol;

@SuppressWarnings("unchecked")
public class Broadcast implements Runnable {
	private Vector<Tuple<String, BufferedWriter>> users;
	private BlockingQueue<Tuple<String, String>> messages;

	public Broadcast(Vector<Tuple<String, BufferedWriter>> initUsers, BlockingQueue<Tuple<String, String>>
	initMessages) {
		this.users = initUsers;
		this.messages = initMessages;
	}

	/** 
	Reads in messages from the queue indefinitely.  This will use the
	take() method of the BlockingQueue which will block this thread 
	indefinitely until a message is able to be removed from the queue.
	 */
	public void run() {
		while (true) { // Repeat indefinitely
			String sender = "";
			String msg = "";
			
			try {
				Tuple<String, String> taken = messages.take();
				sender = taken.x; // The sender's username
				msg = taken.y; // Get a message when it becomes available
			}
			catch (InterruptedException ie) {
				continue; // We didn't get any input, so just start over and try to get the message again
			}

			int newLine = msg.indexOf("\r\n"); // get the index of the first occurrence of \r\n
			String command = msg.substring(0, newLine);
			String msgBody = msg.substring(newLine); // The \r\n and the message contents and the end-char \u0004
			int space = command.indexOf(" "); // Find the index of the space if there is one - for "/private uname\r\n"
			String verb = ""; // the command itself
			String uname = ""; // the user name (if /private)

			if (space > 0) {
				verb = command.substring(0, space);
				uname = command.substring(space + 1); // don't include the space in the user name
			}
			else {
				verb = command;
			}

			if (!(verb.equalsIgnoreCase( Protocol.CLIENT_PUBLIC_MSG) || verb.equalsIgnoreCase(Protocol.CLIENT_PRIVATE_MSG))) {
				// This is a bad request...
			}
			else if (verb.equalsIgnoreCase(Protocol.CLIENT_PUBLIC_MSG)) {
				// send a public message
				String response = "";
				response += Protocol.SERVER_PUBLIC_MSG;
				response += " " + sender; // the sender's username
				response += msgBody; // this includes the \r\n, body, and \u0004

				Object[] userArray = new Object[users.size()*2 + 5]; // just to be safe
				users.copyInto(userArray); 

				Tuple<String, BufferedWriter> entry;

				for (int i = 0; i < userArray.length; i++) {
					if (userArray[i] != null) {
						entry = (Tuple<String, BufferedWriter>) userArray[i];
						try {
							// entry.y yields an BufferedWriter that can talk to that client
							entry.y.write(response);
							entry.y.flush();
						} 
						catch (IOException ioe) { 
							// We can't send a message to this user, but the others should still get it
						}
					}
				}
			}
			else {
				// send a private message
				String response = "";
				response += Protocol.SERVER_PRIVATE_MSG;
				response += " " + sender; // the sender's username
				response += msgBody; // this includes the \r\n, body, and \u0004

				Object[] userArray = new Object[users.size()*2 + 5]; // just to be safe
				users.copyInto(userArray); 

				Tuple<String, BufferedWriter> entry;

				for (int i = 0; i < userArray.length; i++) {
					if (userArray[i] != null) {
						entry = (Tuple<String, BufferedWriter>) userArray[i]; 
						// entry.x is the user name string and entry.y is the BufferedWriter
						// note the username Is case sensitive
						if (entry.x.equals(uname)) {
							try { 
								entry.y.write(response);
								entry.y.flush();
							}
							catch (IOException ioe) { 
								// We can't send a message to this user, but the others should still get it
							}
							break; // We send to at most one user so we're done
						}
					}
				}
			}
		} // end while
	} // end run
} // end Broadcast