package com.molo.audio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class VoiceRecorder extends Thread {
	public  long MAX_TIME=30000;
	private static final int BUFFER_SIZE = 4096;
	private long timer;
	private ByteArrayOutputStream recordBytes;
	File output;
	TargetDataLine line;
	AudioRecordListener listener;
	private boolean isRunning;
	AudioFormat format;
	private boolean useTimer=true,mayBeSaved=true;
	public VoiceRecorder(File output,AudioRecordListener l) {
		// TODO Auto-generated constructor stub
		this.output=output;
		this.listener=l;
	}
	public void setUserTimer(boolean b){
		useTimer=b;
	}
	public void setMayBeSaved(boolean b) {
		this.mayBeSaved=b;
	}
	@Override
	public void run() {
		try {
			starRecord();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public AudioRecordListener getListener() {
		return listener;
	}

	public void setListener(AudioRecordListener listener) {
		this.listener = listener;
	}
	private void starRecord() throws LineUnavailableException{
		System.out.println("StartRecording");
		//format=new  AudioFormat(16000, 8, 2, true, true);
		format=getAudioFormat(16000.0F);
		DataLine.Info info=new DataLine.Info(TargetDataLine.class, format);
		if( !AudioSystem.isLineSupported(info)){
			//
			this.interrupt();
			if(listener!=null)
				listener.onRecordFailed();
		}
		line = (TargetDataLine) AudioSystem.getLine(info);
		line.open(format);
		if(listener!=null)
			listener.onRecordStart();
		byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = 0;
        recordBytes = new ByteArrayOutputStream();
        isRunning = true;
        timer=0;
        final long startTime=System.currentTimeMillis();
        line.start();
        while (isRunning && (useTimer?timer<MAX_TIME:true)) {
        	 //System.out.println("Recording....");
            bytesRead = line.read(buffer, 0, buffer.length);
            if(mayBeSaved)
            	recordBytes.write(buffer, 0, bytesRead);
           // System.out.println("Recording....");
            if(listener!=null)
            	listener.onTimer(timer,buffer,bytesRead);
            timer=System.currentTimeMillis()-startTime;
        }
        finish();
	}
	public void stopRecording(){
		isRunning=false;
	}
	public static AudioFormat getAudioFormat(float sampleRate){
       // float sampleRate = 16000.0F;
        int sampleSizeBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeBits, channels, signed, bigEndian);
    }
	private void finish(){
		isRunning = false;
        if (line != null) {
            line.drain();
            line.close();
        }
        try {
        	if(output!=null)
        		saveFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(listener!=null)
			listener.onRecordFinish(output);
	}
	private void saveFile() throws IOException{
		 byte[] audioData = recordBytes.toByteArray();
	        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
	        AudioInputStream audioInputStream = new AudioInputStream(bais, format,
	                audioData.length / format.getFrameSize());
	        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, output);
	        audioInputStream.close();
	        recordBytes.close();
	}
}
