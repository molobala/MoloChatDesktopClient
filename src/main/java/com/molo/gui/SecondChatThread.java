package com.molo.gui;



import java.util.LinkedList;
import java.util.List;

import javax.swing.ListModel;

import com.molo.entity.ChatMessage;
import com.molo.entity.ChatThread;
import com.molo.entity.Membre;

public class SecondChatThread extends ChatThread{
	private boolean isLoadingMore=false;
	
	public SecondChatThread() {
		// TODO Auto-generated constructor stub
	}
	public SecondChatThread(Membre el) {
		super();
		this.setSelf(el.getLogin());
		// TODO Auto-generated constructor stub
	}
	
	public boolean isLoadingMore() {
		return isLoadingMore;
	}
	public void setLoadingMore(boolean isLoadingMore) {
		this.isLoadingMore = isLoadingMore;
	}
	public SecondChatThread(long id, String member1, String oth) {
		super(id, member1, oth);
		// TODO Auto-generated constructor stub
	}
	public void addAll(ChatMessage[] messages) {
		// TODO Auto-generated method stub
		for(ChatMessage m:messages)
			push(m);
	}
	
	
	
	
}
