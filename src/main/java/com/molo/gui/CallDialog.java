package com.molo.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.DataInputStream;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import com.molo.Main;
import com.molo.Utils;
import com.molo.entity.ChatThread;
import com.molo.net.VoipClient;
import com.molo.net.VoipClient.VoipClientProcessListener;

public class CallDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1111523736364022288L;
	OnCloseListener listener;
	public static final int ENTERING_CALL=0,
			 				OUTGOING_CALL=1;
	public static final String CALL_TYPE="call_type";
	private int callType=ENTERING_CALL;
	JXButton accept,cancel;
	JXPanel mainPanel;
	JXLabel minuter=new JXLabel();
	com.molo.entity.ChatThread other;
	private boolean callAccepted=false;
	VoipClient client=null;
	private boolean isEstablished;
	//StreamClip player;
	public CallDialog(JFrame parent,com.molo.entity.ChatThread currentThread,int type,CallDialog.OnCloseListener l) {
		// TODO Auto-generated constructor stub
		super(parent);
		this.other=currentThread;
		callType=type;
		this.listener=l;
		this.setModalityType(ModalityType.DOCUMENT_MODAL);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setUndecorated(true);
		this.setMinimumSize(new Dimension(160,160));
		this.setPreferredSize(new Dimension(160,180));
		build0();
		if(type==ENTERING_CALL)
		{
			this.setTitle("Call From "+currentThread.getOther());
			accept.setVisible(true);
			cancel.setVisible(true);
		}
		else if(type==OUTGOING_CALL)
		{
			this.setTitle("Calling "+currentThread.getOther());
			cancel.setVisible(true);
			accept.setVisible(false);
			//build1();
		}
		if(callAccepted){
			minuter.setVisible(true);
		}else{
			minuter.setVisible(false);
		}
		accept.addActionListener((ev)->{
			callAccepted=true;
			minuter.setVisible(true);
			accept.setVisible(false);
			if(listener!=null){
				listener.onAccept(other);
			}
			
		});
		cancel.addActionListener((ev)->{
			callAccepted=false;
//			minuter.setVisible(false);
//			accept.setVisible(true);
//			cancel.setVisible(true);
			if(client!=null){
				client.disconnect();
			}
			if(listener!=null){
				listener.onCancel(other);
			}
			//close();
		});
		this.setLocationRelativeTo(parent);
	}
	public void openVoipSocket(int port,String adress,String label){
		System.out.println("Trying to open voip socket");
		client=new VoipClient(port, adress, label, new VoipClientProcessListener(){
			@Override
			public void onClose() {
				// TODO Auto-generated method stub
				super.onClose();
				System.out.println("client closed!!");
				close();
				//client.interrupt();
			}
			@Override
			public void connexionEstablished(DataInputStream reader) {
				// TODO Auto-generated method stub
				System.out.println("connexionEstablished ...");
				client.startRecord();
				minuter.setVisible(true);
//				if(player==null && reader!=null){
//					//data=new ByteArrayInputStream(b, 0, length);
//				    AudioFormat format = VoiceRecorder.getAudioFormat();
//					player=AudioPlayer.createStream(reader,format);
//					System.out.println("Player created now w'll play");
//					player.play(); 
//				}
				isEstablished=true;
				System.out.println("Connexion established");
			}
			@Override
			public void onTimer(long t) {
				// TODO Auto-generated method stub
				super.onTimer(t);
				//System.out.println("OnTimer "+t);
				minuter.setText(""+t/1000+" s");
			}
//			@Override
//			public void onNewData(byte[] b, int length) {
//				// TODO Auto-generated method stub
//				super.onNewData(b, length);
//			}
			@Override
			public void onFinish(){
				
			}
		});
		client.start();
	}
	public boolean isEstablished(){
		return isEstablished;
	}
	public void close() {
		// TODO Auto-generated method stub
//		if(client!=null && client.isAlive())
//			client.close();
		isEstablished=false;
		this.setVisible(false);
		System.gc();
		try{
			if(this.isActive())
				this.dispose();
		}catch(Exception e){}
	}
	private void build0() {
		// TODO Auto-generated method stub
		accept=new JXButton("Accept");
		cancel=new JXButton("Cancel");
		JXLabel other=new JXLabel(this.other.getOther());
		mainPanel=new JXPanel();
		String p=this.other.getProfil();
		other.setIcon(Main.getScaledImage(new ImageIcon((Utils.existInLocal(p))?p:CallDialog.class.getResource("/images/mc_user.png").getFile()),
				30, 30));
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(other,BorderLayout.CENTER);
		JXPanel btns=new JXPanel();
		btns.add(accept);
		btns.add(cancel);
		mainPanel.add(minuter,BorderLayout.NORTH);
		mainPanel.add(btns,BorderLayout.SOUTH);
		getContentPane().add(mainPanel);
	}
	public static interface OnCloseListener{
		public  void onCancel(ChatThread th);
		public void onAccept(ChatThread th);
	}
	
}
