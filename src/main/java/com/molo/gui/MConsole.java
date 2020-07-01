package com.molo.gui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.text.Document;

import org.jdesktop.swingx.JXTextArea;

public class MConsole extends JXTextArea {
	public MConsole() {
		super();
		// TODO Auto-generated constructor stub
		onCreate();
	}

	public MConsole(String string) {
		// TODO Auto-generated constructor stub
		super();
		onCreate();
	}
	private void onCreate(){
		this.setEditable(false);
		this.setBorder(null);
		this.setMinimumSize(new Dimension(this.getWidth(),100));
		this.setForeground(Color.WHITE);
		this.setBackground(Color.BLACK);
		this.setOpaque(true);
	}
	public void log(String str){
		append(str);
	}
	public void println(String str){
		append(str+"\n");
	}
	public void print(String str){
		log(str);
	}

}
