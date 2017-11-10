package com.molo.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

import org.jdesktop.swingx.JXImageView;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import com.molo.Main;
import com.molo.Utils;
import com.molo.audio.AudioPlayer;
import com.molo.entity.Attachment;
import com.molo.entity.ChatMessage;
import com.molo.entity.Membre;


public class SMChatMessageListCellRender extends JXPanel implements ListCellRenderer<ChatMessage>{
	private Membre self;
	private MLabel content=new MLabel(),selfContent=new MLabel();
	private JXPanel attachmentPanel=new JXPanel(),otherAttachmentPanel=new JXPanel();
	private JXImageView profil=new JXImageView();
	private MLabel seen=new MLabel("Seen");
	private Color otherColor=new Color(150,150,150),selfColor=new Color(12,80,100);
	private JXPanel selfPane=new JXPanel(new BorderLayout()),
			selfPaneCenter=new JXPanel(new BorderLayout()),
			otherPane=new JXPanel(new BorderLayout()),
			otherPaneCenter=new JXPanel(new BorderLayout());
	public SMChatMessageListCellRender(Membre log) {
		// TODO Auto-generated constructor stub
		this.self=log;
		//this.setBorder(new RoundedBorder(new Color(180,180,180), 20));
		System.out.println(self);
		this.setOpaque(true);
		this.setBackground(Color.white);
		seen.setOpaque(true);
		profil.setOpaque(true);
		content.setOpaque(true);
		selfContent.setOpaque(true);
		selfPane.setOpaque(true);
		otherPane.setOpaque(true);
		selfPaneCenter.setOpaque(true);
		otherPaneCenter.setOpaque(true);
		attachmentPanel.setOpaque(true);
		otherAttachmentPanel.setOpaque(true);
		attachmentPanel.setLayout(new BoxLayout(attachmentPanel, BoxLayout.PAGE_AXIS));
		otherAttachmentPanel.setLayout(new BoxLayout(otherAttachmentPanel, BoxLayout.PAGE_AXIS));
		//add content label at the center
		otherPaneCenter.add(content,BorderLayout.CENTER);
		otherPaneCenter.add(otherAttachmentPanel,BorderLayout.SOUTH);
		selfPaneCenter.add(selfContent,BorderLayout.CENTER);
		selfPaneCenter.add(attachmentPanel,BorderLayout.SOUTH);
		JXPanel seenPanel=new JXPanel(new BorderLayout());
		seenPanel.add(seen,BorderLayout.EAST);
		seenPanel.setBackground(getBackground());
		selfPane.add(seenPanel,BorderLayout.SOUTH);
		otherPane.add(otherPaneCenter,BorderLayout.CENTER);
		selfPane.add(selfPaneCenter,BorderLayout.CENTER);
		this.setLayout(new BorderLayout());
		this.add(otherPane,BorderLayout.WEST);
		this.add(selfPane,BorderLayout.EAST);
		selfContent.setFillColor(selfColor);
		selfContent.setForeground(Color.black);
		content.setFillColor(otherColor);
		content.setForeground(Color.black);
		selfContent.setBorder(new RoundedBorder(selfColor, 20));
		content.setBorder(new RoundedBorder(otherColor, 20));
	}
	@Override
	public Component getListCellRendererComponent(
			JList<? extends ChatMessage> list, ChatMessage value, int index,
			boolean isSelected, boolean cellHasFocus) {
		this.setOpaque(true);
		this.setMaximumSize(new Dimension((int)(list.getWidth()*0.8),Integer.MAX_VALUE));
		this.setMinimumSize(new Dimension(10,40));
		attachmentPanel.removeAll();
		otherAttachmentPanel.removeAll();
		if(value.getSender().equals(self.getLogin())){
			selfContent.setText(value.getContent());
			if(value.getContent().isEmpty()){
				selfContent.setVisible(false);
			}else selfContent.setVisible(true);
			selfPane.setVisible(true);
			otherPane.setVisible(false);
			//display attachment
			if(value.getAttachments()==null || (value.getAttachments()!=null && value.getAttachments().isEmpty())){
				attachmentPanel.setVisible(false);
			}else{
				attachmentPanel.setVisible(true);
				for(Attachment at:value.getAttachments()){
					String localPath= Utils.getLocalPath(at.getPath());
					if(at.getType().startsWith("image")){
						JXLabel label=new JXLabel();
						ImageIcon im;
						if(Utils.existInLocal(localPath)){
							im=new ImageIcon(localPath);
						}else{
							//im=new ImageIcon(this.getClass().getResource("/images/m_ic_image_up.png"));
							im=new ImageIcon(SecondFenetre.class.getResource("/images/m_ic_image.png"));
						}
						int w=Math.min(100, im.getIconWidth());
						int h=(im.getIconHeight()*w)/im.getIconWidth();
						label.setIcon(Main.getScaledImage(im, w, h));
						attachmentPanel.add(label);
					}else if(at.getType().startsWith("audio")){
						AudioView av=new AudioView(AudioPlayer.get(Utils.getLocalPath(at.getPath())),value.isPlaying());
						av.setMinimumSize(new Dimension(40,40));
						av.setPreferredSize(new Dimension(40,40));
						attachmentPanel.add(av);
					}else{
						JXLabel l=new JXLabel(""+new File(at.getPath()).getName());
						l.setIcon(Main.getScaledImage(new ImageIcon(SMChatMessageListCellRender.class.getResource("/images/m_ic_files.png"))
								,40,20));
						attachmentPanel.add(l);
					}
				}
			}
			if(index>=list.getModel().getSize()-1){
				if(!value.isSeen()){
					seen.setVisible(false);
				}else{
					seen.setVisible(true);
				}
			}else{
				seen.setVisible(false);
			}
		}else{
			content.setText(value.getContent());
			if(value.getContent().isEmpty()){
				content.setVisible(false);
			}else
				content.setVisible(true);
			selfPane.setVisible(false);
			otherPane.setVisible(true);
			//display attachemnt
			if(value.getAttachments()==null || (value.getAttachments()!=null && value.getAttachments().isEmpty())){
				otherAttachmentPanel.setVisible(false);
			}else{
				otherAttachmentPanel.setVisible(true);
				for(Attachment at:value.getAttachments()){
					String localPath= Utils.getLocalPath(at.getPath());
					if(at.getType().startsWith("image")){
						JXLabel label=new JXLabel();
						ImageIcon im;
						if(Utils.existInLocal(localPath)){
							im=new ImageIcon(localPath);
						}else{
							//im=new ImageIcon(this.getClass().getResource("/images/m_ic_image_up.png"));
							im=new ImageIcon(SecondFenetre.class.getResource("/images/m_ic_image.png"));
						}
						int w=Math.min(100, im.getIconWidth());
						int h=(im.getIconHeight()*w)/im.getIconWidth();
						label.setIcon(Main.getScaledImage(im, w, h));
						otherAttachmentPanel.add(label);
					}else if(at.getType().startsWith("audio")){
						AudioView av=new AudioView(AudioPlayer.get(Utils.getLocalPath(at.getPath())),value.isPlaying());
						av.setMinimumSize(new Dimension(40,40));
						av.setPreferredSize(new Dimension(40,40));
						otherAttachmentPanel.add(av);
					}else{
						JXLabel l=new JXLabel(""+new File(at.getPath()).getName());
						l.setIcon(Main.getScaledImage(new ImageIcon(SMChatMessageListCellRender.class.getResource("/images/m_ic_file.png"))
								,40,20));
						otherAttachmentPanel.add(l);
					}
				}
			}
		}
		return this;
	}
	public void setThis(Membre m) {
		// TODO Auto-generated method stub
		this.self=m;
	}
	class MLabel extends JXLabel{
		private Color bgColor;
		public MLabel() {
			super();
			// TODO Auto-generated constructor stub
		}

