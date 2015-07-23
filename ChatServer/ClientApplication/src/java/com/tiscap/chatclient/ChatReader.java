package com.tiscap.chatclient;


import java.io.*;
import com.tiscap.protocol.Protocol;
import java.util.Vector;

/** 
ChatReader.java

Reads input from the server, parses it, and then calls appropriate
Client methods to handle the command.

@author Jace Maxfield && Sean Groathouse


*/

public class ChatReader implements Runnable {
	BufferedReader fromServer;
	Client client;
	
	public ChatReader(Client theClient, BufferedReader fromTheServer) {
		fromServer = fromTheServer;
		client = theClient;
	}

	/** 
	Listen to the server indefinitely
	*/ 
	public void run() {
		String command;
		String truncated;
		String[] words;

		while (true) {
			try {
				command = fromServer.readLine(); // the first line of the server's command
				if (command == null) {
					continue;
				}
				int newLine = command.indexOf("\r\n"); // get the index of the /r/n to cut it off
				if (newLine > -1)
					truncated = command.substring(0, newLine);
				else
					truncated = command.substring(0);

				words = truncated.split(" "); // split by spaces - words[0] should be server command (starts with ])

				// Handle the request based on which of the server commands this is
				if (words[0].equalsIgnoreCase(Protocol.SERVER_WELCOME))
					client.receivedWelcome();
				else if (words[0].equalsIgnoreCase(Protocol.SERVER_USER_TAKEN))
					client.receivedUsernameTaken();
				else if (words[0].equalsIgnoreCase(Protocol.SERVER_USER_CONNECTED)) {
					if (words.length > 1)
						client.receivedConnected(words[1]);
				}
				else if (words[0].equalsIgnoreCase(Protocol.SERVER_USER_DISCONNECTED)) {
					if (words.length > 1) 
						client.receivedDisconnected(words[1]);
				}
				else if (words[0].equalsIgnoreCase(Protocol.SERVER_ACTIVE_USERS)) {
					if (words.length > 1) {
						Vector<String> newUsers = new Vector<String>();
						String[] users = words[1].split(",");
						for (int i = 0; i < users.length; i++) {
							newUsers.add(users[i]);
						}
						client.receivedActiveUsers(newUsers);
					}
				}
				else if (words[0].equalsIgnoreCase(Protocol.SERVER_ERROR)) {
					if (words.length > 1)
						client.receivedError(words[1]);
					else
						client.receivedError("");
				}
				else if (words[0].equalsIgnoreCase(Protocol.SERVER_BAD_SYNTAX)) {
					if (words.length > 1)
						client.receivedBadSyntax(words[1]);
					else
						client.receivedBadSyntax("");
				}
				else if (words[0].equalsIgnoreCase(Protocol.SERVER_PUBLIC_MSG) || words[0].equalsIgnoreCase(Protocol.SERVER_PRIVATE_MSG)) {
					if (words.length > 1) {
						String uname = words[1];
						String msgBody = "";
						char serv;
						while ((serv = (char)fromServer.read()) != Protocol.EOT_CHAR) {
							msgBody += serv;
						}
						if (words[0].equalsIgnoreCase(Protocol.SERVER_PUBLIC_MSG))
							client.receivedPublicMsg(msgBody, uname);
						else
							client.receivedPrivateMsg(msgBody, uname);
					}
				}

			} catch (IOException e) {
				continue;
			}
		} // end while
	}
}