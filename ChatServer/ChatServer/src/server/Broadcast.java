package server;

/**
Protocol.java
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

	public Broadcast(Vector initUsers, BlockingQueue initMessages) {
		this.users = initUsers;
		this.messages = initMessages;
	}

	/** 
	Reads in messages from the queue indefinitely.
	*/
	public void run() {
		
	}

}