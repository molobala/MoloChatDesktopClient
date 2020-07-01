package com.molo.audio;

import com.molo.audio.AudioPlayer.AudioClip;

public class AudioController extends Thread{
	AudioClip clip;
	AudioPlayListener listener;
	boolean playOnRun;
	public AudioController(AudioClip clip,AudioPlayListener listener,boolean playOnRun) {
		// TODO Auto-generated constructor stub
		this.listener=listener;
		this.clip=clip;
		this.playOnRun=playOnRun;
	}
	@Override
	public void run() {
		System.out.println("Runn");
		if(playOnRun)
			clip.play();
		while(clip.clip.isRunning() || clip.clip.isActive()){
			//System.out.println("Running....");
			if(listener!=null){
				listener.onTimer(clip.clip.getMicrosecondPosition());
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
