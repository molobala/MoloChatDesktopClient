package com.molo.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.sound.sampled.Clip;

import org.jdesktop.swingx.JXPanel;

import com.molo.audio.AudioPlayer;
import com.molo.audio.AudioPlayer.AudioClip;

public class AudioView extends JXPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1314582919798168114L;
	private AudioClip clip;
	private boolean isPlaying=false;
	public AudioView(AudioClip c,boolean isPlaying){
		clip=c;
		this.isPlaying=isPlaying;
	}
	public void setClip(AudioClip c){
		clip=c;
	}
	@Override
	protected void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		super.paintComponent(g);
		Graphics2D g2d=(Graphics2D)g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if(!isPlaying)
		{
			g2d.setColor(new Color(200,10,10));
			g2d.setBackground(new Color(200,10,10));
		}else{
			g2d.setColor(new Color(120,10,10));
			g2d.setBackground(new Color(120,10,10));
		}
		g2d.fillOval(getX(), getY(), getWidth(), getHeight());
		String displayText="Play";
		if(clip!=null){
			if(isPlaying){
				displayText=""+clip.clip.getMicrosecondPosition()/1000000;
			}
		}
		g2d.setColor(Color.black);
		FontMetrics metrics = g2d.getFontMetrics();
		int textWidth=metrics.stringWidth(""+displayText);
		g2d.drawString(displayText,getX()+(getWidth()-textWidth)/2,getY()+(getHeight()-metrics.getHeight())/2+ metrics.getAscent());
		g2d.dispose();
	}
}
