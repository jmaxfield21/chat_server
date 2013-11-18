package server;

/**
Broadcast.java
@author Jace Maxfield && Sean Groathouse

This is the broadcast thread which pulls from the message queue in the
server.  The server will initiate a single instance of this class
upon its initialization, and will pass a copy of the users:sockets
vector as well as the queue of messages.

Once it pulls a message from the queue, it parses the contents
to determine if the message is public or private and get the message
body.  Then using the vector of users:sockets on the server, it 
creates the appropriate responses to the recipients and sends the 
message to their respective sockets.
*/

import java.util.concurrent.*;
import java.util.Vector;
import java.net.*;

public class Broadcast implements Runnable {
	private Vector users;
	private BlockingQueue messages;

	private BufferedOutputStream toClient; 
	private Socket[] sockets; // TODO: This should be tuple, but that's currently private

	public Broadcast(Vector initUsers, BlockingQueue initMessages) {
		this.users = initUsers;
		this.messages = initMessages;
	}

	/** 
	Reads in messages from the queue indefinitely.  This will use the
	take() method of the BlockingQueue which will block this thread 
	indefinitely until a message is able to be removed from the queue.
	*/
	public void run() {
		String msg = messages.take(); // Get a message when it becomes available
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
			// TODO: we need to know who is sending the message...
			response += msgBody; // this includes the \r\n, body, and \u0004
			
			// Specifying sockets as a parameter means the return type will be Socket[] 
			sockets = users.toArray(sockets); 

			try {
				for (int i = 0; i < sockets.length(); i++) {
					toClient = new BufferedOutputStream(new DataOutputStream(client.getOutputStream()));
					// TODO: should we be storing (user, outputStream) instead of Sockets?!
					toClient.write(response);
					toClient.flush();
				}
			}
			catch (IOException ioe) {
				// Hopefully this won't need to be here so we won't have to decide
				// what needs to be done...
			}
			finally {
				// see above
			}
		}
		else {
			// send a private message
			String response = "";
			response += "]private ";
			// TODO: add the user name who the message is from
			response += msgBody; // this includes the \r\n, body, and \u0004
		}
	}

}