package com.molo.audio;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class MicroPhone extends Thread {
	
	private static final int BUFFER_SIZE = 4096;
	OutputStream output;
	TargetDataLine line;
	private boolean isRunning;
	AudioFormat format;
	//private boolean useTimer=true;
	public MicroPhone(OutputStream output) {
		// TODO Auto-generated constructor stub
		this.output=output;
	}
	@Override
	public void run() {
		try {
			System.out.println("StartRecording");
			//format=new  AudioFormat(16000, 8, 2, true, true);
			format=getAudioFormat();
			DataLine.Info info=new DataLine.Info(TargetDataLine.class, format);
			if( !AudioSystem.isLineSupported(info)){
				this.interrupt();
				return;
			}
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);
			line.start();
			byte[] buffer = new byte[BUFFER_SIZE];
	        int bytesRead = 0;
	        isRunning = true;
	        while (isRunning) {
	        	 System.out.println("Recording....");
	            bytesRead = line.read(buffer, 0, buffer.length);
	            output.write(buffer, 0, bytesRead);
	        }
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			try {
				output.close();
			} catch (IOException e1) {}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			try {
				output.close();
			} catch (IOException e1) {}
		}
		finish();
	}

	public void stopRecording(){
		isRunning=false;
	}
	public static AudioFormat getAudioFormat(){
        float sampleRate = 16000.0F;
        int sampleSizeBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;

        return new AudioFormat(sampleRate, sampleSizeBits, channels, signed, bigEndian);
    }
	private void finish(){
		isRunning = false;
        if (line != null) {
            line.drain();
            line.close();
        }
	}
}
