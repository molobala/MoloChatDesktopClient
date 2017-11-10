package com.molo.entity;

import java.util.List;




public class ChatMessage {
	private  long id;//l'idenetifiant unique
	private String content;
	private String receiver;
	private String sender;
	private long thread;
	private  boolean seen=false,isNew;
	private  boolean isVoiceMessage=false;
	private List<Attachment> attachments;
	private boolean isPlaying=false;
	private String date;
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public long getThread() {
		return thread;
	}
	public void setThread(long thread) {
		this.thread = thread;
	}
	
	public boolean isVoiceMessage() {
		return isVoiceMessage;
	}
	public void setIsVoiceMessage(boolean isVoiceMessage) {
		this.isVoiceMessage = isVoiceMessage;
	}
	public List<Attachment> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}
	public ChatMessage(long id, String content,long thread, String receiver,
			String sender) {
		super();
		this.id = id;
		this.content = content;
		this.receiver = receiver;
		this.sender = sender;
		this.thread=thread;
	}
	public ChatMessage() {
		// TODO Auto-generated constructor stub
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}

	public void setSeen(boolean seen) {
		this.seen = seen;
	}
	public boolean isSeen(){
		return  this.seen;
	}
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		return isPlaying;
	}
	public void setPlaying(boolean b){
		isPlaying=b;
	}
	public boolean isNew() {
		return isNew;
	}
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
	
}
