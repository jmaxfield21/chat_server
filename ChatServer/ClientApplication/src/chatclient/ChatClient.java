package chatclient;import java.awt.*;import java.awt.event.*;import java.util.Vector;import javax.swing.*;import javax.swing.border.*;/** * ChatClient.java *  * Modified Chat GUI to interface with chat servers * Will enable easy interaction and usability *  * @author Jace Maxfield && Sean Groathouse *  */@SuppressWarnings("serial")public class ChatClient extends JFrame implements ActionListener, KeyListener, ItemListener{	private JButton sendButton;	private JButton exitButton;	private JButton users;	private JButton addPrivateUser;	private JTextField sendText;	private JTextField privateUser;	private JTextArea displayArea;	private JTextArea userList;	private JLabel serverLabel;	private JLabel serverName;	private JCheckBox privateMsg;		private String privUser;	private Client client = new Client();		public ChatClient() {		// Validate the chat server		String connect2Server = JOptionPane.showInputDialog("Please enter the chat server you wish to connect to:", "");		if (connect2Server == null) {			System.exit(0);		}		while (!client.connect(connect2Server)) {			connect2Server = JOptionPane.showInputDialog("The requested server could not be found, please try again:", "");			if (connect2Server == null) {				System.exit(0);			}		}				// Validate the requested username		String login = JOptionPane.showInputDialog("Welcome! Please login or create a user", "");		if (login == null) {			System.exit(0);		}		while (!client.login(login)) {			login = JOptionPane.showInputDialog("The requested username is unavailable or invalid. Please try again:", "");			if (login == null) {				System.exit(0);			}		}				/**		 * a panel used for placing components		 */		JPanel southPane = new JPanel();		JPanel northPane = new JPanel();		JPanel eastPane = new JPanel();				eastPane.setLayout(new BoxLayout(eastPane, BoxLayout.Y_AXIS));		Border etched = BorderFactory.createEtchedBorder();		Border titled = BorderFactory.createTitledBorder(etched, "Enter Message Here ...");		southPane.setBorder(titled);		/**		 * set up all the components		 */		privateMsg = new JCheckBox("Private Message");		serverLabel = new JLabel("Connected to: ");		serverName = new JLabel(connect2Server);		sendText = new JTextField(30);		privateUser = new JTextField(16);		users = new JButton("Active Users");		addPrivateUser = new JButton("Add Recipient");		sendButton = new JButton("Send");		exitButton = new JButton("Exit");				/**		 * Some special alignment had to be established for the right side		 * of the GUI		 */		users.setAlignmentX(Component.CENTER_ALIGNMENT);		users.setAlignmentY(Component.CENTER_ALIGNMENT);		privateMsg.setAlignmentX(Component.CENTER_ALIGNMENT);		privateMsg.setAlignmentY(Component.CENTER_ALIGNMENT);		addPrivateUser.setAlignmentX(Component.CENTER_ALIGNMENT);		addPrivateUser.setAlignmentY(Component.CENTER_ALIGNMENT);		/**		 * register the listeners for the different button clicks		 */		sendText.addKeyListener(this);		privateUser.addKeyListener(this);		users.addActionListener(this);		privateMsg.addActionListener(this);		addPrivateUser.addActionListener(this);		sendButton.addActionListener(this);		exitButton.addActionListener(this);				privateUser.setEnabled(false);						// Set up the necessary user list area		userList = new JTextArea(15,15);		userList.setEditable(false);		userList.setFont(new Font("SansSerif", Font.PLAIN, 14));		JScrollPane userScroll = new JScrollPane(userList);		getContentPane().add(userScroll, "Center");		/**		 * add the components to the panel		 */		northPane.add(serverLabel);		northPane.add(serverName);		southPane.add(sendText);		southPane.add(sendButton);		southPane.add(exitButton);		eastPane.add(users);		eastPane.add(userList);		eastPane.add(privateMsg);		eastPane.add(privateUser);		eastPane.add(addPrivateUser);				// Empty border added to keep components from being		// on top of each other		eastPane.setBorder(new EmptyBorder(10, 10, 10, 10));		/**		 * add the panel to the "south" end of the container		 */		getContentPane().add(northPane,"North");		getContentPane().add(southPane,"South");		getContentPane().add(eastPane, "East");		/**		 * add the text area for displaying output. Associate		 * a scrollbar with this text area. Note we add the scrollpane		 * to the container, not the text area		 */		displayArea = new JTextArea(15,40);		displayArea.setEditable(false);		displayArea.setFont(new Font("SansSerif", Font.PLAIN, 14));		JScrollPane scrollPane = new JScrollPane(displayArea);		getContentPane().add(scrollPane,"Center");		/**		 * set the title and size of the frame		 */		setTitle("Chat Room");		pack();		setVisible(true);		sendText.requestFocus();				receiveMessages();		/** anonymous inner class to handle window closing events */		addWindowListener(new WindowAdapter() {			public void windowClosing(WindowEvent evt) {				System.exit(0);			}		} );	}		/**	 * Run indefinitely while the client is running and 	 * adds all the messages to the screen	 */	public void receiveMessages() {		while (true) {			try {				Thread.sleep(200);				if (client.getPublicMsg() != null) {					displayArea.append(client.getPublicMsg() + "\n");					client.resetPublicMsg();				}				if (client.getPrivateMsg() != null) {					displayArea.append(client.getPrivateMsg() + "\n");					client.resetPrivateMsg();				}			}			catch (InterruptedException ie) { }		}	}		/**	 * This gets the text the user entered and outputs it	 * in the display area.	 */	public void sendMessage() {		String message = sendText.getText().trim();				if (privateMsg.isSelected())			client.sendPrivateMsg(message, privUser);		else			client.sendPublicMsg(message);		sendText.setText("");		sendText.requestFocusInWindow();	}		/**	 * This will add the requested user within the GUI	 */	public void setPrivateUser(String userName) {		privUser = userName;		if (client.isValidPrivateUser(privUser)) {			privateUser.setBackground(Color.GREEN.brighter());			privateUser.setForeground(Color.BLACK);		}		else {			privateUser.setBackground(Color.RED.brighter());			privateUser.setForeground(Color.WHITE);		}	}		/**	 * Populates the Active User list with the most recent	 * list of active users	 */	public void populateActiveUserList() {		userList.setText("");		client.sendUsersRequest();		Vector<String> users = client.getUsersList();		for (int i = 0; i < users.size(); i++) {			userList.append("    " + users.get(i) + "\n");		}	}	/**	 * This method responds to action events .... i.e. button clicks	 * and fulfills the contract of the ActionListener interface.	 */	public void actionPerformed(ActionEvent evt) {		Object source = evt.getSource();		ItemEvent ie = new ItemEvent(privateMsg, ItemEvent.SELECTED, evt, ItemEvent.DESELECTED);		if (source == sendButton) 			sendMessage();		else if (source == privateMsg)			itemStateChanged(ie);		else if (source == users)			populateActiveUserList();		else if (source == addPrivateUser)			setPrivateUser(privateUser.getText());		else if (source == exitButton)			System.exit(0);	}		/**	 * This method is used only for the private message option	 * @param e	 */	public void itemStateChanged(ItemEvent e) {		if (e.getSource() == privateMsg) {			if (privateMsg.isSelected()) {				privateUser.setEnabled(true);			}			else				privateUser.setEnabled(false);		}	}	/**	 * These methods responds to keystroke events and fulfills	 * the contract of the KeyListener interface.	 */	/**	 * This is invoked when the user presses	 * the ENTER key.	 */	public void keyPressed(KeyEvent e) { 		if (e.getKeyCode() == KeyEvent.VK_ENTER)			sendMessage();	}	/** Not implemented */	public void keyReleased(KeyEvent e) { }	/** Not implemented */	public void keyTyped(KeyEvent e) {  }	@SuppressWarnings("unused")	public static void main(String[] args) {		JFrame win = new ChatClient();	}}