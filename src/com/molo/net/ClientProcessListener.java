package com.molo.net;


import java.net.Socket;


public interface ClientProcessListener {
	public void onSocketExceptionWhenRead(Socket s);
	public void onSocketExceptionWhenWrite(Socket s);
	public void onIOExceptionWhenRead(Socket s);
	public void onIOExceptionWhenWrite(Socket s);
	public void onReceiveNewMessage(Response message);
	public void onWrite(Socket socket);
	public void onSocketOpened(Socket socket);
	public void onSocketOpenedFail();
}
