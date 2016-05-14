package controller;

import java.io.IOException;

import model.Model;
import view.View;

public class Controller {
	/**
	 * Model
	 */
	private Model model;

	/**
	 * GUI
	 */
	private View view;

	/**
	 * Controller Constructor
	 * 
	 * @throws IOException
	 */
	public Controller() {
		view = new View(this);
		try {
			model = new Model(view);
		} catch (IOException e) {
			System.out.println("Server failed to start!");
		}
	}

	/**
	 * Start View
	 */
	public void run() {
		view.start();
	}

	/**
	 * Set File Name
	 * 
	 * @param fileName
	 *            File to be sent
	 */
	public void setFile(String fileName) {
		model.setFile(fileName);
	}

	/**
	 * Receive File
	 * 
	 * @param host
	 *            File Server Host
	 * @param fileName
	 *            File to be saved
	 */
	public void getFile(String host, String fileName) {
		try {
			model.receiveFile(host, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send Message to Message Server
	 * 
	 * @param message
	 *            Message
	 */
	public void sendMessage(String message) {
		model.sendMessage(message);
	}

	/**
	 * Connect to a Message Server
	 * 
	 * @param host
	 *            Message Server
	 */
	public void connectMessageServer(String host) {
		try {
			model.connectMessageServer(host);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
