package com.molo.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;

import com.molo.entity.ChatMessage;



public class SMChatListModel extends AbstractListModel<ChatMessage>{
	LinkedList<ChatMessage> mList=new LinkedList<ChatMessage>();
	public SMChatListModel() {
		// TODO Auto-generated constructor stub
	}
	public SMChatListModel(ChatMessage[] l){
		for(ChatMessage s:l)
			mList.add(s);
	}
	public SMChatListModel(List<ChatMessage> l){
		mList.addAll(l);
	}
	@Override
	public ChatMessage getElementAt(int index) {
		// TODO Auto-generated method stub
		if(index>=getSize())
			return null;
		return mList.get(index);
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return mList.size();
	}
	public void clear() {
		// TODO Auto-generated method stub
		mList.clear();
	}
	public void remove(ChatMessage content) {
		// TODO Auto-generated method stub
		mList.remove(content);
	}
	public void add(ChatMessage content) {
		// TODO Auto-generated method stub
		mList.add(content);
	}
	public boolean contains(ChatMessage el) {
		// TODO Auto-generated method stub
		return mList.contains(el);
	}
	public ChatMessage get(int ind) {
		// TODO Auto-generated method stub
		return mList.get(ind);
	}
	public List<ChatMessage> getMessages() {
		// TODO Auto-generated method stub
		return mList;
	}
	public void addAll(List<ChatMessage> messages) {
		// TODO Auto-generated method stub
		mList.addAll(messages);
	}
	public void addFirst(ChatMessage cm) {
		// TODO Auto-generated method stub
		mList.addFirst(cm);
	}
	

}
