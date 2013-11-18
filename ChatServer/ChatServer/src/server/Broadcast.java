package server;

/**
Broadcast.java
@author Jace Maxfield && Sean Groathouse

This is the broadcast thread which pulls from the message queue in the
server.  The server will initiate a single instance of this class
upon its initialization, and will pass a copy of the users:OutputStreams
vector as well as the queue of messages.

Once it pulls a message from the queue, it parses the contents
to determine if the message is public or private and get the message
body.  Then using the vector of users:OutputStreams on the server, it 
creates the appropriate responses to the recipients and sends the 
message to their respective outputStreams.
*/

import java.util.concurrent.*;
import java.util.Vector;
import java.net.*;

public class Broadcast implements Runnable {
	private Vector<Tuple<String, OutputStream>> users;
	private BlockingQueue<Tuple<String, String>> messages;

	private Tuple<String, OutputStream>[] userArray; 


	public Broadcast(Vector<Tuple<String, OutputStream>> initUsers, BlockingQueue<Tuple<String, String>>
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
		Tuple<String, String> taken = messages.take();
		String sender = taken.X; // The sender's username
		String msg = taken.Y; // Get a message when it becomes available

		int newLine = msg.indexOf("\r\n"); // get the index of the first occurrence of \r\n
		String command = msg.substring(0, newLine);
		String msgBody = msg.substring(newLine); // The \r\n and the message contents and the end-char \u0004
		int space = command.indexOf(" "); // Find the index of the space if there is one - for "/private uname\r\n"
		String verb = ""; // the command itself
		String uname = ""; // the user name (if /private)

		if (space > 0) {
			verb = command.substring(0, space);
			uname = command.substring(space);
		}
		else {
			verb = command;
		}

		if !(verb.equalsIgnoreCase("/public") || verb.equalsIgnoreCase("/private")) {
			// This is a bad request...
		}
		else if (verb.equalsIgnoreCase("/public") {
			// send a public message
			String response = "";
			response += "]public ";
			response += sender; // the sender's username
			response += msgBody; // this includes the \r\n, body, and \u0004
			
			// Specifying userArray as a parameter means the return type will be Tuple<String, OutputStream>[] 
			userArray = users.toArray(userArray); 

			for (int i = 0; i < userArray.length(); i++) {
				// userArray[i].Y yields an OutputStream that can talk to that client
				userArray[i].Y.write(response);
				userArray[i].Y.flush();
			}
		}
		else {
			// send a private message
			String response = "";
			response += "]private ";
			response += sender; // the sender's username
			response += msgBody; // this includes the \r\n, body, and \u0004

			// Specifying userArray as a parameter means the return type will be Tuple<String, OutputStream>[]
			userArray = users.toArray(userArray);

			for (int i = 0; i < userArray.length(); i++) {
				// userArray[i].X is the user name string and userArray[i].Y is the outputStream
				if (userArray[i].X.equals(uname)) {
					userArray[i].Y.write(response);
					userArray[i].Y.flush();
					break; // We send to at most one user so we're done
				}
			}
		}

		run(); // Repeat indefinitely
	}

}