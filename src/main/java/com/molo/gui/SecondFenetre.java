package com.molo.gui;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.molo.ImageUtils;
import com.molo.Main;
import com.molo.Utils;
import com.molo.audio.*;
import com.molo.audio.AudioPlayer.AudioClip;
import com.molo.entity.Attachment;
import com.molo.entity.ChatMessage;
import com.molo.entity.ChatThread;
import com.molo.entity.Membre;
import com.molo.ftp.MFtp;
import com.molo.message.MyStandarMessageType;
import com.molo.net.*;
import com.molo.security.CSAREncryption;
import com.molo.security.RSAEncryption;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.prompt.PromptSupport;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;
import java.util.List;


public class SecondFenetre extends JFrame implements OnNewMessageListener, OnMemberConnectionListener, CallListener {
    SecondMListModel mMemberList = new SecondMListModel();
    Vector<SecondChatThread> mChatThreadList = new Vector<>();
    SecondChatThread currentThread = null;
    private JXPanel mainPanel = new JXPanel();
    private JXLabel mLoginLabel = new JXLabel("");
    private JLabel mCurrentChatLogin = new JLabel("");
    private JList<Membre> mMemberListView = new JList<Membre>(mMemberList);
    private JList<SecondChatThread> mChatThreadListView = new JList<SecondChatThread>(mChatThreadList);
    private JList<ChatMessage> mMessages = new JList<ChatMessage>();
    private JTextField mInput = new JTextField("");
    private MConsole console = new MConsole("");
    private JScrollPane consoleScroll;
    private JXButton mConnectButton = new JXButton("Connect to server"),
            mRegisterButton = new JXButton("Register now"), mEditLogin = new JXButton(new ImageIcon(SecondFenetre.class.getResource("/images/edit-16.png")));
    private JXButton callBtn = new JXButton((ImageUtils.getScaledImage((SecondFenetre.class.getResource("/images/m_ic_call.png")), 32, 32)));
    private JTextField mServeurAddressInput;
    private JXButton chooseImage = new JXButton(ImageUtils.getScaledImage(new ImageIcon(SecondFenetre.class.getResource("/images/m_ic_image.png")), 32, 32));
    private JXButton chooseFiles = new JXButton((ImageUtils.getScaledImage(new ImageIcon(SecondFenetre.class.getResource("/images/m_ic_file.png")), 32, 32)));
    private JXButton voiceRecord = new JXButton((ImageUtils.getScaledImage(new ImageIcon(SecondFenetre.class.getResource("/images/m_ic_vocal.png")), 32, 32)));
    private JXLabel selfProfil = new JXLabel();
    private JXLabel recording = new JXLabel("Recording");

    private LoginInput authentificationInput;
    private RegisterDialog registerDialog;
    private MProgressDialog progress = new MProgressDialog("Wait please");
    private Client client;
    private boolean isConnectedToServer = false;
    private boolean isAuthentificated = false;
    private ClientProcessListener clientProcessListener;
    private static List<OnNewMessageListener> messageListeners = new ArrayList<>();
    private static List<OnMemberConnectionListener> membersConnectionListener = new ArrayList<>();
    private static List<CallListener> callListeners = new ArrayList<>();
    private List<File> filesToSend = new ArrayList<File>();
    private static long COMMANDE_ID = 100;
    private int THPAGE = 0, THPSIZE = 10;
    private ObjectMapper mapper = new ObjectMapper();
    private Membre mMemberObject = null;
    private JScrollPane messageScroll;
    private VoiceRecorder recorder;
    private AudioRecordListener recordListener;
    private CallDialog callDialog;
    private KeyPair rsaKeys = null;
    private AudioClip messageSentClip = AudioPlayer.createClip(SecondFenetre.class.getResource("/sound/send.wav"), null);
    private AudioClip messageReceivedClip = AudioPlayer.createClip(SecondFenetre.class.getResource("/sound/msg_receive.wav"), null);
    private AudioClip callFromClip = AudioPlayer.createClip(SecondFenetre.class.getResource("/sound/call.wav"), null);
    private AudioClip callClip = AudioPlayer.createClip(SecondFenetre.class.getResource("/sound/call_bg.wav"), null);
    protected ScrollListener messageScrollListener;
    public boolean isLoading = false;

    private PublicKey publicKey = null;
    private String csarKey = null;

