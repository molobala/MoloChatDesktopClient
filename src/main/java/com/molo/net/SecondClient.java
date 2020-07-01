package com.molo.net;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.molo.message.ChatMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class SecondClient {
	private Thread readThread,writThread;
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	private boolean go=true;
	private ClientProcessListener listener=null;
	private ObjectMapper mapper=new ObjectMapper();
	public SecondClient(String add,int port,ClientProcessListener l) throws UnknownHostException,ConnectException,IOException{
		// TODO Auto-generated constructor stub
		//InetAddress address;
		listener=l;
		open(add, port);
		//run();
	}
	public SecondClient(ClientProcessListener l) {
		// TODO Auto-generated constructor stub
		listener=l;
	}
	public void open(String address,int port) throws ConnectException, UnknownHostException,IOException{
		try {
			//address = InetAddress.getLocalHost();
			socket=new Socket(address, port);
			reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer=new PrintWriter(socket.getOutputStream());
			//System.out.println(reader.readLine());
//			writer.println("Bonjour");
//			writer.flush();
//			System.out.println(reader.readLine());
		}catch(ConnectException e){
			throw e;
		}
		catch (UnknownHostException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
		readThread=new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(go){
					String line;
					try {
						line = reader.readLine();
						if(line==null)
							break;
						ChatMessage msg=new ChatMessage();
						msg.fromJsonString(line);
						JsonNode result=mapper.readTree(line);
						Response r=new Response(result.get("hash").asLong(),result.get("status").asInt(), result.get("data"));
						System.out.println("Result: "+result.toString());
						if(listener!=null)
							listener.onReceiveNewMessage(r);
					}catch(SocketException e){
						if(listener!=null)
							listener.onSocketExceptionWhenRead(socket);
					}
					catch (IOException e) {
						if(listener!=null)
							listener.onIOExceptionWhenRead(socket);
					}
					try {
						Thread.sleep(40);
					} catch (InterruptedException e) {
						// TODO: handle exception
						readThread.interrupt();
					}
				}
			}
		});
		writThread=new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Scanner sc=new Scanner(System.in);
				while(go){
					if(go){						
						String line=sc.nextLine();
						writer.println(line);
						writer.flush();
						if(listener!=null)
							listener.onWrite(socket);
					}
					try {
						Thread.sleep(40);
					} catch (InterruptedException e) {
						// TODO: handle exception
						writThread.interrupt();
					}
				}
				sc.close();
			}
		});
	}
	public void runReading(){
		readThread.start();
	}
	public void runWriting(){
		writThread.start();
	}
	public void close() {
		// TODO Auto-generated method stub
		go=false;
		if(readThread!=null && readThread.isAlive()) readThread.interrupt();
		if(readThread!=null && writThread.isAlive()) writThread.interrupt();
		try {
			if(socket!=null) socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void writeToSocket(String line){
		writer.println(line);
		writer.flush();
		if(listener!=null)
			listener.onWrite(socket);
	}
	public void stopReading() {
		// TODO Auto-generated method stub
		readThread.interrupt();
	}
	public void setClientProcessListener(ClientProcessListener l){
		this.listener=l;
	}
}
