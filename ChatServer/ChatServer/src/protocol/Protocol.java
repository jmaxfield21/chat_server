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
	private static final String CLIENT_LOGIN = "/Login";
	private static final String CLIENT_USER_REQUEST = "/Users";
	private static final String CLIENT_PUBLIC_MSG = "/Public";
	private static final String CLIENT_PRIVATE_MSG = "/Private";
	private static final String CLIENT_CLOSE = "/Close";
	
	//Server side commands sent to all connected clients
	private static final String SERVER_WELCOME = "]Welcome";
	private static final String SERVER_USER_TAKEN = "]UsernameTaken";
	private static final String SERVER_USER_CONNECTED = "]Connected";
	private static final String SERVER_USER_DISCONNECTED = "]Disconnected";
	private static final String SERVER_PUBLIC_MSG = "]Public";
	private static final String SERVER_PRIVATE_MSG = "]Private";
	private static final String SERVER_ACTIVE_USERS = "]ActiveUsers";
	private static final String SERVER_ERROR = "]Error";
	private static final String SERVER_BAD_SYNTAX = "]BadSyntax";
	
}
