package com.molo.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

public class MListModel extends AbstractListModel<String>{
	List<String> mList=new ArrayList<String>();
	public MListModel() {
		// TODO Auto-generated constructor stub
	}
	public MListModel(String[] l){
		for(String s:l)
			mList.add(s);
	}
	public MListModel(List<String> l){
		mList.addAll(l);
	}
	@Override
	public String getElementAt(int index) {
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
	public void remove(String content) {
		// TODO Auto-generated method stub
		mList.remove(content);
	}
	public void add(String content) {
		// TODO Auto-generated method stub
		mList.add(content);
	}
	public boolean contains(String el) {
		// TODO Auto-generated method stub
		return mList.contains(el);
	}
	public String get(int ind) {
		// TODO Auto-generated method stub
		return mList.get(ind);
	}
//	public void replaceAll(String oldLog, String newLog) {
//		// TODO Auto-generated method stub
////		for(String s:mList){
////			if(s.equals(oldLog)){
////				s.replaceFirst(oldLog, newLog);
////			}
////		}
//		for(int i=0;i<mList.size();i++){
//			if()
//		}
//	}
	

}
