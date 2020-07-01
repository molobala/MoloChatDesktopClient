package com.molo.audio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer {
	private static Map<String,AudioClip> clips=new HashMap<String, AudioClip>();
	public static StreamClip createStream(InputStream is,AudioFormat format){
		try {
			
			DataLine.Info info=new DataLine.Info(SourceDataLine.class, format);
			//audioInputStream=;
			SourceDataLine c = (SourceDataLine) AudioSystem.getLine(info);
			c.open(format);
			StreamClip clip=new StreamClip(c, is,format, null);
			return clip;
		} catch(Exception ex) {
			System.out.println("Error with playing sound.");
			ex.printStackTrace();
		}
		return null;
	}
	public static AudioClip play(String fname,LineListener l){
		AudioClip clip=clips.get(fname);
		if(clip==null){
			try {
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(fname).getAbsoluteFile());
				DataLine.Info info=new DataLine.Info(Clip.class, VoiceRecorder.getAudioFormat(16000.f));
				Clip c = (Clip) AudioSystem.getLine(info);
				//c.addLineListener(l);
				c.open(audioInputStream);
				clip=new AudioClip(c, l);
				clips.put(fname,clip);
				clip.play();
			} catch(Exception ex) {
				System.out.println("Error with playing sound.");
				ex.printStackTrace();
			}
		}else{
			
			clip.play();
		}
		return clip;
	}
	public static void pauseAudio(String fname){
		AudioClip clip=clips.get(fname);
		if(clip!=null){
			clip.stop();
		}
	}
	public static AudioClip createClip(URL fname, LineListener lineListener){
		AudioClip clip=clips.get(fname);
		if(clip!=null)
			return clip;
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fname);
			DataLine.Info info=new DataLine.Info(Clip.class, VoiceRecorder.getAudioFormat(16000));
			Clip c = (Clip) AudioSystem.getLine(info);
			c.open(audioInputStream);
			clip=new AudioClip(c, lineListener);
			clips.put(fname.getFile(),clip);
			return clip;
		} catch(Exception ex) {
			System.out.println("Error with playing sound.");
			ex.printStackTrace();
			return null;
		}
	}
	public static AudioClip createClip(String fname, LineListener lineListener){
		AudioClip clip=clips.get(fname);
		if(clip!=null)
			return clip;
		 try {
	        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(fname).getAbsoluteFile());
	        DataLine.Info info=new DataLine.Info(Clip.class, VoiceRecorder.getAudioFormat(16000.f));
			Clip c = (Clip) AudioSystem.getLine(info);
	        c.open(audioInputStream);
			clip=new AudioClip(c, lineListener);
			clips.put(fname,clip);
	        return clip;
	    } catch(Exception ex) {
	        System.out.println("Error with playing sound.");
	        ex.printStackTrace();
	        return null;
	    }
	}
	public static void releaseAll(){
		for(String s:clips.keySet()){
			clips.get(s).close();
		}
	}
	public static AudioClip get(String localPath) {
		// TODO Auto-generated method stub
		return clips.get(localPath);
	}
	public static void resetAudio(String localPath) {
		// TODO Auto-generated method stub
		AudioClip c=clips.get(localPath);
		if(c!=null){
			c.stop();
			//c.reset();
		}
	}
	public static class StreamClip extends Thread{
		public SourceDataLine line;
		public InputStream inputStream;
		AudioFormat format;
		public StreamClip(SourceDataLine c,InputStream is,AudioFormat f, LineListener l) {
			// TODO Auto-generated constructor stub
			this.line=c;
			this.line.addLineListener(l);
			this.inputStream=is;
			format=f;
		}
		public void play(){
			line.start();
			this.start();
		}
		public void pause(){
			if(line!=null)
			{
				line.stop();
				line.drain();
				line.flush();
				line.close();
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		public void arret(){
			pause();
		}
		public void close(){
			line.close();
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//super.run();
			byte[] buff=new byte[(int) (format.getSampleRate()*format.getFrameSize())];
			while(true){
				try {
					int r=inputStream.read(buff,0,buff.length);
					if(r<=0)
						break;
					InputStream byteArrayInputStream = new ByteArrayInputStream(buff);
					AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream,format,buff.length);
					playFrames(audioInputStream);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					break;
				}
			}
			line.drain();
		}
		synchronized private void playFrames(AudioInputStream audioInputStream) throws IOException {
			// TODO Auto-generated method stub
			byte[] buff=new byte[512];
			int r=0;
			while((r=audioInputStream.read(buff))>0){
				line.write(buff, 0,r);
			}
			line.flush();				
			line.drain();
		}
	}
	public static class AudioClip{
		public Clip clip;
		Thread playThread;
		public AudioClip(Clip c,LineListener l) {
			// TODO Auto-generated constructor stub
			this.clip=c;
			this.clip.addLineListener(l);
		}
		public void play(){
			playThread=new Thread(()->{
				if(clip!=null)
					clip.start();				
			});
			playThread.start();
			
		}
		public void pause(){
			if(clip!=null)
			{
				if(clip.isRunning() || clip.isActive()){
					clip.stop();
					clip.flush();
				}
				if(playThread!=null && playThread.isAlive()){
					playThread.interrupt();
					playThread=null;
				}
			}
		}
		public void reset(){
			if(clip!=null){
				clip.setFramePosition(0);
			}
		}
		public void stop(){
			pause();
			reset();
		}
		public void close(){
			clip.close();
		}
		public void play(boolean b) {
			// TODO Auto-generated method stub
			if(b)
				clip.loop(Clip.LOOP_CONTINUOUSLY);
			play();
		}
		public void replay() {
			// TODO Auto-generated method stub
			stop();
			reset();
			play();
		}
		public void replay(boolean b) {
			// TODO Auto-generated method stub
			stop();
			reset();
			play(b);
		}
	}
}
