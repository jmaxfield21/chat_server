package chatclient;

import protocol.Protocol;

import java.io.*;
import java.util.concurrent.*;

/** 
ChatReader.java

Reads input from the server, parses it, and then calls appropriate
Client methods to handle the command.

@author Jace Maxfield && Sean Groathouse


*/

public class ChatReader implements Runnable {
	BufferedReader fromServer;
	
	public ChatReader(Client client, BufferedReader fromTheServer) {
		fromServer = fromTheServer;
	}

	/** 
	Listen to the server indefinitely
	*/ 
	public void run() {
		while (true) {
			try {
				fromServer.readLine();
			} catch (IOException e) {
				continue;
			}
		}
	}
}