package com.molo.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
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
import org.jdesktop.swingx.prompt.PromptSupport;

public class RegisterDialog extends JDialog {
	private JXTextField login=new JXTextField();
	private JXTextField nom=new JXTextField();
	private JXTextField prenom=new JXTextField();
	private JPasswordField password=new JPasswordField();
	private JPasswordField passwordConfirm=new JPasswordField();
	public static final String PASSWORD="password",
							   PASSWORD_C="passwdord_c",
							   LOGIN="login",
							   NOM="nom",
							   PRENOM="prenom";
	private JXPanel contentPanel=new JXPanel();
	private OnCloseInput closeListener=null;
	public RegisterDialog(JFrame parent,OnCloseInput l) {
		super(parent);
		
		this.setTitle("Login to the serveur");
		this.setModal(true);
		PromptSupport.setPrompt("Name", nom);
		PromptSupport.setPrompt("First Name", prenom);
		PromptSupport.setPrompt("Login", login);
		PromptSupport.setPrompt("Password", password);
		PromptSupport.setPrompt("Confirm password", passwordConfirm);
		contentPanel.setLayout(new GridLayout(5,2));
		contentPanel.add(new JLabel("Login"));
		contentPanel.add(login);
		contentPanel.add(new JLabel("Password"));
		contentPanel.add(password);
		contentPanel.add(new JXLabel("Confirm password"));
		contentPanel.add(passwordConfirm);
		contentPanel.add(new JXLabel("Name"));
		contentPanel.add(nom);
		contentPanel.add(new JXLabel("First Name"));
		contentPanel.add(prenom);
		closeListener=l;
		this.add(contentPanel);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		//this.setMinimumSize(new Dimension(200,200));
		this.setSize(new Dimension(300,200));
		this.setResizable(false);
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
		//this.setLocationByPlatform(true);
	}
	public static interface OnCloseInput{
		public abstract void onClose(Map<String, Object> value,final Dialog d);
	}
	public void setCloseListener(OnCloseInput l) {
		this.closeListener=l;
	}
	private void close(){
		Map<String, Object> v=new HashMap<>();
		v.put(LOGIN, login.getText());
		//System.out.println(v.get(LOGIN));
		v.put(PASSWORD, new String(password.getPassword()));
		v.put(PASSWORD_C, new String(passwordConfirm.getPassword()));
		v.put(NOM, nom.getText());
		v.put(PRENOM, nom.getText());
		if(closeListener!=null) closeListener.onClose(v,this);
	}
}
