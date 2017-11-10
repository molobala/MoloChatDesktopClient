package com.molo.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import com.molo.Main;
import com.molo.Utils;
import com.molo.entity.Membre;

public class MemberListCellRender extends JXPanel implements ListCellRenderer<Membre>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8012937026308303368L;
	JXLabel userProfil,name,onLine;
	JXPanel centerPan,rightPane;
	Color selectedC=new Color(200,150,0),defaultc=new Color(200,200,200),color;	
	public MemberListCellRender() {
		// TODO Auto-generated constructor stub
		this.setOpaque(true);
		createView();
		//unreadFont=new Font(lastMessage.getFont().getName(), Font.BOLD, 14);
		//readFont=lastMessage.getFont();
	}
	@Override
	public Component getListCellRendererComponent(
			JList<? extends Membre> list, Membre value, int position,
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
		onLine.setVisible(value.isOnline());
		String n=value.getLogin();
		String nom=value.getNom(),prenom=value.getPrenom();
		if(nom!=null){
			n=nom;
			if(prenom!=null && !prenom.isEmpty())
				n+=" "+prenom;
		}else if(prenom!=null && !prenom.isEmpty()){
			n=prenom;
		}
		name.setText(n);
		ImageIcon icon;
		String p=value.getProfil();
		if(p!=null && !p.isEmpty() && Utils.existInLocal(p)){
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
		onLine=new JXLabel("Online");
		name.setOpaque(true);
		onLine.setOpaque(true);
		userProfil.setOpaque(true);
		onLine.setForeground(new Color(10,250,40));
		this.setLayout(new BorderLayout());
		centerPan=new JXPanel(new BorderLayout());
		rightPane=new JXPanel(new BorderLayout());
		centerPan.setOpaque(true);
		rightPane.setOpaque(true);
		centerPan.add(name,BorderLayout.NORTH);
		centerPan.setBackground(null);
		rightPane.setBackground(null);
		rightPane.add(onLine,BorderLayout.SOUTH);
		this.add(userProfil,BorderLayout.WEST);
		this.add(centerPan,BorderLayout.CENTER);
		this.add(rightPane,BorderLayout.EAST);
		this.setBorder(BorderFactory.createLineBorder(new Color(100,100,100), 1));
		name.setBackground(null);
		userProfil.setBackground(null);
		this.setOpaque(true);
	}
}
