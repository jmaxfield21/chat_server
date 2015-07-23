package com.tiscap.server;

/**
MessageAdder.java
@author Jace Maxfield && Sean Groathouse

This is a simple class to allow messages to be added in a separate thread
in case the blocking-queue is not immediately available (we don't want the
server thread to be waiting for the queue to beomce available, nor do we
want any individual client thread to have to stall while the queue is busy).
 */

import java.util.concurrent.*;

public class MessageAdder implements Runnable {
	
	private BlockingQueue<Tuple<String, String>> messages;
	private String senderName;
	private String command;

	public MessageAdder(BlockingQueue<Tuple<String, String>> initMessages, String senderUserName, String entireCommand) {
		this.messages = initMessages;
		this.senderName = senderUserName;
		this.command = entireCommand;
	}

	/** 
	Reads in messages from the queue indefinitely.  This will use the
	take() method of the BlockingQueue which will block this thread 
	indefinitely until a message is able to be removed from the queue.
	 */
	public void run() {
		while (true) {
			try {
				// will stall this thread until the queue is available to add
				messages.put(new Tuple<String, String>(senderName, command));
				break; // we added it successfully, so we're done.
			}
			catch (InterruptedException ie) {
				// don't do anything and we'll try again when the for loop starts back up
			}
			catch (NullPointerException npe) {
				break; // we shouldn't be adding this
			}
		}
	} // end run
} // end MessageAdder