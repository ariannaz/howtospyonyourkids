package com.example.testapp;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.util.Log;

/**
 * Runs the server code that can service multiple connecting clients
 * simultaneously.
 */
public class ServerSocket {

	public final static String CMD_PREFIX = "/";

	public class ByteBufferAttachment {
		public ByteBufferAttachment() {
			byteBuffers = new ArrayList<ByteBuffer>();
		}

		public ArrayList<ByteBuffer> byteBuffers;
	}

	public class ServerThread implements Runnable {
		private Map<ClientDevice, Integer> deviceMap = new HashMap<ClientDevice, Integer>();

		int registerClient(String clientString, Socket client) {
			ClientDevice device = new ClientDevice(clientString);
			if (!deviceMap.containsKey(device)) {
				device.id = deviceMap.size();
				device.ip = client.getInetAddress();
				device.port = client.getPort();

				Log.d("ServerActivity",
						"S: registered new client device " + device.name
								+ " with hash " + Long.toHexString(device.hash)
								+ " with id: " + device.id);

				deviceMap.put(device, device.id);
				return device.id;
			} else {
				return deviceMap.get(device);
			}
		}

		public void run() {

			final int port = 3490;

			final int bufSize = 128;

			Selector selector = null;
			ServerSocketChannel server = null;
			try {
				selector = Selector.open();
				server = ServerSocketChannel.open();
				server.socket().bind(new InetSocketAddress(port));
				server.configureBlocking(false);
				server.register(selector, SelectionKey.OP_ACCEPT);
				while (true) {
					selector.select();
					for (Iterator<SelectionKey> i = selector.selectedKeys()
							.iterator(); i.hasNext();) {
						SelectionKey key = i.next();
						i.remove();
						if (key.isConnectable()) {
							((SocketChannel) key.channel()).finishConnect();
						}
						if (key.isAcceptable()) {
							// accept connection
							SocketChannel client = server.accept();
							client.configureBlocking(false);
							client.socket().setTcpNoDelay(true);
							client.register(selector, SelectionKey.OP_READ);

							Log.d("ServerActivity", "S: client connected: "
									+ client.socket().getInetAddress() + " "
									+ client.socket().getPort());
						}
						if (key.isReadable()) {
							SocketChannel client = (SocketChannel) key
									.channel();
							ByteBuffer readBuf = ByteBuffer.allocate(bufSize);
							int bytes = client.read(readBuf);
							if (bytes == -1) {
								Log.d("ServerActivity", "S: closing client: "
										+ client.socket().getInetAddress()
										+ " " + client.socket().getPort());
								client.close();
								continue;
							}

							// Resize to the correct size
							ByteBuffer storeBuf = ByteBuffer.allocate(bytes);
							for (int c = 0; c < bytes; c++) {
								storeBuf.put(readBuf.get(c));
							}

							String receivedMessage = new String(
									storeBuf.array());

							Log.d("ServerActivity", "S: received " + bytes
									+ " bytes");
							Log.d("ServerActivity", "S: received message: "
									+ receivedMessage);

							String replyMessage;

							if (receivedMessage.startsWith(CMD_PREFIX)) {
								replyMessage = "Sorry, I cannot "
										+ receivedMessage.substring(CMD_PREFIX
												.length()) + '\n';
							} else {

								int id = registerClient(receivedMessage,
										client.socket());

								replyMessage = "Client #" + id + " confirmed\n";
							}

							ByteBuffer sendBuf = ByteBuffer
									.allocate(replyMessage.length());

							for (char c : replyMessage.toCharArray()) {
								sendBuf.put((byte) c);
							}

							client.register(selector, SelectionKey.OP_READ
									| SelectionKey.OP_WRITE);

							if (key.attachment() instanceof ByteBufferAttachment) {
								ByteBufferAttachment attachment = (ByteBufferAttachment) key
										.attachment();
								attachment.byteBuffers.add(sendBuf);
								key.attach(attachment);
							} else {
								ByteBufferAttachment attachment = new ByteBufferAttachment();
								attachment.byteBuffers.add(sendBuf);
								key.attach(attachment);
							}
						}
						if (key.isWritable()) {
							SocketChannel client = (SocketChannel) key
									.channel();
							if (key.attachment() instanceof ByteBufferAttachment) {
								ByteBufferAttachment attachment = (ByteBufferAttachment) key
										.attachment();
								for (ByteBuffer writeBuf : attachment.byteBuffers) {
									writeBuf.flip();
									client.write(writeBuf);
									Log.d("ServerActivity", "S: sent message: "
											+ new String(writeBuf.array()));
								}
								attachment.byteBuffers.clear();
							} else {
								Log.d("ServerActivity", "S: unknown attachment");
							}

							client.register(selector, SelectionKey.OP_READ);
						}
					}
				}
			} catch (Exception e) {
				Log.e("ServerActivity", "S: Error", e);
			} finally {
				try {
					selector.close();
					server.socket().close();
					server.close();
				} catch (Exception e) {
					Log.e("ServerActivity", "S: Error trying to destruct", e);
				}
			}
		}
	}

	public void launch() {
		Thread cThread = new Thread(new ServerThread());
		cThread.start();
	}
}
