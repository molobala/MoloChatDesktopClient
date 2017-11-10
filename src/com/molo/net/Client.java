package com.molo.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.molo.ftp.MFtp;


public class Client {
	public static final String TEMP_DIR="./temp";
	public static final String USERS_DATA_DIR="./data/users";
	public static final String USERS_FILE_DATA_DIR="./data/users/files";
	private Thread readThread,writThread;
	private Socket socket;
	private DataOutputStream writer;
	private DataInputStream reader;
	private boolean go=true;
	private ClientProcessListener listener=null;
	private boolean isAuthentificate=false;
	private String HOST;
	private int PORT;
    private MCQueryManager queryManager;
    ObjectMapper mapper= new ObjectMapper();
	public Client(String add, int port, ClientProcessListener l){
		// TODO Auto-generated constructor stub
		//InetAddress address;
		config();
		listener=l;
		PORT=port;
		HOST=add;
		//open(add, port);
		//run();
	}
	public Client() {
		// TODO Auto-generated constructor stub
		config();
	}
	public void setPort(int p){
		PORT=p;
	}
	public void setAddress(String ad){
		HOST=ad;
	}
	public void open(String address,int port){
		System.out.println(address+":"+port);
		try {
			socket=new Socket(address, port);
			reader=new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			writer=new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			if(listener!=null){
				listener.onSocketOpened(socket);
			}
		}catch(ConnectException e){
			e.printStackTrace();
			if(listener!=null)
				listener.onSocketOpenedFail();
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
			if(listener!=null)
				listener.onSocketOpenedFail();
		} catch (IOException e) {
			e.printStackTrace();
			if(listener!=null)
				listener.onSocketOpenedFail();
		}
		readThread=new ReadThread();
		writThread=new WriteThread();
	}
	public void runReading(){
		if(readThread==null) {
            readThread=new ReadThread();
            readThread.start();
        }
		else{
            if(readThread.getState()==Thread.State.NEW)
                readThread.start();
		}

	}

	public void close() {
		// TODO Auto-generated method stub
		go=false;
		if(reader!=null) {
//			try {
//				reader.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
		if(writer!=null) {
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
        if(writer==null){
            if(socket!=null && socket.isConnected()){
                try {
                	writer=new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                return;
            }
        }

		try {
			writer.writeUTF(line+"\n");
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(listener!=null)
			listener.onWrite(socket);
	}
	public void stopReading() {
		// TODO Auto-generated method stub
		if(readThread!=null && readThread.isAlive()){
			readThread.interrupt();
			readThread=null;
		}
		//readThread=null;
	}

	public boolean isAuthentificate() {
		return isAuthentificate;
	}

	public void setAuthentificate(boolean authentificate) {
		isAuthentificate = authentificate;
	}

	public void setClientProcessListener(ClientProcessListener l){
		this.listener=l;
	}

	public void startReading() {
		runReading();
	}

	public void tryToConnect() {
		open(HOST, PORT);
	}

	public boolean isConnected() {
		return socket!=null && socket.isConnected();
	}

    public void registerManager(MCQueryManager man) {
        this.queryManager=man;
    }

    private class ReadThread extends Thread{
		@Override
		public void run() {
			if(reader==null){
				try {
                    System.out.println("Openning");
                    reader=new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			while(go){
				String line;
				try {
					line = readLine(reader);
					if(line==null)
						break;
					JsonNode result=mapper.readTree(line);
					System.out.println(line);
					if(!result.has("hash") || !result.has("status"))
						continue;
					Response r=new Response(result.get("hash").asLong(),result.get("status").asInt(), result.get("data"));
					//System.out.println("Result: "+result.toString());
					MediaFile[] medias=null;
					if(result.has("files")){
						medias= mapper.readValue(result.get("files"),MediaFile[].class);
						if(medias!=null){
							System.out.println("Has Media : "+medias.length);
							for(MediaFile m:medias){
								File f=MFtp.ftpGetFile(reader,TEMP_DIR);
								m.absolutPath=f.getAbsolutePath();
							}
						}
					}
					r.medias=medias;
                    if(queryManager!=null && r.hash>0){
                        queryManager.onResult(r);
                    }
					else if(listener!=null && r.hash<=0)
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
					//readThread.interrupt();
                    break;
				}
			}

		}
	}
    private String readLine(DataInputStream reader2) throws SocketTimeoutException,IOException,SocketException{
		String l="";
		try {
			l=reader2.readUTF();
		}
		catch(SocketException e){
			throw e;
		}
		catch(SocketTimeoutException e){
			throw e;
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}
		return l;
	}
	private  class WriteThread extends Thread{
		@Override
		public void run() {
			Scanner sc=new Scanner(System.in);
			while(go){
				if(go){
					String line=sc.nextLine();
					try {
						writer.writeUTF(line+"\n");
						writer.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
		}
	}
	public String address() {
		// TODO Auto-generated method stub
		return HOST;
	}
	public int getPort(){
		return PORT;
	}
	public DataOutputStream getOut() {
		// TODO Auto-generated method stub
		return writer;
	}
	public DataInputStream getIn(){
		return reader;
	}
	private static void config() {
		// TODO Auto-generated method stub
		File tempDir=new File(TEMP_DIR);
		if(!tempDir.isDirectory()){
			tempDir.mkdir();
		}
		tempDir=new File(USERS_DATA_DIR);
		if(!tempDir.isDirectory()){
			tempDir.mkdirs();
		}
		tempDir=new File(USERS_FILE_DATA_DIR);
		if(!tempDir.isDirectory()){
			tempDir.mkdirs();
		}
		
	}

	
}