    public SecondFenetre(String title) {
        // TODO Auto-generated constructor stub
        super(title);
        this.setSize(new Dimension(600, 480));
        try {
            this.setIconImage(ImageIO.read(this.getClass().getResource("/images/mc_logo_mini.png")));
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            //e1.printStackTrace();
        }
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.buildView();
        registerForMemberConnection(this);
        registerForNewMessageReception(this);
        registerCallListener(this);
//		try{
//			InputStream s1=new FileInputStream(Fenetre.class.getResource("/sound/send.mp3").getPath());
//			receiveMessage=new Player(s1);
//			sendMessage=receiveMessage;
//		}catch(IOException e){
//
//		}
//		catch(JavaLayerException e){
//
//		}
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        PromptSupport.setPrompt("Your message here", mInput);
        PromptSupport.setPrompt("Example localhost:7081", mServeurAddressInput);
        mEditLogin.setToolTipText("Change your login");
        mEditLogin.setEnabled(false);
        mMemberListView.setCellRenderer(new MemberListCellRender());
        //mMessages.setModel(messageList);
        rsaKeys = RSAEncryption.KeyManager.genKey(2048);
        clientProcessListener = new ClienProcessAdapter() {
            @Override
            public void onReceiveNewMessage(Response rep) {
                JsonNode data = (JsonNode) rep.data;
                switch (rep.status) {
                    case MyStandarMessageType.EXIT:
                        if (isAuthentificated) {
                            console.println("Disconnected from serveur");
                            //System.out.println(message.toString());
                            mConnectButton.setText("Authentificate");
                            mMemberList.clear();
                            mChatThreadList.clear();
                            isAuthentificated = false;
                            SwingUtilities.invokeLater(() -> {
                                mChatThreadListView.updateUI();
                                mMemberListView.updateUI();
                            });
                            mLoginLabel.setText("not connected");
                        } else {
                            client.stopReading();
                            mConnectButton.setText("Connect to Serveur");
                            mServeurAddressInput.setEnabled(true);
                            isConnectedToServer = false;
                        }

                        break;
                    case MyStandarMessageType.MESSAGE: {
                        ChatMessage m;
                        SecondChatThread th = null;
                        try {
                            m = mapper.readValue(data.toString(), ChatMessage.class);
                            //we retrieve correspondant from the members list
                            for (SecondChatThread it : mChatThreadList) {
                                System.out.println("Thread: id " + it.getId());
                                if (it.getId() == m.getThread()) {
                                    th = it;
                                    break;
                                }
                            }
                            if (th == null) {
                                //new thread
                                System.out.println("new Thread : " + m.getThread());
                                th = new SecondChatThread(m.getThread(), mMemberObject.getLogin(), m.getSender());

                                mChatThreadList.add(th);
                            } else {
//									th.setLastActiveTime(m.getDate());
//									th.setLastMessage(m.getContent());
//									th.setMessageCount(th.getMessageCount()+1);
//									th.setUnread(1);
                            }
                            if (th != null)
                                dispatchNewMessage(th, m);
                            SwingUtilities.invokeLater(() -> {
                                mChatThreadListView.updateUI();
                            });
                            //mCurrentChatLogin.setText(message.getSender());
                            console.println("New Message from " + m.getSender() + ": " + m.getContent());
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                    break;
                    case MyStandarMessageType.ON_MEMBER_DISCONNECTION: {
                        Membre m;
                        try {
                            m = mapper.readValue(data.toString(), Membre.class);
                            dispatchMemberDisconnection(m);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    break;
                    case MyStandarMessageType.ON_NEW_MEMBER_CONNECTION: {
                        Membre m;
                        try {
                            m = mapper.readValue(data.toString(), Membre.class);
                            dispatchMemberConnection(m);

                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    break;
                    case MyStandarMessageType.ON_MESSAGE_SEEN:
                        long thid = data.get("thread").asLong();
                        System.out.println("Message seen " + MyStandarMessageType.ON_MESSAGE_SEEN);
                        for (ChatThread t : mChatThreadList) {
                            if (t.getId() == thid) {
                                dispatchMessageSeen(t);
                                break;
                            }
                        }
                        break;
                    case MyStandarMessageType.MESSAGE_SENT:
                        System.out.println("Message sent " + MyStandarMessageType.ON_MESSAGE_SEEN);
                        ChatMessage m;
                        SecondChatThread th = null;
                        try {
                            m = mapper.readValue(data.toString(), ChatMessage.class);
                            //we retrieve correspondant from the members list
                            for (SecondChatThread it : mChatThreadList) {
                                //System.out.println("Thread: id "+it.getId());
                                if (it.getId() == m.getThread()) {
                                    th = it;
                                    break;
                                }
                            }

                            if (th != null)
                                dispatchMessageSent(th, m);
                        } catch (IOException e) {
                            // TODO: handle exception
                            e.printStackTrace();
                        }
                        break;
                    case MyStandarMessageType.CALL_ACCEPTED: {
                        String log = data.get("other").asText();
                        String label = data.get("label").asText();
                        com.molo.entity.ChatThread thr = null;
                        for (com.molo.entity.ChatThread it : mChatThreadList) {
                            if (it.getOther().equals(log)) {
                                thr = it;
                                break;
                            }
                        }
                        if (thr != null)
                            dispatchCallAcepted(thr, label);
                    }
                    break;
                    case MyStandarMessageType.CALL_CANCELED: {
                        String log = data.asText();
                        System.out.println("Call from " + log + " is cancelled");
                        com.molo.entity.ChatThread thr = null;
                        for (com.molo.entity.ChatThread it : mChatThreadList) {
                            if (it.getOther().equals(log)) {
                                thr = it;
                                break;
                            }
                        }
                        if (thr != null)
                            dispatchCallCanceled(thr);
                    }
                    break;
                    case MyStandarMessageType.CALL_FINISHED: {
                        String log = data.asText();
                        System.out.println("Call from " + log + " is finished");
                        com.molo.entity.ChatThread thr = null;
                        for (com.molo.entity.ChatThread it : mChatThreadList) {
                            if (it.getOther().equals(log)) {
                                thr = it;
                                break;
                            }
                        }
                        if (thr != null)
                            dispatchCallCanceled(thr);
                    }
                    break;
                    case MyStandarMessageType.CALL_FROM: {
                        String log = data.get("other").asText();
                        SwingUtilities.invokeLater(() -> {
                            console.println("Call from " + log);
                        });
                        com.molo.entity.ChatThread thr = null;
                        for (com.molo.entity.ChatThread it : mChatThreadList) {
                            if (it.getOther().equals(log)) {
                                System.out.println("call from " + log);
                                thr = it;
                                break;
                            }
                        }
                        if (thr != null)
                            dispatchCallFrom(thr);
                    }
                    break;
                    default:
                        break;
                }
            }

            @Override
            public void onSocketOpened(Socket socket) {
                // TODO Auto-generated method stub
                super.onSocketOpened(socket);
                SwingUtilities.invokeLater(() -> {
                    mConnectButton.setEnabled(true);
                    mConnectButton.setEnabled(true);
                    mConnectButton.setText("Authentificate");
                    console.println("A socket has been opened successfully to the serveur on " + client.address() + ":" + client.getPort());
                    client.runReading();
                    mServeurAddressInput.setEnabled(false);
                    mRegisterButton.setVisible(true);
                    //authentificationInput.setVisible(true);
                });
                isConnectedToServer = true;

            }

            @Override
            public void onSocketOpenedFail() {
                // TODO Auto-generated method stub
                super.onSocketOpenedFail();
                SwingUtilities.invokeLater(() -> {
                    mConnectButton.setEnabled(true);
                    JOptionPane.showMessageDialog(SecondFenetre.this, "Connection Refused", "Error", JOptionPane.ERROR_MESSAGE);
                    console.println("Connection Refused to the serveur on that socket!");
                });
            }
        };

//		mMemberList.add("Client_1");
//		mMemberList.add("Client_2");
//		mMemberList.add("Client_3");
        this.mMemberListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.mChatThreadListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.mMemberListView.addListSelectionListener((e) -> {
            if (!e.getValueIsAdjusting()) {
                Membre el = mMemberList.get(mMemberListView.getSelectedIndex());
                SecondChatThread cth = null;
                int i = 0;
                for (SecondChatThread t : mChatThreadList) {
                    if (t.getOther().equals(el.getLogin())) {
                        cth = t;
                        break;
                    }
                    i++;
                }
                if (cth != null) {
                    currentThread = cth;
                    messageScrollListener.th = currentThread;
                    mMessages.setModel(cth.getDataModel());
                    mMessages.updateUI();
                    //mChatThreadListView.clearSelection();
                    mChatThreadListView.setSelectedIndex(i);
                    mCurrentChatLogin.setText("Current distant client login " + currentThread.getOther());
                    getMessageForAThread(currentThread);
                } else {
                    //we open that thread
                    System.out.println("New Thread on send");
                    System.out.println("member " + el.getLogin());
                    cth = new SecondChatThread(0, mMemberObject.getLogin(), el.getLogin());
                    for (SecondChatThread t : mChatThreadList)
                        System.out.println(t);
                    cth.setNew(true);
                    currentThread = cth;
                    messageScrollListener.th = currentThread;
                    mMessages.setModel(cth.getDataModel());
                    mMessages.updateUI();
                    mChatThreadList.add(cth);
                    mChatThreadListView.updateUI();
                    mCurrentChatLogin.setText("Current distant client login " + currentThread.getOther());
                }
            }
        });
        this.mChatThreadListView.addListSelectionListener((e) -> {
            if (!e.getValueIsAdjusting()) {
                SecondChatThread el = mChatThreadList.get(mChatThreadListView.getSelectedIndex());
                if (el != currentThread) {
                    currentThread = el;
                    messageScrollListener.th = currentThread;
                    //updateUnread(currentThread);
                    mMessages.setModel(currentThread.getDataModel());
                    if (currentThread.size() == 0 && currentThread.getMessageCount() > 0) {
                        getMessageForAThread(currentThread);
                    } else {
                        if (currentThread.size() > 0) {
                            mMessages.ensureIndexIsVisible(currentThread.size() - 1);
                        }
                    }
                    //currentThread.setUnread(0);
                    mMessages.updateUI();
                    mMessages.ensureIndexIsVisible(currentThread.size() - 1);
                    console.println("Open chat with " + currentThread);
                    mCurrentChatLogin.setText("Current distant client login " + currentThread.getOther());
                } else {
                    mMessages.updateUI();
                }
            }
            if (currentThread != null) {
                voiceRecord.setEnabled(true);
                callBtn.setEnabled(true);
                chooseFiles.setEnabled(true);
                chooseImage.setEnabled(true);
                mInput.setEnabled(true);
            }
        });
        selfProfil.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                super.mouseClicked(e);
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileFilter() {
                    @Override
                    public String getDescription() {
                        // TODO Auto-generated method stub
                        return "";
                    }

                    @Override
                    public boolean accept(File f) {
                        // TODO Auto-generated method stub
                        return f.getName().matches("(.*\\.png)|(.*\\.jpg)|(.*\\.jpeg)|(.*\\.gif)");
                    }
                });
                if (chooser.showOpenDialog(SecondFenetre.this) == JFileChooser.APPROVE_OPTION) {
                    //sendFile(chooser.getSelectedFile());
                    final File file = chooser.getSelectedFile();
                    MCQuery q = new MCQuery("updateProfil");
                    q.addFile(chooser.getSelectedFile());
                    q.callback = new OnMCQueryResult() {
                        @Override
                        public void onSuccess(Response r) {
                            // TODO Auto-generated method stub
                            if (r.status == MyStandarMessageType.OK) {
                                JsonNode n = (JsonNode) r.data;
                                String newProf = n.get("profil").asText();
                                File dest = new File(Client.USERS_FILE_DATA_DIR + "/" + new File(newProf).getName());
                                mMemberObject.setProfil(newProf);
                                try {
                                    Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    SwingUtilities.invokeLater(() -> {
                                        selfProfil.setIcon(Main.getScaledImage(new ImageIcon(dest.getAbsolutePath()), 48, 48));
                                    });
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                    selfProfil.setIcon(Main.getScaledImage(new ImageIcon(SecondFenetre.class.getResource("/images/mc_user.png")), 48, 48));
                                }
                            } else {
                                //error
                                selfProfil.setIcon(Main.getScaledImage(new ImageIcon(SecondFenetre.class.getResource("/images/mc_user.png")), 48, 48));
                            }
                        }

                        @Override
                        public void onFail(Response r) {
                        }
                    };
                    q.execute();
                }
            }
        });
//		mEditLogin.addActionListener((e)->{
//			//mLoginLabel.setEditable(true);
//			mLoginLabel.requestFocus();
//			mEditLogin.setEnabled(false);
//		});
//		mLoginLabel.addKeyListener(new KeyAdapter() {
//			@Override
//			public void keyPressed(KeyEvent e) {
//				// TODO Auto-generated method stub
//				if(e.getKeyChar()=='\n'){
//					requestTypes.add(new Requete(COMMANDE_ID, RequestType.SET_LOGIN));
//					requestMaker.removeAll();
//					requestMaker.put("hash", COMMANDE_ID++);
//					requestMaker.put("command", "setLogin");
//					requestMaker.put("login",mLoginLabel.getText());
//					client.writeToSocket(requestMaker.toString());
//					mLoginLabel.setEditable(false);
//				}
//			}
//		});
        mServeurAddressInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // TODO Auto-generated method stub
                if (e.getKeyChar() == '\n') {
                    mConnectButton.doClick();
                }
            }
        });
        mConnectButton.addActionListener((e) -> {
//			int option=JOptionPane.sh
            if (!isConnectedToServer) {
                String tmps[] = mServeurAddressInput.getText().split("[:]");
                if (tmps.length == 2) {
                    String add = tmps[0];
                    int p = 0;
                    try {
                        p = Integer.valueOf(tmps[1]);
                    } catch (NumberFormatException ex) {
                        p = 0;
                    }
                    System.out.println("Addr: " + add + "; port : " + p);
                    client.setAddress(add);
                    client.setPort(p);
                    new Runnable() {
                        public void run() {
                            client.tryToConnect();
                        }
                    }.run();
                    mConnectButton.setEnabled(false);

//					} catch(UnknownHostException ex){
//						JOptionPane.showMessageDialog(this, "Unreachable Host", "Error", JOptionPane.ERROR_MESSAGE);
//						console.println( "Unable to find the serveur on that socket!");
//					} catch(ConnectException ex){
//						JOptionPane.showMessageDialog(this, "Connection Refused", "Error", JOptionPane.ERROR_MESSAGE);
//						console.println( "Connection Refused to the serveur on that socket!");
//					}
//					catch (IOException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid adress! Should be (address:port)", "Invalid input", JOptionPane.ERROR_MESSAGE);
                    console.println("Invalid adress! Should be (address:port)");
                }
            } else if (isConnectedToServer && !isAuthentificated) {
                authentificationInput.setVisible(true);
            } else {
                //client.writeToSocket("{\"hash\":"+(COMMANDE_ID++)+",\"command\":\"quit\"}");
                MCQuery q = new MCQuery("quit");
                q.callback = new OnMCQueryResult() {
                    @Override
                    public void onSuccess(Response r) {
                        // TODO Auto-generated method stub
                        System.out.println("QUIIIIIITTTTTTT");
                        currentThread = null;
                        mMemberList.clear();
                        mMemberList.clear();
                        mChatThreadList.clear();
                        isAuthentificated = false;
                        SwingUtilities.invokeLater(() -> {
                            mLoginLabel.setText("not connected");
                            mChatThreadListView.updateUI();
                            mMemberListView.updateUI();
                            authentificationInput.setEnabled(true);
                            mConnectButton.setText("Connect to sever");
                            mRegisterButton.setVisible(false);
                            mInput.setEnabled(false);
                            chooseFiles.setEnabled(false);
                            voiceRecord.setEnabled(false);
                            callBtn.setEnabled(false);
                            chooseImage.setEnabled(false);
                            mServeurAddressInput.setEnabled(true);
                            isConnectedToServer = false;
                            isAuthentificated = false;
                            //mLoginLabel.setText(mMemberObject.getLogin()+" offline");
                        });
                    }

                    @Override
                    public void onFail(Response r) {
                        // TODO Auto-generated method stub

                    }
                };
                q.execute();
                //client.writeToSocket("{\"hash\":"+(COMMANDE_ID++)+",\"command\":\"quit\"}");
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // TODO Auto-generated method stub
                if (client != null) {
                    if (isAuthentificated) {
                        client.writeToSocket("{\"hash\":" + (COMMANDE_ID++) + ",\"command\":\"quit\"}");
                        //client.writeToSocket("{\"hash\":"+(COMMANDE_ID++)+",\"command\":\"quit\"}");
                    } else if (isConnectedToServer) {
                        client.writeToSocket("{\"hash\":" + (COMMANDE_ID++) + ",\"command\":\"exit\"}");
                    }
                    messageReceivedClip.close();
                    messageSentClip.close();
                    callClip.close();
                    callFromClip.close();
                    client.close();
                }
                AudioPlayer.releaseAll();
                //receiveMessage.close();
                super.windowClosing(e);
            }
        });
        mInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                // TODO Auto-generated method stub
                char c = e.getKeyChar();
                if (c == '\n') {
                    //System.out.println("Key typed");
                    //send message
                    sendMessage(currentThread);
                    mInput.setText("");
                }
            }
        });
        chooseImage.addActionListener((ev) -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileFilter() {

                @Override
                public String getDescription() {
                    // TODO Auto-generated method stub
                    return "";
                }

                @Override
                public boolean accept(File f) {
                    // TODO Auto-generated method stub
                    return f.getName().matches("(.*\\.png)|(.*\\.jpg)|(.*\\.jpeg)|(.*\\.gif)");
                }
            });
            if (chooser.showOpenDialog(SecondFenetre.this) == JFileChooser.APPROVE_OPTION) {
                //sendFile(chooser.getSelectedFile());
                filesToSend.add(chooser.getSelectedFile());
            }
        });
        chooseFiles.addActionListener((ev) -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileFilter() {

                @Override
                public String getDescription() {
                    // TODO Auto-generated method stub
                    return "";
                }

                @Override
                public boolean accept(File f) {
                    // TODO Auto-generated method stub
                    return f.getName().matches(".*");
                }
            });
            if (chooser.showOpenDialog(SecondFenetre.this) == JFileChooser.APPROVE_OPTION) {
                filesToSend.add(chooser.getSelectedFile());
            }
        });
        callBtn.addActionListener((ev) -> {
            if (currentThread != null) {
                callDialog = new CallDialog(SecondFenetre.this, currentThread, CallDialog.OUTGOING_CALL, new CallDialog.OnCloseListener() {
                    @Override
                    public void onCancel(ChatThread th) {
                        // TODO Auto-generated method stub
                        System.out.println("Cancel OUT_GOING_CALL " + th.getOther());
                        callDialog.close();
                        MCQuery q = new MCQuery("cancelCall");
                        q.data.put("other", currentThread.getOther());
                        q.callback = new OnMCQueryResult() {
                            @Override
                            public void onSuccess(Response r) {
                                // TODO Auto-generated method stub
                                callFromClip.stop();
                                callClip.stop();
                            }

                            @Override
                            public void onFail(Response r) {
                                // TODO Auto-generated method stub

                            }
                        };
                        q.execute();
                        System.out.println("Cancel OUT_GOING_CALL " + th.getOther());
                    }

                    @Override
                    public void onAccept(ChatThread th) {
                        // TODO Auto-generated method stub

                    }


                });
                MCQuery q = new MCQuery("call");
                q.data.put("other", currentThread.getOther());
                q.callback = new OnMCQueryResult() {
                    @Override
                    public void onSuccess(Response r) {
                        // TODO Auto-generated method stub
                        if (r.status == MyStandarMessageType.OK) {
                            SwingUtilities.invokeLater(() -> {
                                console.println("Calling.....");
                                callClip.play(true);
                            });
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                callDialog.close();
                                console.println("Call unreachable");
                            });
                        }
                    }

                    @Override
                    public void onFail(Response r) {
                        // TODO Auto-generated method stub

                    }
                };
                q.execute();
                SwingUtilities.invokeLater(() -> {
                    callDialog.setVisible(true);
                });
            }
        });
        recordListener = new AudioRecordListener() {
            @Override
            public void onRecordStart() {
                // TODO Auto-generated method stub
                //System.out.println("Record start");
                System.out.println("Record Started");
                SwingUtilities.invokeLater(() -> {
                    recording.setVisible(true);
                });
                recording.setVisible(true);
            }

            @Override
            public void onRecordFailed() {
                // TODO Auto-generated method stub
                System.out.println("Record failed");
                SwingUtilities.invokeLater(() -> {
                    recording.setVisible(false);
                });
            }

            @Override
            public void onRecordFinish(File f) {
                // TODO Auto-generated method stub
                // TODO Auto-generated method stub
                System.out.println("Record ended");
                filesToSend.clear();
                filesToSend.add(f);
                recording.setVisible(false);
//				SwingUtilities.invokeLater(()->{
//					recording.setVisible(false);
//				});
                sendMessage(currentThread);
            }

            @Override
            public void onTimer(long t, byte[] data, int length) {
                // TODO Auto-generated method stub
                recording.setText("Recording " + t / 1000);
                SwingUtilities.invokeLater(() -> {
                    recording.setText("Recording: " + t / 1000);
                });
            }
        };
        voiceRecord.addActionListener((ev) -> {
            if (recorder != null && recorder.isAlive()) {
                //on arrête l'enregistrement
                recorder.stopRecording();
            } else {
                recorder = new VoiceRecorder(new File(Client.TEMP_DIR + "/rec_" + (new Date().getTime()) + ".wav"), recordListener);
                recorder.start();
            }
//			SwingUtilities.invokeLater(()->{
//				recording.setVisible(false);
//			});
        });
        mRegisterButton.addActionListener((ev) -> {
            //
            registerDialog.setVisible(true);

        });
        mMessages.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // TODO Auto-generated method stub
                if (!e.getValueIsAdjusting()) {
                    int selectedIndex = mMessages.getSelectedIndex();
                    if (currentThread != null && selectedIndex >= 0) {
                        final ChatMessage message = currentThread.getMessageAt(selectedIndex);
                        if (message != null) {
                            if (isSound(message) && !message.isPlaying()) {
                                Attachment a = message.getAttachments().get(0);
                                if (Utils.existInLocal(a.getPath())) {
                                    message.setPlaying(true);
                                    System.out.println("En lecture");
                                    new AudioController(AudioPlayer.play(Utils.getLocalPath(a.getPath()), new LineListener() {
                                        @Override
                                        public void update(LineEvent event) {
                                            // TODO Auto-generated method stub
                                            System.out.println("Update");
                                            if (event.getType().equals(LineEvent.Type.STOP)) {
                                                System.out.println("Finish playing ");
                                                message.setPlaying(false);
                                                AudioPlayer.resetAudio(Utils.getLocalPath(message.getAttachments().get(0).getPath()));
                                            }
                                            SwingUtilities.invokeLater(() -> {
                                                mMessages.updateUI();
                                            });
                                        }
                                    }), new AudioPlayListener() {
                                        @Override
                                        public void onTimer(long currentTimeNanos) {
                                            // TODO Auto-generated method stub
                                            System.out.println("Update " + currentTimeNanos / 1000000);
                                            SwingUtilities.invokeLater(() -> {
                                                mMessages.updateUI();
                                            });
                                        }
                                    }, false).start();
                                }
                                mMessages.clearSelection();
                            } else if (message.isVoiceMessage() && message.isPlaying()) {
                                System.out.println("En pause");
                                message.setPlaying(false);
                                AudioPlayer.pauseAudio(Utils.getLocalPath(message.getAttachments().get(0).getPath()));
                                mMessages.clearSelection();
                            }
                        }
                    }

                }
            }
        });
        messageScrollListener = new ScrollListener(currentThread);
        messageScroll.getVerticalScrollBar().addAdjustmentListener(messageScrollListener);
        this.setVisible(true);
        client = new Client();
        client.setClientProcessListener(clientProcessListener);
        MCQueryManager manager = new MCQueryManager(client);
        MCQuery.configure(manager);
    }

    protected boolean isSound(ChatMessage message) {
        // TODO Auto-generated method stub
        List<Attachment> atts = message.getAttachments();
        if ((atts != null && atts.isEmpty()) || atts == null)
            return false;
        Attachment a = atts.get(0);
        return a.getType().matches("audio/.*wav.*");
    }

    protected void dispatchCallAcepted(com.molo.entity.ChatThread membre, String label) {
        // TODO Auto-generated method stub
        for (CallListener l : callListeners)
            l.onCallAccepted(membre, label);
    }

    protected void dispatchCallCanceled(com.molo.entity.ChatThread membre) {
        // TODO Auto-generated method stub
        for (CallListener l : callListeners)
            l.onCallCanceled(membre);
    }

    protected void dispatchCallFinished(com.molo.entity.ChatThread t) {
        // TODO Auto-generated method stub
        for (CallListener l : callListeners)
            l.onCallFinished(t);
    }

    protected void dispatchCallFrom(com.molo.entity.ChatThread membre) {
        // TODO Auto-generated method stub
        for (CallListener l : callListeners)
            l.onCallFrom(membre);
    }

    private void dispatchMessageSeen(ChatThread t) {
        // TODO Auto-generated method stub
        for (OnNewMessageListener l : messageListeners) {
            l.onMessageSeen(t);
        }
    }

    private void dispatchMessageSent(ChatThread th, ChatMessage cm) {
        // TODO Auto-generated method stub
        for (OnNewMessageListener l : messageListeners) {
            l.onMessageSent(th, cm);
        }
    }

    private void updateUnread(final ChatThread th) {
        // TODO Auto-generated method stub
        if (th.getUnread() <= 0 || th.getLastSender().equals(mMemberObject.getLogin()))
            return;
        MCQuery q = new MCQuery("updateUnread");
        q.data.put("thread", th.getId());
        q.callback = new OnMCQueryResult() {

            @Override
            public void onSuccess(Response r) {
                // TODO Auto-generated method stub
                th.setUnread(0);
                for (ChatMessage m : th.getMessages()) {
                    m.setSeen(true);
                }
                SwingUtilities.invokeLater(() -> {
                    mChatThreadListView.updateUI();
                    mMessages.updateUI();
                });
            }

            @Override
            public void onFail(Response r) {
                // TODO Auto-generated method stub

            }
        };
        q.execute();
    }

    private void getAllMessageForAThread(final SecondChatThread th) {
//		requestTypes.add(new Requete(COMMANDE_ID,RequestType.GET_ALL_MESSAGE));
//		requestMaker.removeAll();
//		th.requestHash=COMMANDE_ID;
//		requestMaker.put("hash", COMMANDE_ID++);
//		requestMaker.put("command", "getAllMessages");
//		requestMaker.put("other",th.getOther().getLogin());
//		client.writeToSocket(requestMaker.toString());
        MCQuery q = new MCQuery("getAllMessages");
        q.callback = new OnMCQueryResult() {
            @Override
            public void onSuccess(Response r) {
                // TODO Auto-generated method stub
                if (r.status == MyStandarMessageType.OK) {
                    //messages fetched
                    if (th != null) {
                        th.getMessages().clear();
                        ;
                        try {
                            ChatMessage[] messages = mapper.readValue(r.data.toString(), ChatMessage[].class);
                            for (ChatMessage m : messages) {
                                //add m
                                th.push(m);
                                for (Attachment a : m.getAttachments()) {
                                    if (!Utils.existInLocal(a.getPath()))
                                        getAttachement(a);
                                }
                            }
                            SwingUtilities.invokeLater(() -> {
                                mMessages.updateUI();
                            });
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFail(Response r) {
                // TODO Auto-generated method stub

            }
        };
        q.data.put("other", th.getOther());
        q.execute();
    }

    private void getMessageForAThread(final SecondChatThread th) {
        MCQuery q = new MCQuery("getMessages");
        q.data.put("other", th.getOther());
        q.data.put("page", th.PAGE);
        q.data.put("size", th.SIZE);
        q.callback = new OnMCQueryResult() {
            @Override
            public void onSuccess(Response r) {
                // TODO Auto-generated method stub
                if (r.status == MyStandarMessageType.OK) {
                    //messages fetched
                    try {
                        ChatMessage[] messages = mapper.readValue(r.data.toString(), ChatMessage[].class);
                        if (messages.length > 0) {
                            if (th != null) {
                                if (th.size() > 0) {
                                    for (int i = 0; i < messages.length; i++) {
                                        ChatMessage cm = messages[i];
                                        System.out.println("Attchs: " + cm.getAttachments().size());
                                        th.unshift(cm);
                                        cm.setSeen(true);
                                        cm.setContent(decryptCSAR(cm.getContent()));
                                        for (Attachment a : cm.getAttachments()) {
                                            if (!Utils.existInLocal(a.getPath()))
                                                getAttachement(a);
                                        }
                                        SwingUtilities.invokeLater(() -> {
                                            mMessages.updateUI();
                                        });
                                    }
                                } else {
                                    List<ChatMessage> msg = new ArrayList<ChatMessage>();
                                    for (int i = messages.length - 1; i >= 0; i--) {
                                        ChatMessage cm = messages[i];
                                        System.out.println("Attchs: " + cm.getAttachments().size());
                                        cm.setSeen(true);
                                        cm.setContent(decryptCSAR(cm.getContent()));
                                        msg.add(cm);
                                        for (Attachment a : cm.getAttachments()) {
                                            if (!Utils.existInLocal(a.getPath()))
                                                getAttachement(a);
                                        }
                                    }
                                    th.addAll(msg);
                                    if (th.getUnread() > 0 && th.getLastSender().equals(mMemberObject.getLogin())) {
                                        for (int i = th.size() - th.getUnread(); i < th.size(); i++)
                                            msg.get(i).setSeen(false);
                                    }
                                    SwingUtilities.invokeLater(() -> {
                                        mMessages.updateUI();
                                        mMessages.ensureIndexIsVisible(th.size() - 1);
                                        messageScrollListener.th = th;

                                    });
                                }
                            } else {
                                //we just ignore the reuslt
                            }
                        }
                        mMessages.setModel(th.getDataModel());
                        th.PAGE++;

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFail(Response r) {
                // TODO Auto-generated method stub

            }
        };
        q.execute();
    }

    protected void getAttachement(Attachment a) {
        // TODO Auto-generated method stub
        MCQuery q = new MCQuery("fget");
        q.data.put("name", a.getPath());
        q.callback = new OnMCQueryResult() {
            @Override
            public void onSuccess(Response r) {
                if (r.status == MyStandarMessageType.OK && r.medias != null) {
                    MediaFile m = r.medias[0];
                    File f = new File(m.absolutPath), dest = new File(Client.USERS_FILE_DATA_DIR + "/" + m.name);
                    System.out.println("PROFILE FILE: " + f.getAbsolutePath());
                    try {
                        Files.move(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    SwingUtilities.invokeLater(() -> {
                        if (currentThread != null)
                            mMessages.updateUI();
                    });
                }
            }

            @Override
            public void onFail(Response r) {

            }
        };
        q.execute();
    }

    private void getAllThreads() {
//		requestTypes.add(new Requete(COMMANDE_ID,RequestType.GET_ALL_THREAD));
//		requestMaker.removeAll();
//		requestMaker.put("hash", COMMANDE_ID++);
//		requestMaker.put("command", "getAllThreads");
//		client.writeToSocket(requestMaker.toString());
        MCQuery q = new MCQuery("getAllThreads");
        q.callback = new OnMCQueryResult() {

            @Override
            public void onSuccess(Response r) {
                // TODO Auto-generated method stub
                if (r.status == MyStandarMessageType.OK) {
                    //messages fetched
                    mChatThreadList.clear();
                    try {
                        SecondChatThread[] threads = mapper.readValue(r.data.toString(), SecondChatThread[].class);
                        for (SecondChatThread t : threads) {
                            //add m
                            mChatThreadList.add(t);
                            if (t.getProfil() != null && !t.getProfil().isEmpty()) {
                                if (!profilExistInLocal(t.getProfil())) {
                                    getProfil(t);
                                } else {
                                    t.setProfil(new File(Client.USERS_FILE_DATA_DIR + "/" + (new File(t.getProfil()).getName())).getAbsolutePath());
                                }
                            }
                        }
                        SwingUtilities.invokeLater(() -> {
                            mChatThreadListView.updateUI();
                        });
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFail(Response r) {
                // TODO Auto-generated method stub

            }
        };
        q.execute();
    }

    private void getThreads() {
        //requestTypes.add(new Requete(COMMANDE_ID,RequestType.GET_THREAD));
//		requestMaker.removeAll();
//		requestMaker.put("hash", COMMANDE_ID++);
//		requestMaker.put("command", "getThreads");
//		requestMaker.put("page",THPAGE);
//		requestMaker.put("size",THPSIZE);
//		client.writeToSocket(requestMaker.toString());
        MCQuery q = new MCQuery("getThreads");
        q.data.put("page", THPAGE);
        q.data.put("size", THPSIZE);
        q.callback = new OnMCQueryResult() {

            @Override
            public void onSuccess(Response r) {
                // TODO Auto-generated method stub
                if (r.status == MyStandarMessageType.OK) {
                    //messages fetched
                    //mChatThreadList.clear();
                    try {
                        SecondChatThread[] threads = mapper.readValue(r.data.toString(), SecondChatThread[].class);
                        THPAGE++;
                        for (int i = threads.length - 1; i >= 0; i--) {
                            //add m
                            ChatThread t = threads[i];
                            //System.err.println("TTT: "+t.toString());
                            mChatThreadList.add(threads[i]);
                            if (t.getProfil() != null && !t.getProfil().isEmpty()) {
                                if (!profilExistInLocal(t.getProfil())) {
                                    getProfil(t);
                                } else {
                                    t.setProfil(new File(Client.USERS_FILE_DATA_DIR + "/" + (new File(t.getProfil()).getName())).getAbsolutePath());
                                }
                            }
                        }
                        SwingUtilities.invokeLater(() -> {
                            mChatThreadListView.updateUI();
                        });
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFail(Response r) {
                // TODO Auto-generated method stub

            }
        };
        q.execute();
    }

    private void buildView() {
        // TODO Auto-generated method stub
        this.setContentPane(mainPanel);
        registerDialog = new RegisterDialog(this, (m, d) -> {
            String l = (String) m.get(RegisterDialog.LOGIN);
            String p = (String) m.get(RegisterDialog.PASSWORD);
            String pc = (String) m.get(RegisterDialog.PASSWORD_C);
            String nom = (String) m.get(RegisterDialog.NOM);
            String prenom = (String) m.get(RegisterDialog.PRENOM);
            System.out.println("On Oke btn: " + l);
            if (l.isEmpty()) {
                JOptionPane.showMessageDialog(this, "May specify a login", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "May specify a password", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                if (!p.equals(pc)) {
                    JOptionPane.showMessageDialog(this, "Password not match", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            d.setVisible(false);
            d.dispose();
            if (isConnectedToServer) {
                MCQuery q = new MCQuery("register");
                q.data.put("login", l);
                q.data.put("password", p);
                q.data.put("name", nom);
                q.data.put("fname", prenom);
                q.callback = new OnMCQueryResult() {
                    @Override
                    public void onSuccess(Response r) {
                        // TODO Auto-generated method stub
                        if (r.status == MyStandarMessageType.OK) {
                            //
                            SwingUtilities.invokeLater(() -> {
                                progress.setVisible(false);
                                JOptionPane.showMessageDialog(SecondFenetre.this, "Registered with success", "Success", JOptionPane.PLAIN_MESSAGE);
                            });
                        } else {
                            //register error
                            JOptionPane.showMessageDialog(SecondFenetre.this, "Not registered : " + ((JsonNode) r.data), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    @Override
                    public void onFail(Response r) {
                        // TODO Auto-generated method stub

                    }
                };
                q.execute();
                progress.setVisible(true);
            }
        });
        authentificationInput = new LoginInput(this, (m, d) -> {
            System.out.println("Closed");
            //m=(Map<String, Object>)m;
            String l = (String) m.get(LoginInput.LOGIN);
            String p = (String) m.get(LoginInput.PASSWORD);
            boolean remember = (boolean) m.get(LoginInput.REMEMBER_ME);
            if (l.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Invalid Login information", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            d.setVisible(false);
            d.dispose();

            if (isConnectedToServer && !isAuthentificated) {
                //we try to connect to the serveur
//					requestMaker.removeAll();
//					requestMaker.put("hash", COMMANDE_ID++);
//					requestMaker.put("command", "authentificate");
//					requestMaker.put("login",l);
//					requestMaker.put("password",p);
//					client.writeToSocket(requestMaker.toString());
                setPublicKey();
                MCQuery q = new MCQuery("authentificate");
                q.data.put("login", l);
                q.data.put("password", p);
                q.callback = new OnMCQueryResult() {

                    @Override
                    public void onSuccess(Response r) {
                        // TODO Auto-generated method stub
                        SwingUtilities.invokeLater(() -> {
                            progress.setVisible(false);
                        });
                        if (r.status == MyStandarMessageType.AUTHENTIFICATED_SUCCESSFULLY) {
                            isAuthentificated = true;
                            //String content=data.get("content").asText();
                            console.println("Authentificate successfully");
                            //System.out.println(content+";");
                            try {
                                mMemberObject = mapper.readValue(r.data.toString(), Membre.class);
                                SwingUtilities.invokeLater(() -> {
                                    mLoginLabel.setText(mMemberObject.getLogin());
                                    mEditLogin.setEnabled(true);
                                    mEditLogin.setEnabled(true);

//									chooseFiles.setEnabled(true);
//									voiceRecord.setEnabled(true);
//									chooseImage.setEnabled(true);
                                    mRegisterButton.setVisible(false);
                                    if (!mMemberObject.getProfil().isEmpty()) {
                                        System.out.println("Profil non null");
                                        String pr = mMemberObject.getProfil();
                                        File f = new File(Client.USERS_FILE_DATA_DIR + "/" + new File(pr).getName());
                                        if (!f.exists()) {
                                            new Runnable() {
                                                public void run() {
                                                    getProfil(mMemberObject);
                                                }
                                            }.run();

                                        } else {
                                            selfProfil.setIcon(Main.getScaledImage(new ImageIcon(f.getAbsolutePath()), 48, 48));
                                        }
                                    } else {
                                        System.out.println("Pas de profil:! default");
                                        selfProfil.setIcon(Main.getScaledImage(new ImageIcon(SecondFenetre.class.getResource("/images/mc_user.png")), 48, 48));
                                    }
                                });
                                mChatThreadListView.setCellRenderer(new SMChatThreadListCellRender(mMemberObject));
                                mMessages.setCellRenderer(new SMChatMessageListCellRender(mMemberObject));
                                getThreads();
                                getAllMembers();
                                loadKeys();
                            } catch (IOException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }

                            //getLogin();
                            //mConnectButton.setText("Disconnect");
                            mConnectButton.setEnabled(false);
                        } else if (r.status == MyStandarMessageType.AUTHENTIFICATION_ERROR) {
                            String tmp = ((JsonNode) r.data).get("content").asText();
                            console.println(tmp);
                            JOptionPane.showMessageDialog(SecondFenetre.this, tmp, "Error login", JOptionPane.ERROR_MESSAGE);
                        }

                    }

                    @Override
                    public void onFail(Response r) {
                        // TODO Auto-generated method stub
                    }
                };
                q.execute();
                progress.setVisible(true);
            }
        });
        selfProfil.setSize(new Dimension(48, 48));
        selfProfil.setPreferredSize(new Dimension(48, 48));
        selfProfil.setMaximumSize(new Dimension(48, 48));
        //selfProfil.getIcon()
        authentificationInput.setLocationRelativeTo(this);
        mServeurAddressInput = new JTextField();
//		mServeurAddressInput.setPlaceHolder("example= localhost:7081");
//		mInput.setPlaceHolder("Your message here");
        consoleScroll = new JScrollPane(console);
        consoleScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        consoleScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        consoleScroll.setPreferredSize(new Dimension(this.getWidth() - 100, 100));
        JXPanel centerPane = new JXPanel(),
                leftSidePan = new JXPanel();
        JScrollPane connectedListScroll = new JScrollPane(mMemberListView),
                openedChatListScroll = new JScrollPane(mChatThreadListView);
        centerPane.setLayout(new BorderLayout());
        connectedListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        openedChatListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        connectedListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        openedChatListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JXPanel centerTopPan = new JXPanel(new BorderLayout()),
                centerTopPenEast = new JXPanel(),
                centerTopPenCenter = new JXPanel(new BorderLayout());
        mServeurAddressInput.setBorder(new RoundedBorder(new Color(180, 180, 180), 10));
        mRegisterButton.setVisible(false);
        JXPanel messageInputPane = new JXPanel(new BorderLayout()), messageInputBtnPane = new JXPanel(new BorderLayout()),
                messageInputBtnPaneLeft = new JXPanel(), messageInputBtnPaneRight = new JXPanel(new BorderLayout());
        messageInputPane.add(mInput, BorderLayout.CENTER);
        messageInputPane.add(messageInputBtnPane, BorderLayout.SOUTH);
        messageInputBtnPaneLeft.setLayout(new BoxLayout(messageInputBtnPaneLeft, BoxLayout.LINE_AXIS));
//		chooseFiles.setOpaque(true);
//		chooseFiles.setBackground(null);
        messageInputBtnPaneLeft.add(chooseImage);
        messageInputBtnPaneLeft.add(chooseFiles);
        messageInputBtnPane.add(messageInputBtnPaneLeft, BorderLayout.WEST);
        messageInputBtnPane.add(messageInputBtnPaneRight, BorderLayout.EAST);
        messageInputBtnPaneRight.add(recording, BorderLayout.WEST);
        messageInputBtnPaneRight.add(voiceRecord, BorderLayout.EAST);
        recording.setVisible(false);
        recording.setOpaque(true);
        recording.setForeground(Color.red);
        centerTopPenCenter.add(mServeurAddressInput, BorderLayout.NORTH);
        centerTopPenEast.setLayout(new BorderLayout());
        centerTopPenEast.add(mConnectButton, BorderLayout.NORTH);
        centerTopPenEast.add(mRegisterButton, BorderLayout.SOUTH);
        centerTopPan.add(centerTopPenCenter, BorderLayout.CENTER);
        centerTopPan.add(centerTopPenEast, BorderLayout.EAST);
        centerPane.add(centerTopPan, BorderLayout.NORTH);
        centerPane.add(messageInputPane, BorderLayout.SOUTH);
        JXPanel messagePane = new JXPanel(new BorderLayout());
        messageScroll = new JScrollPane(mMessages);


        messageScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messageScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JXPanel topButtonsPan = new JXPanel(new BorderLayout());
        topButtonsPan.add(mCurrentChatLogin, BorderLayout.WEST);
        topButtonsPan.add(callBtn, BorderLayout.EAST);
        messagePane.add(topButtonsPan, BorderLayout.NORTH);
        messagePane.add(messageScroll, BorderLayout.CENTER);
        centerPane.add(messagePane, BorderLayout.CENTER);
        leftSidePan.setLayout(new BorderLayout());
        leftSidePan.setPreferredSize(new Dimension(200, leftSidePan.getHeight()));
//		mMemberListView.setMinimumSize(new Dimension(200,400));
//		mChatThreadListView.setMinimumSize(new Dimension(200,400));
//		connectedListScroll.add(mMemberListView);
//		openedChatListScroll.add(mChatThreadListView);
        JXPanel splitPane1 = new JXPanel(new BorderLayout()), splitPane2 = new JXPanel(new BorderLayout());
        splitPane1.add(new JLabel("Connected list"), BorderLayout.NORTH);
        splitPane1.add(connectedListScroll, BorderLayout.CENTER);
        splitPane2.add(new JLabel("Opened chat list"), BorderLayout.NORTH);
        splitPane2.add(openedChatListScroll, BorderLayout.CENTER);
        JSplitPane connOpenSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane1, splitPane2);
//		connOpenSplit.add(connectedListScroll);
//		connOpenSplit.add(openedChatListScroll,1);
        connOpenSplit.setDividerLocation(0.5d);
        connOpenSplit.setResizeWeight(.5d);
        JXPanel loginPane = new JXPanel(new BorderLayout()), loginPaneCenter = new JXPanel();
        //mLoginLabel.setEditable(false);
        loginPaneCenter.setLayout(new BoxLayout(loginPaneCenter, BoxLayout.LINE_AXIS));
        loginPaneCenter.add(selfProfil);
        loginPaneCenter.add(mLoginLabel);
        loginPane.add(loginPaneCenter, BorderLayout.CENTER);
        loginPane.add(mEditLogin, BorderLayout.EAST);
        leftSidePan.add(loginPane, BorderLayout.NORTH);
        leftSidePan.add(connOpenSplit, BorderLayout.CENTER);
        //leftSidePan.add(openedChatListScroll);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(centerPane, BorderLayout.CENTER);
        mainPanel.add(leftSidePan, BorderLayout.WEST);
        mainPanel.add(consoleScroll, BorderLayout.SOUTH);
        mMessages.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //if last message not seen we make it seen
                if (currentThread != null) {
                    updateUnread(currentThread);
                }
            }
        });
        mInput.setEnabled(false);
        chooseFiles.setEnabled(false);
        voiceRecord.setEnabled(false);
        chooseImage.setEnabled(false);
        callBtn.setEnabled(false);
    }

    private void getAllMembers() {
        if (isConnectedToServer) {
            MCQuery q = new MCQuery("getAllMembers");
            q.callback = new OnMCQueryResult() {
                @Override
                public void onSuccess(Response r) {
                    // TODO Auto-generated method stub
                    try {
                        Membre[] tmps = mapper.readValue(r.data.toString(), Membre[].class);
                        List<Membre> members = new ArrayList<Membre>(Arrays.asList(tmps));
                        mMemberList.clear();
                        for (Membre m : members) {
                            console.println(m.getLogin());
                            mMemberList.add(m);
                            if (!m.getProfil().isEmpty()) {
                                if (!profilExistInLocal(m.getProfil())) {
                                    getProfil(m);
                                } else {
                                    m.setProfil(new File(Client.USERS_FILE_DATA_DIR + "/" + (new File(m.getProfil()).getName())).getAbsolutePath());
                                }
                            }
                        }
                        SwingUtilities.invokeLater(() -> {
                            mMemberListView.updateUI();
                        });

                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }

                @Override
                public void onFail(Response r) {
                    // TODO Auto-generated method stub

                }
            };
            q.execute();
        }
    }

    private void getAllConnectedMembers() {
        if (isConnectedToServer) {
            MCQuery q = new MCQuery("getAllConnectedMembers");
            q.callback = new OnMCQueryResult() {
                @Override
                public void onSuccess(Response r) {
                    // TODO Auto-generated method stub
                    try {
                        Membre[] tmps = mapper.readValue(r.data.toString(), Membre[].class);
                        List<Membre> members = new ArrayList<Membre>(Arrays.asList(tmps));
                        mMemberList.clear();
                        for (Membre m : members) {
                            console.println(m.getLogin());
                            mMemberList.add(m);
                            if (!m.getProfil().isEmpty()) {
                                if (!profilExistInLocal(m.getProfil())) {
                                    getProfil(m);
                                } else {
                                    m.setProfil(new File(Client.USERS_FILE_DATA_DIR + "/" + (new File(m.getProfil()).getName())).getAbsolutePath());
                                }
                            }
                        }
                        SwingUtilities.invokeLater(() -> {
                            mMemberListView.updateUI();
                        });

                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }

                @Override
                public void onFail(Response r) {
                    // TODO Auto-generated method stub

                }
            };
            q.execute();
        }
    }

    private void sendFile(File f) {
        if (f.exists()) {
            MCQuery q = new MCQuery("fput");
            q.data.put("fname", f.getName());
            q.data.put("fsize", f.length());
            q.data.put("ftype", "jpeg");
            q.callback = new OnMCQueryResult() {

                @Override
                public void onSuccess(Response r) {
                    // TODO Auto-generated method stub
                    if (r.status == MyStandarMessageType.OK) {
                        new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                DataOutputStream out = client.getOut();
                                if (out != null) {
                                    System.out.println("Out NOT null");
                                    if (MFtp.ftpPut(f.getAbsolutePath(), out)) {
                                        console.println("File sent");
                                    }
                                } else {
                                    System.out.println("Out null");
                                }
                            }
                        }.run();
                    }
                }

                @Override
                public void onFail(Response r) {
                    // TODO Auto-generated method stub

                }
            };
            q.execute();
        }
    }

    private void getLogin() {
        if (isConnectedToServer && isAuthentificated) {
//			requestTypes.add(new Requete(COMMANDE_ID,RequestType.GET_LOGIN));
//			requestMaker.removeAll();
//			requestMaker.put("hash", COMMANDE_ID++);
//			requestMaker.put("command", "getLogin");
//			client.writeToSocket(requestMaker.toString());
            MCQuery q = new MCQuery("getLogin");
            q.callback = new OnMCQueryResult() {

                @Override
                public void onSuccess(Response r) {
                    // TODO Auto-generated method stub
                    try {
                        mMemberObject = mapper.readValue(r.data.toString(), Membre.class);
                        mMessages.setCellRenderer(new SMChatMessageListCellRender(mMemberObject));
                        SwingUtilities.invokeLater(() -> {
                            mLoginLabel.setText(mMemberObject.getLogin());

                            //mEditLogin.setEnabled(true);
                            if (!mMemberObject.getProfil().isEmpty()) {
                                System.out.println("Profil non null");
                                String pr = mMemberObject.getProfil();
                                File f = new File(Client.USERS_FILE_DATA_DIR + "/" + new File(pr).getName());
                                if (!f.exists())
                                    getProfil(mMemberObject);
                                else
                                    selfProfil.setIcon(Main.getScaledImage(new ImageIcon(f.getAbsolutePath()), 48, 48));
                            } else {
                                System.out.println("Pas de profil:! default");
                                selfProfil.setIcon(Main.getScaledImage(new ImageIcon(SecondFenetre.class.getResource("/assets/mages/mc_user")), 48, 48));
                            }
                        });
                        //getThreads();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFail(Response r) {
                    // TODO Auto-generated method stub

                }
            };
            q.execute();
        }
    }

    synchronized private void getProfil(Membre membre) {
        // TODO Auto-generated method stub
        MCQuery q = new MCQuery("getUserProfil");
        q.data.put("user", membre.getLogin());
        q.callback = new OnMCQueryResult() {
            @Override
            public void onSuccess(Response r) {
                if (r.status == MyStandarMessageType.OK && r.medias != null) {
                    MediaFile m = r.medias[0];
                    File f = new File(m.absolutPath), dest = new File(Client.USERS_FILE_DATA_DIR + "/" + m.name);
                    System.out.println("PROFILE FILE: " + f.getAbsolutePath());
                    try {
                        Files.move(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (dest != null && dest.isFile()) {
                        //m.setIcon(getScaledImage(new ImageIcon(f.getAbsolutePath()),48,48));
                        membre.setProfil(dest.getAbsolutePath());
                    }
                    if (membre.equals(mMemberObject)) {
                        SwingUtilities.invokeLater(() -> {
                            selfProfil.setIcon(Main.getScaledImage(new ImageIcon(membre.getProfil()), 48, 48));
                        });
                    }
                }
            }

            @Override
            public void onFail(Response r) {

            }
        };
        q.execute();
    }

    private void getProfil(ChatThread th) {
        // TODO Auto-generated method stub
        MCQuery q = new MCQuery("getUserProfil");
        q.data.put("user", th.getOther());
        q.callback = new OnMCQueryResult() {
            @Override
            public void onSuccess(Response r) {
                if (r.status == MyStandarMessageType.OK && r.medias != null) {
                    MediaFile m = r.medias[0];
                    File f = new File(m.absolutPath), dest = new File(Client.USERS_FILE_DATA_DIR + "/" + m.name);
                    try {
                        Files.move(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (dest != null && dest.isFile()) {
                        //m.setIcon(getScaledImage(new ImageIcon(f.getAbsolutePath()),48,48));
                        th.setProfil(dest.getAbsolutePath());
                    }

                    SwingUtilities.invokeLater(() -> {
                        mChatThreadListView.updateUI();
                    });

                }
            }

            @Override
            public void onFail(Response r) {

            }
        };
        q.execute();
    }

    private void sendMessage(final SecondChatThread th) {
        String text = mInput.getText();
        if (!isAuthentificated || currentThread == null)
            return;
        if (text.isEmpty() && filesToSend.isEmpty())
            return;
        String encText = encryptCSAR(text);
        ChatMessage msgToSend = new ChatMessage();
        msgToSend.setContent(text);
        msgToSend.setId(0);
        msgToSend.setReceiver(th.getOther());
        msgToSend.setSender(mMemberObject.getLogin());
        final int currentPos = th.size();
        th.push(msgToSend);
        th.setLastMessage(msgToSend.getContent());
        final MCQuery q = new MCQuery("postmessage");
        q.data.put("receiver", th.getOther());
        q.data.put("content", encText);
        //if(!encText.equals(text)){
        q.data.put("encrypt", true);
        //}
        if (filesToSend.size() > 0)
            q.addFiles(filesToSend);
        q.callback = new OnMCQueryResult() {
            @Override
            public void onSuccess(Response r) {
                // TODO Auto-generated method stub
                if (r.status != MyStandarMessageType.SEND_MESSAGE_ERROR) {
                    ChatMessage message;
                    try {
                        message = mapper.readValue(r.data.toString(), ChatMessage.class);
                        System.out.println("Send message + " + th.getId());
                        //nouveau thread
                        if (th.getId() == 0) {
                            System.err.println("Premier message ");
                            //new thread
                            th.setId(message.getThread());
                            th.setNew(false);
                            SwingUtilities.invokeLater(() -> {
                                mChatThreadListView.updateUI();
                            });
                        }
                        //we move the file to their destination is necessary
                        if (q.medias != null && !q.medias.isEmpty()
                                && message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                            int i = 0;
                            for (Attachment at : message.getAttachments()) {
                                Files.copy(new File(q.medias.get(i++).absolutPath).toPath(),
                                        new File(Client.USERS_FILE_DATA_DIR + "/" + (new File(at.getPath()).getName())).toPath(),
                                        StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                        ChatMessage m = th.getMessageAt(currentPos);
                        //m.setContent(decryptCSAR(message.getContent()));
                        m.setDate(message.getDate());
                        m.setId(message.getId());
                        m.setThread(message.getThread());
                        m.setAttachments(message.getAttachments());
                        m.setIsVoiceMessage(message.isVoiceMessage());
                        //th.push(message);
                        th.setLastActiveTime(m.getDate());
                        th.setLastMessage(m.getContent());
                        th.setUnread(th.getUnread() + 1);
                        th.setMessageCount(th.getMessageCount() + 1);
                        th.setLastSender(mMemberObject.getLogin());
                        SwingUtilities.invokeLater(() -> {
                            mMessages.updateUI();
                        });
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {

                }
            }

            @Override
            public void onFail(Response r) {
                // TODO Auto-generated method stub

            }
        };
        q.execute();
        filesToSend.clear();
        console.println("Sending message to " + currentThread.getOther());
        SwingUtilities.invokeLater(() -> {
            mMessages.updateUI();
            mChatThreadListView.updateUI();
        });
    }

    private String encryptCSAR(final String text) {
        if (this.csarKey != null) {
            return CSAREncryption.encrypt(text, CSAREncryption.ALPHABET, this.csarKey);
        }
        return new String(text);
    }

    private String decryptCSAR(final String text) {
        if (csarKey != null) {
            return CSAREncryption.decrypt(text, CSAREncryption.ALPHABET, this.csarKey);
        }
        return new String(text);
    }

    private void setPublicKey() {
        MCQuery q = new MCQuery("setRSAKey");
        PublicKey data = rsaKeys.getPublic();
        RSAPublicKeySpec spec = RSAEncryption.KeyManager.getPublicKeySpecification(data);
        BigInteger mod = spec.getModulus();
        BigInteger exp = spec.getPublicExponent();
        q.data.put("mod", mod.toString());
        q.data.put("exp", exp.toString());
        q.execute();
    }

    private void loadKeys() {
        if (isAuthentificated) {
            MCQuery q = new MCQuery("getPublicKey");
            q.callback = new OnMCQueryResult() {
                @Override
                public void onSuccess(Response r) {
                    // TODO Auto-generated method stub
                    if (r.status == MyStandarMessageType.OK) {
                        JsonNode data = (JsonNode) r.data;
                        MCQuery q = new MCQuery("getCSARKey");
                        BigInteger mod = new BigInteger(data.get("mod").asText());
                        BigInteger exp = new BigInteger(data.get("exp").asText());
                        System.out.println("Mod : " + mod);
                        System.out.println("Exp : " + exp);
                        publicKey = RSAEncryption.KeyManager.readPublicKey(mod, exp);
                        q.callback = new OnMCQueryResult() {
                            @Override
                            public void onSuccess(Response rep) {
                                // TODO Auto-generated method stub
                                if (rep.status == MyStandarMessageType.OK) {
                                    JsonNode n = (JsonNode) rep.data;
                                    System.out.println("nnn: " + n);
                                    String keyStr = (n.get("key").asText());
                                    System.out.println("KeyStr:: : " + keyStr);
                                    csarKey = new String(RSAEncryption.decrypt(Base64.getDecoder().decode(keyStr), rsaKeys.getPrivate()));
                                    System.out.println("Key : " + csarKey);
                                }
                            }

                            @Override
                            public void onFail(Response rep) {
                                // TODO Auto-generated method stub

                            }
                        };
                        q.execute();
                    }
                }

                @Override
                public void onFail(Response r) {
                    // TODO Auto-generated method stub

                }
            };
            q.execute();
        }
    }

    public static void registerForNewMessageReception(OnNewMessageListener l) {
        synchronized (messageListeners) {
            messageListeners.add(l);
        }
    }

    public static void removeFromNewMessageReception(OnNewMessageListener l) {
        synchronized (messageListeners) {
            messageListeners.remove(l);
        }
    }

    public static void registerCallListener(CallListener l) {
        synchronized (callListeners) {
            callListeners.add(l);
        }
    }

    public static void removeCallListener(CallListener l) {
        synchronized (callListeners) {
            callListeners.remove(l);
        }
    }

    public static void registerForMemberConnection(OnMemberConnectionListener l) {
        synchronized (membersConnectionListener) {
            membersConnectionListener.add(l);
        }
    }

    public static void removeFromMemberConnection(OnMemberConnectionListener l) {
        synchronized (membersConnectionListener) {
            membersConnectionListener.remove(l);
        }
    }

    private void dispatchMemberConnection(Membre m) {
        for (OnMemberConnectionListener l : membersConnectionListener) {
            l.onMemberConnected(m);
        }
    }

    private void dispatchMemberDisconnection(Membre m) {
        for (OnMemberConnectionListener l : membersConnectionListener) {
            l.onMemberDisconnection(m);
        }
    }

    private void dispatchNewMessage(ChatThread th, ChatMessage m) {
        System.out.println("Dispatching message on thread " + th.getId() + " : m " + m.getThread());
        for (OnNewMessageListener l : messageListeners) {
            l.onNewMessage(th, m);
        }
    }

    @Override
    public void onMemberConnected(Membre m) {
        // TODO Auto-generated method stub
        Membre membre = mMemberList.get(m.getLogin());
        if (membre == null) {
            //membre non existant
            mMemberList.add(m);
            console.println(m.getLogin() + " is connected now");
        } else {
            //membre existant
            membre.setOnline(m.isOnline());
        }
        for (ChatThread t : mChatThreadList) {
            if (t.getOther().equals(m.getLogin())) {
                t.setIsOnline(true);
                System.out.println(t + " is on line");
                break;
            }
        }
        mChatThreadListView.updateUI();
        mMemberListView.updateUI();
//		SwingUtilities.invokeLater(()->{
//		});
    }

    @Override
    public void onMemberDisconnection(Membre m) {
        // TODO Auto-generated method stub
        Membre membre = mMemberList.get(m.getLogin());
        if (!m.isOnline())
            console.println(m.getLogin() + " is disconnected");
        for (ChatThread t : mChatThreadList) {
            if (t.getOther().equals(m.getLogin())) {
                t.setIsOnline(m.isOnline());
                break;
            }
        }
        membre.setOnline(m.isOnline());
        SwingUtilities.invokeLater(() -> {
            mChatThreadListView.updateUI();
            mMemberListView.updateUI();
        });
    }

    @Override
    public void onNewMessage(ChatThread th, ChatMessage m) {
        // TODO Auto-generated method stub
        System.out.println("MContent: " + m.getContent());
        m.setContent(decryptCSAR(m.getContent()));
        System.out.println("MContent DEC: " + m.getContent());
        if (th.size() > 0) {
            th.push(m);
            if (m.getAttachments() != null)
                for (Attachment a : m.getAttachments()) {
                    getAttachement(a);
                }
        }
        messageReceivedClip.replay();
        th.setDate(m.getDate());
        th.setLastActiveTime(m.getDate());
        th.setLastMessage(m.getContent());
        th.setMessageCount(th.getMessageCount() + 1);
        th.setLastSender(th.getOther());
        th.setUnread(th.getUnread() + 1);
        if (th == currentThread) {
            SwingUtilities.invokeLater(() -> {
                mMessages.updateUI();
            });
        }
        SwingUtilities.invokeLater(() -> {
            mChatThreadListView.updateUI();
        });
    }

    private void loadMore(SecondChatThread th) {
        if (th == currentThread && isLoading)
            return;
        th.setLoadingMore(true);
        isLoading = true;
        MCQuery q = new MCQuery("getMessagesOffset");
        q.data.put("other", th.getOther());
        q.data.put("offset", th.size());
        q.data.put("number", th.SIZE);
        q.callback = new OnMCQueryResult() {
            @Override
            public void onSuccess(Response r) {
                // TODO Auto-generated method stub
                if (r.status == MyStandarMessageType.OK) {
                    isLoading = false;
                    //messages fetched
                    try {
                        ChatMessage[] messages = mapper.readValue(r.data.toString(), ChatMessage[].class);
                        if (messages.length > 0) {
                            if (th != null) {
                                for (int i = 0; i < messages.length; i++) {
                                    ChatMessage cm = messages[i];
                                    cm.setContent(decryptCSAR(cm.getContent()));
                                    th.unshift(cm);
                                    for (Attachment a : cm.getAttachments()) {
                                        if (!Utils.existInLocal(a.getPath()))
                                            getAttachement(a);
                                    }
                                }
                                if (th == currentThread)
                                    SwingUtilities.invokeLater(() -> {
                                        mMessages.updateUI();
                                        mMessages.ensureIndexIsVisible(th.PAGE);
                                    });
                            } else {
                                //we just ignore the reuslt
                            }
                            //mMessages.setModel(th.getDataModel());
                        }
                        th.PAGE++;
                        th.setLoadingMore(false);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFail(Response r) {
                // TODO Auto-generated method stub

            }
        };
        q.execute();
    }

    @Override
    public void onMessageSent(ChatThread th, ChatMessage cm) {
        // TODO Auto-generated method stub
        //th.push(cm);
        th.setUnread(th.getUnread() + 1);
        if (th.size() > 0) {
            th.push(cm);
            for (Attachment a : cm.getAttachments())
                getAttachement(a);
        }
        messageSentClip.replay();
        cm.setSeen(false);
        th.setDate(cm.getDate());
        th.setLastActiveTime(cm.getDate());
        th.setLastMessage(cm.getContent());
        th.setMessageCount(th.getMessageCount() + 1);
        th.setLastSender(mMemberObject.getLogin());
        th.setUnread(th.getUnread() + 1);
        if (th == currentThread) {
            SwingUtilities.invokeLater(() -> {
                mMessages.updateUI();
            });
        }
        SwingUtilities.invokeLater(() -> {
            mChatThreadListView.updateUI();
            console.println("Message sent on thread " + th);
        });
    }

    @Override
    public void onMessageSeen(ChatThread th) {
        // TODO Auto-generated method stub
        System.out.println("Thid: " + th);
        if (true) {
            th.setUnread(0);
            th.markAllAsSeen();
            if (th == currentThread)
                SwingUtilities.invokeLater(() -> {
                    mMessages.updateUI();
                });
            SwingUtilities.invokeLater(() -> {
                mChatThreadListView.updateUI();
                console.println("Message seen on thread " + th);
            });
        }
    }

    private boolean profilExistInLocal(String pr) {
        File f = new File(Client.USERS_FILE_DATA_DIR + "/" + (new File(pr).getName()));
        return f.exists();
    }

    @Override
    public void onCallFrom(ChatThread m) {
        // TODO Auto-generated method stub
        //System.out.println("OnCallFrom "+m.getOther());
        callFromClip.play(true);
        callDialog = new CallDialog(this, m, CallDialog.ENTERING_CALL, new CallDialog.OnCloseListener() {
            @Override
            public void onCancel(ChatThread th) {
                // TODO Auto-generated method stub
                System.out.println("Cancel ENTERING_CALL");
                callFromClip.stop();
                SwingUtilities.invokeLater(() -> {
                    callDialog.close();
                });
                MCQuery q = new MCQuery("cancelCall");
                q.data.put("other", th.getOther());
                q.execute();
            }

            @Override
            public void onAccept(ChatThread th) {
                // TODO Auto-generated method stub
                MCQuery q = new MCQuery("answerCall");
                callFromClip.stop();
                q.data.put("other", th.getOther());
                q.callback = new OnMCQueryResult() {
                    @Override
                    public void onSuccess(Response r) {
                        // TODO Auto-generated method stub
                        String label = ((JsonNode) r.data).get("label").asText();
                        System.out.println("Le label : " + label);
                        //SwingUtilities.invokeLater(()->{
//							if(callDialog!=null)
//								callDialog.openVoipSocket(Main.VOIP_PORT, client.address(), label);
                        //});
                        new Thread(() -> {
                            if (callDialog != null)
                                callDialog.openVoipSocket(Main.VOIP_PORT, client.address(), label);
                        }).start();

                    }

                    @Override
                    public void onFail(Response r) {
                        // TODO Auto-generated method stub

                    }
                };
                q.execute();

            }
        });
        SwingUtilities.invokeLater(() -> {
            callDialog.setVisible(true);
        });
    }

    @Override
    public void onCallAccepted(ChatThread m, String label) {
        // TODO Auto-generated method stub
        System.out.println("Call accepted :" + label);
        callFromClip.stop();
        callClip.stop();
        //SwingUtilities.invokeLater(()->{
//			if(callDialog!=null)
//				callDialog.openVoipSocket(Main.VOIP_PORT, client.address(), label);
        //});
        new Thread(() -> {
            if (callDialog != null)
                callDialog.openVoipSocket(Main.VOIP_PORT, client.address(), label);
        }).start();
    }

    @Override
    public void onCallCanceled(ChatThread m) {
        // TODO Auto-generated method stub
        callFromClip.stop();
        callClip.stop();
        System.out.println("Call finished");
        if (callDialog != null)
            callDialog.close();

    }

    @Override
    public void onCallFinished(ChatThread c) {
        // TODO Auto-generated method stub
        callFromClip.stop();
        callClip.stop();
        System.out.println("Call finished");
    }

    class ScrollListener implements AdjustmentListener {
        private SecondChatThread th;

        public ScrollListener(SecondChatThread t) {
            // TODO Auto-generated constructor stub
            th = t;
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent ev) {
            // TODO Auto-generated method stub
            if (ev.getValueIsAdjusting()) {
                return;
            }

            //System.out.println("Value "+ev.getValue());
            if (ev.getValue() == 0 && !isLoading) {
                if (th != null && th.size() < th.getMessageCount() && th.size() > 0) {
                    th.SIZE = 10;
                    loadMore(th);
                }
            }
        }
    }
}
