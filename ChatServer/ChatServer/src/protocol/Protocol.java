package protocol;

/**
 * Protocol.java
 * @author Jace Maxfield && Sean Groathouse
 * This class will be the 'Protocol Enforcer'.
 * It will contain protocol variables as well as booleans checking to ensure
 * the protocol is kept across the network
 */

public class Protocol {
	
	//Client side commands that are sent to the server
	public static final String CLIENT_LOGIN = "/Login";
	public static final String CLIENT_USER_REQUEST = "/Users";
	public static final String CLIENT_PUBLIC_MSG = "/Public";
	public static final String CLIENT_PRIVATE_MSG = "/Private";
	public static final String CLIENT_CLOSE = "/Close";
	
	//Server side commands sent to all connected clients
	public static final String SERVER_WELCOME = "]Welcome";
	public static final String SERVER_USER_TAKEN = "]UsernameTaken";
	public static final String SERVER_USER_CONNECTED = "]Connected";
	public static final String SERVER_USER_DISCONNECTED = "]Disconnected";
	public static final String SERVER_PUBLIC_MSG = "]Public";
	public static final String SERVER_PRIVATE_MSG = "]Private";
	public static final String SERVER_ACTIVE_USERS = "]ActiveUsers";
	public static final String SERVER_ERROR = "]Error";
	public static final String SERVER_BAD_SYNTAX = "]BadSyntax";
	
}
