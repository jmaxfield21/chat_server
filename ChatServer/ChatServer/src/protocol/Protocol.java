package protocol;

/**
 * Protocol.java
 * @author Jace Maxfield && Sean Groathouse
 * This class will be the 'Protocol Enforcer'.
 * It will contain protocol variables as well as booleans checking to ensure
 * the protocol is kept across the network
 */

public class Protocol {

	public static final int MAX_MESSAGE_LENGTH = 10000;
	private static final char[] ALPHA_NUMERIC = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
			'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 
			'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

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

	//End of transmission command
	public static final String EOT = "\u0004"; 
	public static final char EOT_CHAR = '\u0004';

	public Protocol() {	}


	//Validates a requested username
	public static boolean isValidUsername(String userName) {
		boolean valid = false;
		char[] userCheck = userName.toCharArray();
		if(userName.length() < 16 && userName != null) {
			valid = true;
		}
		for (int i = 0; i < userCheck.length; i++) {
			boolean validChar = false;
			for (int j = 0; j < ALPHA_NUMERIC.length; j++) {
				if (userCheck[i] == ALPHA_NUMERIC[j]) {
					validChar = true;
					break;
				}
			}
			if(!validChar)
				valid = false;
		}	
		return valid;
	}

	/** 
	Returns true if this is a proper first line (that is if the command is 
	1.  '/users\r\n'
	2.  '/close\r\n'
	3.  '/public\r\n'
	4.  '/private uName\r\n'
	5.  '/login uName\r\n')

	*/
	public static boolean isProperFirstLine(String firstLine) {
		if (firstLine.equalsIgnoreCase(CLIENT_USER_REQUEST + "\r\n")) {
			return true;
		}
		else if (firstLine.equalsIgnoreCase(CLIENT_CLOSE + "\r\n")) {
			return true;
		}
		else if (firstLine.equalsIgnoreCase(CLIENT_PUBLIC_MSG + "\r\n")) {
			return true;
		}
		else if (firstLine.indexOf(" ") < 1) {
			return false;
		}
		else {
			String[] words = firstLine.split(" ");
			if (words[0].equalsIgnoreCase(CLIENT_PRIVATE_MSG) || words[0].equalsIgnoreCase(CLIENT_LOGIN)) {
				if (words[1] == null) {
					return false;
				}
				if (words[1].contains("\r\n")) {
					return true;
				}
			}
		}
				
		return false;
	}

	/** 
	Returns true if the entire command is proper.
	*/
	public static boolean isProperCommand(String command) {
		boolean correct = false;
		char[] cmd = command.toCharArray();

		for (int i = 0; i < cmd.length; i++) {
			if(cmd[i] == '\r') {
				String[] parsedCmd = command.split("\r\n");
				if(parsedCmd[0].equalsIgnoreCase(CLIENT_USER_REQUEST) || parsedCmd[0].equalsIgnoreCase(CLIENT_CLOSE)) {
					correct = true;
					break;
				}
				else if(parsedCmd[0].equalsIgnoreCase(CLIENT_PUBLIC_MSG)) {
					if(cmd[cmd.length-1] == EOT_CHAR) {
						correct = true;
						break;
					}
				}
				break;
			}
			else if(cmd[i] == ' ') {
				String[] parsedCmd = command.split(" ");
				if (parsedCmd.length < 2) {
					break;
				}

				if(parsedCmd[0].equalsIgnoreCase(CLIENT_LOGIN)) {
					if (parsedCmd[1].charAt(0) == '\r' || parsedCmd[1].charAt(0) == '\n') {
						return false;
					}
					correct = true;
					break;
				}
				else if(parsedCmd[0].equalsIgnoreCase(CLIENT_PRIVATE_MSG)) {
					if (parsedCmd[1].charAt(0) == '\r' || parsedCmd[1].charAt(0) == '\n') {
						return false;
					}
					if(cmd[cmd.length-1] == EOT_CHAR) {
						correct = true;
						break;
					}
				}
				break;
			}
		}
		return correct;
	}
}
