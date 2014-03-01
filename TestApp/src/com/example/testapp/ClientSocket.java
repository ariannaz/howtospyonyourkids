package com.example.testapp;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.util.Log;

/** Runs the client code for a socket. */
public class ClientSocket  {

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

	public void sendData(String s) {
		message = s;
		if (!serverIpAddress.equals("")) {
			Thread cThread = new Thread(new ClientThread());
			cThread.start();
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
					// where you issue the commands
					out.println(message);
					Log.d("ClientActivity", "C: Sent.");

					Log.d("ClientActivity", "vzhang debug:" + message);
				} catch (Exception e) {
					Log.e("ClientActivity", "S: Error", e);
				}
				socket.close();
				Log.d("ClientActivity", "C: Closed.");
			} catch (Exception e) {
				Log.e("ClientActivity", "C: Error", e);
			}
		}
	}
}