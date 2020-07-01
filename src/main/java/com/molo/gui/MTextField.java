package com.molo.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.FocusManager;
import javax.swing.JTextField;
import javax.swing.text.Document;

public class MTextField extends JTextField {
	private String mPlaceHolder;
	public MTextField() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MTextField(Document arg0, String arg1, int arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public MTextField(int arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public MTextField(String arg0, int arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public MTextField(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	public String getPlaceHolder(){
		return mPlaceHolder;
	}
	@Override
	protected void paintComponent(Graphics g) {
		if(!getText().isEmpty()){
			super.paintComponent(g);
		}else{
			if(!(FocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this)){
				Graphics2D g2 = (Graphics2D)g.create();
		        g2.setBackground(Color.gray);
		        g2.setFont(getFont().deriveFont(Font.ITALIC));
		        g2.drawString(mPlaceHolder, 5, 10); //figure out x, y from font's FontMetrics and size of component.
		        g2.dispose();
			}
		}
	}
	public void setPlaceHolder(String ph){
		mPlaceHolder=ph;
	}
}
