package com.tiscap.chatclient;

public class UsersUpdateThread implements Runnable {
	ChatClient theGui;
	
	public UsersUpdateThread(ChatClient cc) {
		theGui = cc;
	}

	public void run() {
		while (true) {
			theGui.softUpdateUserList();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				continue;
			}
		}		
	}
}
