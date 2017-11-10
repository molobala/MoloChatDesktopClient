package com.molo.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.molo.audio.AudioRecordListener;
import com.molo.audio.VoiceRecorder;
import com.molo.message.MyStandarMessageType;

public class VoipClient extends Thread{
	private Socket voipServeur;
	public int PORT;
	public String ADRESS;
	private VoipClientProcessListener listener;
	DataInputStream reader;
	DataOutputStream writer;
	private String label;
	private ObjectMapper mapper;
	private boolean isConnected;
	VoiceRecorder recorder;
	
	private SourceDataLine line;
	private AudioFormat format=VoiceRecorder.getAudioFormat(16000.0F);
	public VoipClient(int port,String add,String label,VoipClientProcessListener l) {
		// TODO Auto-generated constructor stub
		ADRESS=add;
		PORT=port;
		this.label=label;
		listener=l;
		mapper=new ObjectMapper();
		recorder=new VoiceRecorder(null, new AudioRecordListener() {
			@Override
			public void onTimer(long t, byte[] data, int bytesRead) {
				// TODO Auto-generated method stub
				try {
					if(listener!=null)
						listener.onTimer(t);
					//System.out.println("recorder.onTimer "+t+" / "+bytesRead);
					writer.write(data, 0, bytesRead);
					writer.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					recorder.stopRecording();
				}
			}
			@Override
			public void onRecordStart() {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onRecordFinish(File f) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onRecordFailed() {
				// TODO Auto-generated method stub
			}
		});
		recorder.setUserTimer(false);
		recorder.setMayBeSaved(false);
		open();
	}
	public void setListener(VoipClientProcessListener l){
		this.listener=l;
	}
	public void startRecord(){
		recorder.start();
	}
	public void disconnect(){
		isConnected=false;
		try {
			voipServeur.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		interrupt();
		recorder.stopRecording();
	}
	@Override
	public void run() {
		 //authentification on server
		try {
			ObjectNode n=mapper.createObjectNode();
			n.put("label", this.label);
			System.out.println("Trying to establish peer to peer connection with voip server whith the label: "+label);
			writer.writeUTF(mapper.writeValueAsString(n));
			writer.flush();
			String l=reader.readUTF();
			JsonNode result=mapper.readTree(l);
			if(result.has("status") && result.get("status").asInt()==MyStandarMessageType.OK){
				System.out.println("peer to peer connexion established ");
				if(listener!=null)
					listener.connexionEstablished(reader);
				DataLine.Info info=new DataLine.Info(SourceDataLine.class, format);
				line=(SourceDataLine)AudioSystem.getLine(info);
				line.open(format);
				line.start();
				byte[] buff=new byte[(int) (format.getSampleRate()*format.getFrameSize())];
				while(voipServeur.isConnected() && isConnected){
					int r=reader.read(buff,0,buff.length);
					if(r<=0)
						break;
					InputStream byteArrayInputStream = new ByteArrayInputStream(buff);
					AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream,format,buff.length);
					playFrames(audioInputStream);
					Thread.sleep(20);
					//System.out.println("Thread alive");
				}
				line.flush();
				line.drain();
			}else{
				//fin
				close();
			
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			close();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			close();
		}
	}
	public void close() {
		// TODO Auto-generated method stub
		System.out.println("Fin d'appel");
		try {
			if(listener!=null)
				listener.onClose();
			if(voipServeur.isConnected()) voipServeur.close();
			reader.close();
			writer.close();
			//this.interrupt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	private void open() {
		// TODO Auto-generated method stub
		try {
			System.out.println("Opening a voip socket on "+ADRESS+":"+PORT);
			voipServeur=new Socket(ADRESS, PORT);
			reader=new DataInputStream(new BufferedInputStream(voipServeur.getInputStream()));
			writer=new DataOutputStream(new BufferedOutputStream(voipServeur.getOutputStream()));
			isConnected=true;
			System.out.println("Voip socket opened successfully");
			if(listener!=null){
				listener.onSocketOpened(voipServeur);
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(listener!=null){
				listener.onSocketOpenedFail();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(listener!=null){
				listener.onSocketOpenedFail();
			}
		}
	}
	public static class VoipClientProcessListener{
		    public void onSocketOpened(Socket socket) {

		    }

		    public void onSocketExceptionWhenRead(Socket s) {

		    }

		    public void onSocketExceptionWhenWrite(Socket s) {

		    }

		    public void onIOExceptionWhenRead(Socket s) {

		    }

		    public void onIOExceptionWhenWrite(Socket s) {

		    }

		    public void onNewData(byte[] data,int length) {

		    }
		    public void onWrite(Socket socket) {

		    }

			public void onSocketOpenedFail() {
				// TODO Auto-generated method stub
				
			}
			public void onClose(){
				
			}
			public void onTimer(long t){
				
			}

			public void connexionEstablished(DataInputStream reader) {
				// TODO Auto-generated method stub
				
			}

			public void onFinish() {
				// TODO Auto-generated method stub
				
			}
	}
	synchronized private void playFrames(AudioInputStream audioInputStream) throws IOException {
		// TODO Auto-generated method stub
		byte[] buff=new byte[(int) (format.getSampleRate()*(format.getFrameSize()/10))];
		int r=0;
		while((r=audioInputStream.read(buff))>0){
			line.write(buff, 0,r);
		}
		line.flush();				
		line.drain();
	}
}
