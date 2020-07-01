package com.molo.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import com.molo.Main;
import com.molo.entity.Membre;

public class SMChatThreadListCellRender extends JXPanel implements ListCellRenderer<SecondChatThread>{
	private static final long serialVersionUID = 6813982407159099503L;
	JXLabel userProfil,name,unread,lastMessage,lastActiveTime,onLine;
	JXPanel centerPan,rightPane;
	Color selectedC=new Color(200,150,0),defaultc=new Color(200,200,200),color;
	private Font unreadFont,readFont;
	private Membre self;
	public Membre getSelf() {
		return self;
	}
	public void setSelf(Membre self) {
		this.self = self;
	}
	public SMChatThreadListCellRender(Membre self) {
		// TODO Auto-generated constructor stub
		this.self=self;
		this.setOpaque(true);
		createView();
		unreadFont=new Font(lastMessage.getFont().getName(), Font.BOLD, 14);
		readFont=lastMessage.getFont();
	}
	@Override
	public Component getListCellRendererComponent(
			JList<? extends SecondChatThread> list, SecondChatThread value, int position,
			boolean selected, boolean hasFocus) {
		// TODO Auto-generated method stub
		//createView
		
		if(selected)
		{
			this.setBackground(selectedC);
			centerPan.setBackground(selectedC);
			rightPane.setBackground(selectedC);
			//color=selectedC;
			//name.setForeground(new Color(100,100,100));
			//repaint();
		}else{
			///color=defaultc;
			this.setBackground(defaultc);
			centerPan.setBackground(defaultc);
			rightPane.setBackground(defaultc);
			//name.setForeground(new Color(2,2,2));
			//repaint();
		}
		if(!value.isNew()){
			
			if(!value.getLastSender().equals(self.getLogin())){
				if(value.getUnread()>0)
				{
					lastMessage.setFont(unreadFont);
					lastActiveTime.setFont(unreadFont);
					name.setFont(unreadFont);
					unread.setText(value.getUnread()+"");
				}
				else
				{
					unread.setText("");
					lastMessage.setFont(readFont);
					lastActiveTime.setFont(readFont);
					name.setFont(readFont);
				}
			}
			else{
				unread.setText("");
				if(value.getUnread()>0)
				{
					lastMessage.setFont(unreadFont);
					lastActiveTime.setFont(unreadFont);
					name.setFont(unreadFont);
				}
				else
				{
					lastMessage.setFont(readFont);
					lastActiveTime.setFont(readFont);
					name.setFont(readFont);
				}
			}
			lastActiveTime.setText(value.getLastActiveTime());
			lastMessage.setText(value.getLastMessage());
		}else{
			lastActiveTime.setText("");
			lastMessage.setText("");
		}
		onLine.setVisible(value.isOnline());
		name.setText(value.getOther());
		ImageIcon icon;
		String p=value.getProfil();
		if(p!=null && !p.isEmpty()){
			icon=new ImageIcon(p);
		}else{
			icon=new ImageIcon(SecondFenetre.class.getResource("/images/mc_user.png"));			
		}
		icon=Main.getScaledImage(icon, 32, 32);
		userProfil.setIcon(icon);
		return this;
	}
	private void createView() {
		// TODO Auto-generated method stub
		name=new JXLabel();
		userProfil=new JXLabel();
		unread=new JXLabel();
		lastMessage=new JXLabel();
		lastActiveTime=new JXLabel();
		onLine=new JXLabel("Online");
		name.setOpaque(true);
		onLine.setOpaque(true);
		userProfil.setOpaque(true);
		unread.setOpaque(true);
		lastMessage.setOpaque(true);
		lastActiveTime.setOpaque(true);
		unread.setForeground(new Color(180, 1, 1));
		onLine.setForeground(new Color(10,250,40));
		this.setLayout(new BorderLayout());
		centerPan=new JXPanel(new BorderLayout());
		rightPane=new JXPanel(new BorderLayout());
		centerPan.setOpaque(true);
		rightPane.setOpaque(true);
		centerPan.add(name,BorderLayout.NORTH);
		centerPan.add(lastMessage,BorderLayout.CENTER);
		centerPan.add(lastActiveTime,BorderLayout.SOUTH);
		centerPan.setBackground(null);
		rightPane.setBackground(null);
		rightPane.add(unread,BorderLayout.NORTH);
		rightPane.add(onLine,BorderLayout.SOUTH);
		this.add(userProfil,BorderLayout.WEST);
		this.add(centerPan,BorderLayout.CENTER);
		this.add(rightPane,BorderLayout.EAST);
		this.setBorder(BorderFactory.createLineBorder(new Color(100,100,100), 1));
		name.setBackground(null);
		userProfil.setBackground(null);
		unread.setBackground(null);
		lastActiveTime.setBackground(null);
		lastMessage.setBackground(null);
		this.setOpaque(true);
	}
//	@Override
//	public void paintComponents(Graphics g) {
//		// TODO Auto-generated method stub
//		g.setColor(color);
//		g.fillRect(0, 0, getWidth(), getHeight());
//		super.paintComponents(g);
//	}
}
