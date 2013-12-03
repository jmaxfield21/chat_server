package chatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.*;

import protocol.Protocol;
import chatclient.ChatClient;

/**
 * Client.java
 *  
 * Manages all the back-end work of 
 * parsing, error checking, and interacting with the
 * server. It will pass the needed information to the GUI.
 * 
 * @author Jace Maxfield && Sean Groathouse
 *
 */

@SuppressWarnings("unused")
public class Client {
	public static final int DEFAULT_PORT = 4020;
	private String error;
	private String publicMsg;
	private String privateMsg;
	private Socket sock;
	private BufferedReader fromServer = null;
	private PrintWriter toServer = null;
	private Vector<String> activeUsers;

	private static final Executor exec = Executors.newCachedThreadPool();
	private boolean connectedToServer;
	private boolean userIsLoggedIn;
	private boolean unameTakenToReport = false; // if we receive a username taken error, set to true to report it, set back to false when done
	
	public Client() {
		activeUsers = new Vector<String>();
		connectedToServer = false;
		userIsLoggedIn = false;
	}

	/** 
	I renamed this from run to listen (I think that's what it was intended to do?)
	Creates a ChatReader object to read in from the server in a separate thread.
	*/
	private void listen() {
		Runnable cr = new ChatReader(this, fromServer);
		exec.execute(cr);
	}
	
	/** 
	Attempt to connect to the given server.  Return true upon success, false otherwise.
	*/
	public boolean connect(String server) {
		if (server.equals("") || server.equals(null)) {
			return false;
		}
		try {
			sock = new Socket(server, DEFAULT_PORT);
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()), true); // auto-flush enabled
			connectedToServer = true;
			listen(); // Begin listening to this server
			return true;			
		}
		catch (IOException ioe) {
			
		}
		return false;
	}
	
	/** 
	Attempt to login with the given username. Return true upon a welcome receipt, false if usernameTaken.
	*/
	public boolean login(String uname) {
		if (uname.equals("") || uname.equals(null)) {
			return false;
		}
		if (!Protocol.isValidUsername(uname)) {
			return false;
		}
		unameTakenToReport = false;
		String command = Protocol.CLIENT_LOGIN + " " + uname + "\r\n";
		toServer.println(command);
		while (!userIsLoggedIn && !unameTakenToReport) {
			try {
				Thread.sleep(100); // try again in 100 milliseconds
			}
			catch (InterruptedException ie){
				continue;
			}				
			// we haven't received a response either way from the server yet
		}
		if (userIsLoggedIn) {
			return true;
		}
		else if (unameTakenToReport) {
			unameTakenToReport = false; // we read this one, no more to report right now
			return false;
		}
		return false;
	}

	/**
	Creates and sends a public msg request to the server with the given msgBody.
	The EOT will be added by this method and should not be given in the msgBody parameter.
	*/
	public void sendPublicMsg(String msgBody) {
		String command = Protocol.CLIENT_PUBLIC_MSG + "\r\n";
		
		command += msgBody + Protocol.EOT;
		toServer.println(command);
		
	}

	/**
	Creates and sends a private msg request to the server with the given msgBody and username.
	The EOT will be added by this method and should not be given in the msgBody parameter.
	(Assumes the uname has already been pre-checked)
	*/
	public void sendPrivateMsg(String msgBody, String uname) {
		String command = Protocol.CLIENT_PRIVATE_MSG + " " + uname + "\r\n";
		
		command += msgBody + Protocol.EOT;
		toServer.println(command);
	}

	public void sendUsersRequest() {
		String command = Protocol.CLIENT_USER_REQUEST + "\r\n";
		toServer.println(command);
	}

	public void sendCloseRequest() {
		String command = Protocol.CLIENT_CLOSE + "\r\n";
		toServer.println(command);
	}
	
	/** 
	Returns true if the given privUser is present in the vector
	of active users.
	*/
	public boolean isValidPrivateUser(String privUser) {
		return activeUsers.contains(privUser);
	}



	/********
	Methods that the ChatReader will call after it parses the server's commands.
	*********/


	public void receivedUsernameTaken() {
		unameTakenToReport = true;
	}

	public void receivedWelcome() {
		userIsLoggedIn = true;
	}

	public void receivedPublicMsg(String msgBody, String senderName) {
		publicMsg = senderName + ": " + msgBody;
	}

	public String getPublicMsg() {
		return publicMsg;
	}
	
	public void resetPublicMsg() {
		publicMsg = null;
	}
	
	public void receivedPrivateMsg(String msgBody, String senderName) {
		privateMsg = senderName + ": " + msgBody;
	}
		
	public String getPrivateMsg() {
		return privateMsg;
	}
	
	public void resetPrivateMsg() {
		privateMsg = null;
	}

	public void receivedActiveUsers(Vector<String> users) {
		activeUsers = users;
	}

	public void receivedConnected(String uname) {
		activeUsers.add(uname);
	}

	public void receivedDisconnected(String uname) {
		activeUsers.remove(uname);
	}

	public void receivedBadSyntax(String errorMsg) {
		error = errorMsg;
	}

	public void receivedError(String errorMsg) {
		error = errorMsg;
	}
	
	public Vector<String> getUsersList() {
		return activeUsers;
	}
	
}
