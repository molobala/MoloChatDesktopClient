package com.molo.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import com.molo.entity.Membre;

public class SecondMListModel extends AbstractListModel<Membre>{
	List<Membre> mList=new ArrayList<Membre>();
	public SecondMListModel() {
		// TODO Auto-generated constructor stub
	}
	public SecondMListModel(Membre[] l){
		for(Membre s:l)
			mList.add(s);
	}
	public SecondMListModel(List<Membre> l){
		mList.addAll(l);
	}
	@Override
	public Membre getElementAt(int index) {
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
	public void remove(Membre content) {
		// TODO Auto-generated method stub
		mList.remove(content);
	}
	public void add(Membre content) {
		// TODO Auto-generated method stub
		mList.add(content);
	}
	public boolean contains(Membre el) {
		// TODO Auto-generated method stub
		return mList.contains(el);
	}
	public Membre get(int ind) {
		// TODO Auto-generated method stub
		return mList.get(ind);
	}
//	public void replaceAll(Membre oldLog, Membre newLog) {
//		// TODO Auto-generated method stub
////		for(Membre s:mList){
////			if(s.equals(oldLog)){
////				s.replaceFirst(oldLog, newLog);
////			}
////		}
//		for(int i=0;i<mList.size();i++){
//			if()
//		}
//	}
	public void remove(String log) {
		// TODO Auto-generated method stub
		mList.remove(log);
	}
	public Membre get(String login) {
		// TODO Auto-generated method stub
		for(Membre m:mList){
			if(m.getLogin().equals(login))
				return m;
		}
		return null;
	}
	

}
