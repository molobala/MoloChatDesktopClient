package com.molo.gui;

import java.util.ArrayList;
import java.util.List;

import com.molo.message.ChatMessage;

public class ChatThread {
	private MChatListModel messages=new MChatListModel();//list de message dans le fil de chat
	private String other="";
	private long unread=0;
	public ChatThread() {
		
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return other;
	}
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(o==this) return true;
		if(((ChatThread)o).getOther().equals(other))
			return true;
		return false;
	}
	public ChatThread(MChatListModel messages) {
		this.messages = messages;
	}

	public ChatThread(String oth) {
		// TODO Auto-generated constructor stub
		other=oth;
	}
	public void add(ChatMessage mes){
		messages.add(mes);
	}
	
	public String getOther() {
		return other;
	}
	public void setOther(String other) {
		this.other = other;
	}
	public MChatListModel getMessages() {
		return messages;
	}
	public void setMessages(MChatListModel messages) {
		this.messages = messages;
	}
	public void incrementUnreadMessage() {
		// TODO Auto-generated method stub
		unread++;
	}
	public long getUnread() {
		return unread;
	}
	public void setUnread(long unread) {
		this.unread = unread;
	}
	
}
