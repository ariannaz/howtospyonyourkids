package com.example.testapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.util.Log;

/** Runs the client code that connects to a particular socket on the server. */
public class ClientSocket {

	// Default to localhost
	private String serverIpAddress = "127.0.0.1";

	private int serverPort = 3490;

	private String message = "";

	public void setServerIpAddress(String ip) {
		serverIpAddress = ip;
	}

	public void setServerPort(int port) {
		serverPort = port;
	}

	public String getServerIpAddress() {
		return serverIpAddress;
	}

	public int getServerPort() {
		return serverPort;
	}

	private transient String responseMessage = "";

	public String getResponse() {
		final int MAX_TRIES = 40;
		int tries;
		for (tries = 0; tries < MAX_TRIES && responseMessage.equals(""); tries++) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Log.e("ClientActivity", "C: Error", e);
			}
		}
		if (tries >= MAX_TRIES) {
			return "\nCould not connect";
		}
		String responseCopy = responseMessage;
		responseMessage = "";
		return responseCopy;
	}

	public void sendData(String s) {
		message = s;
		if (!serverIpAddress.equals("")) {
			Thread cThread = new Thread(new ClientThread());
			cThread.start();
		} else {
			responseMessage = "Please enter a non-empty IP address";
		}
	}

	public class ClientThread implements Runnable {

		public void run() {
			try {
				InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
				Log.d("ClientActivity", "C: Connecting...");
				Socket socket = new Socket(serverAddr, serverPort);
				try {
					Log.d("ClientActivity", "C: Sending command.");
					PrintWriter out = new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(socket.getOutputStream())),
							true);
					out.print(message);
					// Need to flush since no newline is forced
					out.flush();
					Log.d("ClientActivity", "C: Sent message: " + message);

					// Wait to receive confirmation from server before closing
					BufferedReader in = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));

					// Can only send one-line messages
					while ((responseMessage = in.readLine()) != null) {
						Log.d("ClientActivity", "C: Received message: "
								+ responseMessage);
						break;
					}
				} catch (Exception e) {
					Log.e("ClientActivity", "S: Error", e);
				}

				// close socket
				socket.close();
				Log.d("ClientActivity", "C: Closed.");
			} catch (Exception e) {
				Log.e("ClientActivity", "C: Error", e);
			}
		}
	}
}
