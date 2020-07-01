package com.molo.net;

import java.net.Socket;

import com.molo.message.ChatMessage;

public interface SecondClientProcessListener {
	public void onSocketExceptionWhenRead(Socket s);
	public void onSocketExceptionWhenWrite(Socket s);
	public void onIOExceptionWhenRead(Socket s);
	public void onIOExceptionWhenWrite(Socket s);
	public void onReceiveNewMessage(Response message);
	public void onWrite(Socket socket);
}
