package com.molo.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import com.molo.message.ChatMessage;

public class MChatMessageListCellRender extends JLabel implements ListCellRenderer<ChatMessage>{
	private String self;
	public MChatMessageListCellRender(String log) {
		// TODO Auto-generated constructor stub
		this.self=log;
		System.out.println(self);
	}
	@Override
	public Component getListCellRendererComponent(
			JList<? extends ChatMessage> list, ChatMessage value, int index,
			boolean isSelected, boolean cellHasFocus) {
		this.setOpaque(true);
		if(value.getSender().equals(self)){
			//this.setBackground(new Color(200, 200, 200));
			this.setHorizontalAlignment(SwingConstants.RIGHT);
			this.setForeground(Color.BLACK);
			this.setText("<html><div style=\"width:100%;position:absolute;\"><p style=\"background-color:#909090;min-height:40px;margin-bottom:10px;border-radius:5px;position:absolute;right:0;border:1px solid #888\">"+value.getContent()+"</p></div></html>");
		}else{
			//this.setBackground(new Color(0, 50, 150));
			this.setHorizontalAlignment(SwingConstants.LEFT);
			this.setForeground(Color.BLACK);
			this.setText("<html><div style=\"width:100%;position:absolute;\"><p style=\"background-color:#6dd;min-height:40px;margin-bottom:10px;border-radius:5px;position:absolute;left:0;border:1px solid #888\">"+value.getContent()+"</p></div></html>");
		}
		//this.setText(value.getContent());
		// TODO Auto-generated method stub
		return this;
	}
	public void setLogin(String text) {
		// TODO Auto-generated method stub
		this.self=text;
	}
	
}
