package com.molo.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class MChatThreadListCellRender extends JLabel implements ListCellRenderer<ChatThread>{

	@Override
	public Component getListCellRendererComponent(
			JList<? extends ChatThread> list, ChatThread value, int position,
			boolean selected, boolean hasFocus) {
		// TODO Auto-generated method stub
		this.setOpaque(true);
		if(selected)
		{
			this.setBackground(new Color(100,100,100));
			this.setForeground(new Color(20,20,20));
		}else{
			this.setBackground(null);
		}
		if(value.getUnread()>0){
			this.setText("<html><div style=\"width:100%;position:absolute;\"><span>"+value.getOther()+"</span><span style=\"color:red;position:absolute;right:0px;top:0px;\"> ("+value.getUnread()+") </span></div></html>");
		}else{
			this.setText("<html><div style=\"width:100%;position:absolute;\"><span>"+value.getOther()+"</span></div></html>");
		}
		return this;
	}

}
