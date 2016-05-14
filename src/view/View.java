package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import controller.Controller;

public class View {
	/**
	 * Controller
	 */
	private Controller controller;
	private JFrame frame;

	/**
	 * Message Box
	 */
	private JTextArea incoming;
	/**
	 * Send Message Field
	 */
	private JTextField outgoing;
	/**
	 * Connected Client List
	 */
	private JTextArea connectedIP;

	/**
	 * Start view
	 */
	public void start() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Update Message Box
	 * 
	 * @param message
	 *            Message
	 */
	public void updateIncoming(String message) {
		incoming.append(message + "\n");
	}

	/**
	 * Update Connected Client
	 * 
	 * @param client
	 *            Client
	 */
	public void updateConnectedIP(String client) {
		connectedIP.append(client + "\n");
	}

	/**
	 * View Constructor
	 * 
	 * @param controller
	 *            Controller
	 */
	public View(Controller controller) {
		this.controller = controller;
		initialize();
	}

	/**
	 * Initialize the view
	 */
	private void initialize() {
		frame = new JFrame("Instant Messager");
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(null);

		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = outgoing.getText();
				if (message == null || message.length() == 0) {
					return;
				}
				controller.sendMessage(message);
				outgoing.setText("");
				outgoing.requestFocus();
			}
		});
		sendButton.setBounds(568, 603, 105, 41);
		mainPanel.add(sendButton);

		outgoing = new JTextField(20);
		outgoing.setBounds(26, 604, 505, 39);
		outgoing.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendButton.doClick();
				}
			}
		});
		mainPanel.add(outgoing);
		incoming = new JTextArea(15, 50);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);

		JScrollPane incomingScroller = new JScrollPane(incoming);
		incomingScroller.setBounds(26, 28, 647, 509);
		incomingScroller
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		incomingScroller
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		mainPanel.add(incomingScroller);
		frame.getContentPane().add(BorderLayout.CENTER, mainPanel);

		JTextField serverIP = new JTextField();
		serverIP.setBounds(780, 60, 205, 39);
		mainPanel.add(serverIP);
		serverIP.setColumns(10);
		JButton connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.connectMessageServer(serverIP.getText());
			}
		});
		connectButton.setBounds(992, 59, 171, 41);
		mainPanel.add(connectButton);

		JScrollPane connectedScrollPane = new JScrollPane((Component) null);
		connectedScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		connectedScrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		connectedScrollPane.setBounds(780, 128, 383, 412);
		mainPanel.add(connectedScrollPane);

		connectedIP = new JTextArea(15, 50);
		connectedIP.setWrapStyleWord(true);
		connectedIP.setLineWrap(true);
		connectedIP.setEditable(false);
		connectedScrollPane.setViewportView(connectedIP);

		JTextField sendFileName = new JTextField();
		sendFileName.setBounds(790, 569, 229, 40);
		mainPanel.add(sendFileName);
		sendFileName.setColumns(10);

		JButton setFileButton = new JButton("Set File");
		setFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = sendFileName.getText().trim();
				if (fileName != null) {
					controller.setFile(fileName);
				}
			}
		});
		setFileButton.setBounds(1018, 568, 145, 41);
		mainPanel.add(setFileButton);

		JTextField fileServerHost = new JTextField();
		fileServerHost.setColumns(10);
		fileServerHost.setBounds(780, 615, 63, 40);
		mainPanel.add(fileServerHost);

		JTextField receiveFileName = new JTextField();
		receiveFileName.setColumns(10);
		receiveFileName.setBounds(880, 615, 105, 40);
		mainPanel.add(receiveFileName);

		JButton receiveFileButton = new JButton("Get File");
		receiveFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String host = fileServerHost.getText().trim();
				String fileName = receiveFileName.getText().trim();
				if (host != null && fileName != null) {
					controller.getFile(host, fileName);
				}
			}
		});
		receiveFileButton.setBounds(1018, 614, 145, 41);
		mainPanel.add(receiveFileButton);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1221, 771);
		frame.setVisible(true);
	}
}