		public MLabel(Icon image, int horizontalAlignment) {
			super(image, horizontalAlignment);
			// TODO Auto-generated constructor stub
		}

		public MLabel(Icon image) {
			super(image);
			// TODO Auto-generated constructor stub
		}

		public MLabel(String text, Icon image, int horizontalAlignment) {
			super(text, image, horizontalAlignment);
			// TODO Auto-generated constructor stub
		}

		public MLabel(String text, int horizontalAlignment) {
			super(text, horizontalAlignment);
			// TODO Auto-generated constructor stub
		}

		public MLabel(String text) {
			super(text);
			// TODO Auto-generated constructor stub
		}
		public void setFillColor(Color bg) {
			// TODO Auto-generated method stub
			bgColor=bg;
			this.setBackground(null);
		}
		@Override
		public void paint(Graphics g) {
			// TODO Auto-generated method stub
			drawBg(g);
			super.paint(g);
			//super.paintComponent(g);
		}
		private void drawBg(Graphics g) {
			if(bgColor!=null){
				Border b=getBorder();
				Insets ins=b.getBorderInsets(this);
				Graphics2D g2d=(Graphics2D)g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setColor(bgColor);//g2d.setBackground(bgColor);
				g2d.fillRoundRect(this.getX()+1, this.getY()+1, this.getWidth() - 2, this.getHeight() - 2, ins.left*2, ins.left*2);
				g2d.dispose();
			}
			//super.paintComponent(g);
		}
	}
}
