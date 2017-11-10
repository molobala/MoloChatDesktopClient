package com.molo.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import com.molo.message.ChatMessage;

public class MChatListModel extends AbstractListModel<ChatMessage>{
	List<ChatMessage> mList=new ArrayList<ChatMessage>();
	public MChatListModel() {
		// TODO Auto-generated constructor stub
	}
	public MChatListModel(ChatMessage[] l){
		for(ChatMessage s:l)
			mList.add(s);
	}
	public MChatListModel(List<ChatMessage> l){
		mList.addAll(l);
	}
	@Override
	public ChatMessage getElementAt(int index) {
		// TODO Auto-generated method stub
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
	

}
