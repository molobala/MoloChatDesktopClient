package com.molo.entity;


import java.util.LinkedList;
import java.util.List;

import javax.swing.ListModel;

import com.molo.gui.SMChatListModel;

public class ChatThread {
	private long messageCount=0;
	public  int SIZE = 15;
	public int PAGE=0;
	private long id;
	private String other;
	private int unread;
	private String lastActiveTime,
				   lastMessage,
				   lastSender,
				   profil;
	private boolean isOnline;
	private String self;
	private String date;
	private SMChatListModel listModel=new SMChatListModel();
	private boolean isNew=false;
	
	public boolean isOnline() {
		return isOnline;
	}
	public void setIsOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
	public String getProfil() {
		return profil;
	}
	public void setProfil(String profil) {
		this.profil = profil;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public ChatThread() {
		// TODO Auto-generated constructor stub
	}
	public ChatThread(long id, String member1, String oth) {
		super();
		this.id = id;
		this.other = oth;

	}
	public void push(ChatMessage message){
		listModel.add(message);
	}
	public int getUnread() {
		return unread;
	}
	public String getLastSender() {
		return lastSender;
	}
	public void setLastSender(String lastSender) {
		this.lastSender = lastSender;
	}
	public void setUnread(int unread) {
		this.unread = unread;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getOther() {
		return other;
	}
	public void setOther(String oth) {
		this.other = oth;
	}

	public void setLastActiveTime(String lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	public String getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	public String getLastMessage() {
		return lastMessage;
	}

	
	public List<ChatMessage> getMessages() {
		return listModel.getMessages();
	}
	public void setMessages(List<ChatMessage> messages) {
		this.listModel.clear();
		this.listModel.addAll(messages);
	}
	public void setSelf(String self) {
		this.self = self;
	}

	public String getSelf() {
		return self;
	}
	public ListModel<ChatMessage> getDataModel() {
		// TODO Auto-generated method stub
		return listModel;
	}
	public void setMessageCount(long messageCount) {
		this.messageCount = messageCount;
	}
	public long getMessageCount(){
		return messageCount;
	}
	public int size(){
		return listModel.getSize();
	}

	public void unshift(ChatMessage cm) {
		listModel.addFirst(cm);
	}

	public void addAll(List<ChatMessage> mls) {
		listModel.addAll(mls);
	}
	public void markAllAsSeen() {
		// TODO Auto-generated method stub
		for(ChatMessage m:listModel.getMessages())
			m.setSeen(true);
	}
	public ChatMessage getMessageAt(int currentPos) {
		// TODO Auto-generated method stub
		return listModel.get(currentPos);
	}
	public boolean isNew() {
		// TODO Auto-generated method stub
		return isNew;
	}
	public void setNew(boolean t){
		isNew=t;
	}
}
