package com.molo;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.molo.gui.SecondFenetre;


public class Main {
	
	public static final int VOIP_PORT = 7082;
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
		//Main.class.getResource("").
		//config();
		//Fenetre fen=new Fenetre("Chat","localhost",7081);
		SecondFenetre fen=new SecondFenetre("Chat");
	}
	public static ImageIcon getScaledImage(ImageIcon icon,int w,int h){
		Image image = icon.getImage(); // transform it 
		Image newimg = image.getScaledInstance(w, h,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		return new ImageIcon(newimg);  // transform it back
	}
	
}
