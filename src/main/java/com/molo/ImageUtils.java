package com.molo;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

public class ImageUtils {
	public static ImageIcon getScaledImage(ImageIcon icon,int w,int h){
		Image image = icon.getImage(); // transform it 
		Image newimg = image.getScaledInstance(w, h,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		return new ImageIcon(newimg);  // transform it back
	}
	public static ImageIcon getScaledImage(String f,int w,int h){
		ImageIcon icon=new ImageIcon(f);
		Image image = icon.getImage(); // transform it 
		Image newimg = image.getScaledInstance(w, h,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		return new ImageIcon(newimg);  // transform it back
	}
	public static ImageIcon getScaledImage(URL f,int w,int h){
		ImageIcon icon=new ImageIcon(f);
		Image image = icon.getImage(); // transform it 
		Image newimg = image.getScaledInstance(w, h,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		return new ImageIcon(newimg);  // transform it back
	}
}
