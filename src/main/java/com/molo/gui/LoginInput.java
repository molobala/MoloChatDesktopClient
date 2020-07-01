package com.molo.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;

public class LoginInput extends JDialog {
	private JXTextField login=new JXTextField();
	private JPasswordField password=new JPasswordField();
	public static final String PASSWORD="password",
							   LOGIN="login",
							   REMEMBER_ME="remember_me";
	private JXPanel contentPanel=new JXPanel();
	private JCheckBox remeberMe=new JCheckBox();
	private OnCloseInput closeListener=null;
	public LoginInput(JFrame parent,OnCloseInput l) {
		super(parent);
		this.setTitle("Authentification");
		this.setModalityType(ModalityType.DOCUMENT_MODAL);
		//this.setLocationRelativeTo(parent);
		contentPanel.setLayout(new GridLayout(3,2));
		contentPanel.add(new JLabel("Login"));
		contentPanel.add(login);
		contentPanel.add(new JLabel("Password"));
		contentPanel.add(password);
		contentPanel.add(new JXLabel("Remember me"));
		contentPanel.add(remeberMe);
		closeListener=l;
		this.add(contentPanel);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(new Dimension(200,120));
//		this.addWindowListener(new WindowAdapter() {
//			@Override
//			public void windowClosed(WindowEvent e) {
//				// TODO Auto-generated method stub
//				
//				//if(closeListener!=null) closeListener.onClose(login.getText(), password.getText());
//				//super.windowClosed(e);
//			}
//		});
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton quitter = new JButton("Ok");
				//cancelButton.setActionCommand(");
				buttonPane.add(quitter);
				quitter.addActionListener((e)->{
					close();
				});
			}
		}
		password.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar()=='\n'){
					close();
				}
			}
		});
		this.setLocationRelativeTo(parent);
	}
	public static interface OnCloseInput{
		public abstract void onClose(Map<String, Object> value,final JDialog d);
	}
	public void setCloseListener(OnCloseInput l) {
		this.closeListener=l;
	}
	private void close(){
		
		Map<String, Object> v=new HashMap<>();
		v.put(LOGIN, login.getText());
		v.put(PASSWORD, new String(password.getPassword()));
		v.put(REMEMBER_ME, remeberMe.isSelected());
		if(closeListener!=null) closeListener.onClose(v,this);
	}
}
