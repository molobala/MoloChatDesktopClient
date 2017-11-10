package com.molo.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JProgressBar;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

public class MProgressDialog extends JDialog{
	private JProgressBar progress=new JProgressBar();
	private JXLabel message=new JXLabel();
	public MProgressDialog() {
		super();
		// TODO Auto-generated constructor stub
		init();
		
	}

	public MProgressDialog(Frame owner, String message) {
		super(owner, message);
		// TODO Auto-generated constructor stub
		init();
		this.message.setText(message);
	}

	public MProgressDialog(Frame owner) {
		super(owner);
		// TODO Auto-generated constructor stub
		init();
	}
	
	public MProgressDialog(String message) {
		// TODO Auto-generated constructor stub
		super();
		init();
		this.message.setText(message);
	}

	private void init() {
		// TODO Auto-generated method stub
		this.progress.setIndeterminate(true);
		this.setMaximumSize(getParent().getSize());
		this.setMinimumSize(new Dimension(100,40));
		this.setLocationRelativeTo(getParent());
		this.setUndecorated(true);
		setModalityType(ModalityType.DOCUMENT_MODAL);
		JXPanel pane=new JXPanel(new BorderLayout());
		pane.add(progress,BorderLayout.WEST);
		pane.add(message,BorderLayout.CENTER);
		getContentPane().add(pane);
	}
	public void setMessage(String message){
		this.message.setText(message);
	}
	public String getMessage(){
		return message.getText();
	}
}
