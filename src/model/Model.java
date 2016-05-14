package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import view.View;

/**
 * Model
 * 
 * @author Geurney
 *
 */
public class Model {
	/**
	 * Message Service Port
	 */
	private static final int MESSAGE_PORT = 9090;

	/**
	 * File Service Port
	 */
	private static final int FILE_PORT = 8080;

	/**
	 * View
	 */
	private View view;

	/**
	 * Local host name
	 */
	private String name;

	/**
	 * Local ip
	 */
	private String ip;

	/**
	 * Message Server
	 */
	private final MessageServer mServer;

	/**
	 * File Server
	 */
	private final FileServer fServer;

	/**
	 * Message Client
	 */
	private MessageClient mClient;

	/**
	 * Get current time
	 * 
	 * @return Current Time
	 */
	public static String getTime() {
		return new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance()
				.getTime());
	}

	/**
	 * Model Constructor
	 * 
	 * @param view
	 *            View
	 * @throws IOException
	 *             Connection Exception
	 */
	public Model(View view) throws IOException {
		this.view = view;
		name = InetAddress.getLocalHost().getHostName();
		ip = InetAddress.getLocalHost().getCanonicalHostName();
		mServer = new MessageServer();
		fServer = new FileServer();
	}

	/**
	 * Get local host name
	 * 
	 * @return Local host name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get local IP
	 * 
	 * @return Local IP
	 */
	public String getIP() {
		return ip;
	}

	/**
	 * File Server
	 * 
	 * @author Geurney
	 *
	 */
	private class FileServer {
		/**
		 * File Server Socket
		 */
		ServerSocket serverSocket;
		/**
		 * File Server listening thread
		 */
		Listen listenService;
		/**
		 * File to be sent
		 */
		File file;

		FileServer() throws IOException {
			serverSocket = new ServerSocket(FILE_PORT);
			listenService = new Listen();
			listenService.start();
		}

		/**
		 * Set file name
		 * 
		 * @param fileName
		 *            File name
		 */
		void setFile(String fileName) {
			file = new File(fileName);
			if (!file.exists()) {
				System.out.println("File not found" + file.getAbsolutePath());
				file = null;
			} else if (file.length() > 200000) {
				System.out.println("File too big : " + file.length());
				// file = null;
			}
		}

		/**
		 * Close file server socket
		 * 
		 * @throws IOException
		 */
		void close() throws IOException {
			serverSocket.close();
		}

		/**
		 * Listening Thread
		 * 
		 * @author Geurney
		 *
		 */
		class Listen extends Thread {
			void sendFile(InputStream file, OutputStream out)
					throws IOException {
				byte[] buffer = new byte[16 * 1024];
				while (file.available() > 0) {
					out.write(buffer, 0, file.read(buffer));
				}
				out.flush();
			}

			@Override
			public void run() {
				while (true) {
					Socket socket;
					try {
						socket = serverSocket.accept();
						if (file == null) {
							continue;
						}
						FileInputStream in = new FileInputStream(file);
						sendFile(in, socket.getOutputStream());
						in.close();
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Receive File
	 * 
	 * @param host
	 *            File server host
	 * @param saveFile
	 *            Save file name
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void receiveFile(String host, String saveFile)
			throws UnknownHostException, IOException {
		Socket socket = new Socket(host, FILE_PORT);
		InputStream in = socket.getInputStream();
		FileOutputStream out = new FileOutputStream(saveFile);
		byte[] byteArray = new byte[16 * 1024];
		int count;
		while ((count = in.read(byteArray)) > 0) {
			out.write(byteArray, 0, count);
		}
		in.close();
		out.close();
		socket.close();
	}

	/**
	 * Message Server
	 * 
	 * @author Geurney
	 *
	 */
	private class MessageServer {
		ServerSocket serverSocket;
		ArrayList<PrintWriter> clientWriters;

		MessageServer() throws IOException {
			serverSocket = new ServerSocket(MESSAGE_PORT);
			clientWriters = new ArrayList<PrintWriter>();
			new Listen().start();
		}

		/**
		 * Get connected client ip
		 * 
		 * @param socket
		 *            Connected socket
		 * @return Client ip
		 */
		String getConnectedClient(Socket socket) {
			return socket.getInetAddress().getCanonicalHostName();
		}

		/**
		 * Close Message Server socket
		 * 
		 * @throws IOException
		 */
		void close() throws IOException {
			serverSocket.close();
		}

		/**
		 * Message Server Listening Thread
		 * 
		 * @author Geurney
		 */
		class Listen extends Thread {
			@Override
			public void run() {
				try {
					while (true) {
						Socket socket = serverSocket.accept();
						PrintWriter writer = new PrintWriter(
								socket.getOutputStream(), true);
						clientWriters.add(writer);
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(socket.getInputStream()));
						new ServerService(reader).start();
						view.updateConnectedIP(getConnectedClient(socket));
					}
				} catch (IOException e) {
				} finally {
					try {
						serverSocket.close();
					} catch (IOException e) {
					}
				}
			}

			/**
			 * Sever BroadCast Service
			 * 
			 * @author Geurney
			 *
			 */
			class ServerService extends Thread {
				BufferedReader reader;

				ServerService(BufferedReader reader) {
					this.reader = reader;
				}

				@Override
				public void run() {
					String message = null;
					try {
						while ((message = reader.readLine()) != null) {
							broadCast(message);
						}
					} catch (IOException e) {
						return;
					}
				}

				/**
				 * BroadCast message to all sockets.
				 * 
				 * @param message
				 *            Message
				 */
				private void broadCast(String message) {
					Iterator<PrintWriter> it = clientWriters.iterator();
					while (it.hasNext()) {
						PrintWriter writer = it.next();
						writer.println(message);
						if (writer.checkError()) {
							it.remove();
						}
					}
				}
			}
		}
	}

	/**
	 * Message Client
	 * 
	 * @author Geurney
	 *
	 */
	private class MessageClient {
		private BufferedReader reader;
		private PrintWriter writer;
		private Socket socket;

		MessageClient(String host) {
			try {
				socket = new Socket(host, MESSAGE_PORT);
				reader = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				new ReadService().start();
				writer = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				System.out.println("Connection to server failed!");
			}
		}

		/**
		 * Close Client Socket
		 * 
		 * @throws IOException
		 */
		void close() throws IOException {
			socket.close();
		}

		/**
		 * Send message to server
		 * 
		 * @param message
		 *            Message
		 */
		void send(String message) {
			writer.println(name + "  " + getTime() + ": " + message);
		}

		/**
		 * Client Read Service
		 * 
		 * @author Geurney
		 *
		 */
		class ReadService extends Thread {
			@Override
			public void run() {
				String message = null;
				try {
					while ((message = reader.readLine()) != null) {
						view.updateIncoming(message);
					}
				} catch (IOException e) {
					return;
				}
			}
		}
	}

	/**
	 * Set File
	 * 
	 * @param fileName
	 *            File to be sent
	 */
	public void setFile(String fileName) {
		fServer.setFile(fileName);
	}

	/**
	 * Connect to a message server
	 * 
	 * @param host
	 *            Message Server
	 * @throws IOException
	 */
	public void connectMessageServer(String host) throws IOException {
		if (mClient != null) {
			mClient.close();
		}
		mClient = new MessageClient(host);
	}

	/**
	 * Send Message to message server
	 * 
	 * @param message
	 *            Message
	 */
	public void sendMessage(String message) {
		if (mClient == null) {
			return;
		}
		mClient.send(message);
	}

	/**
	 * Close Client
	 * 
	 * @throws IOException
	 */
	public void closeClient() throws IOException {
		mClient.close();
	}

	/**
	 * Close Message Server
	 * 
	 * @throws IOException
	 */
	public void closeMessageServer() throws IOException {
		mServer.close();
	}

	/**
	 * Close File Server
	 * 
	 * @throws IOException
	 */
	public void closeFileServer() throws IOException {
		fServer.close();
	}

}
