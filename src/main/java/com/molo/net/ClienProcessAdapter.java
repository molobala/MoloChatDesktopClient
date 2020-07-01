package com.molo.net;

import java.net.Socket;

/**
 * Created by Molobala on 29/03/2017.
 */

public class ClienProcessAdapter implements ClientProcessListener {
    @Override
    public void onSocketOpened(Socket socket) {

    }

    @Override
    public void onSocketExceptionWhenRead(Socket s) {

    }

    @Override
    public void onSocketExceptionWhenWrite(Socket s) {

    }

    @Override
    public void onIOExceptionWhenRead(Socket s) {

    }

    @Override
    public void onIOExceptionWhenWrite(Socket s) {

    }

    @Override
    public void onReceiveNewMessage(Response message) {

    }

    @Override
    public void onWrite(Socket socket) {

    }

	@Override
	public void onSocketOpenedFail() {
		// TODO Auto-generated method stub
		
	}
}
