package com.example.testapp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/** Runs the server code that can service multiple connecting clients simultaneously. */
public class ServerSocket {

	public class ServerThread implements Runnable {
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

							System.err.println("Client connected: "
									+ client.toString());
						}
						ByteBuffer readBuf = ByteBuffer.allocate(bufSize);
						if (key.isReadable()) {
							SocketChannel client = (SocketChannel) key
									.channel();
							int bytes = client.read(readBuf);
							if (bytes == -1) {
								System.err.println("Closing client: "
										+ client.toString());
								client.close();
								continue;
							}
							System.err.println("Received message: "
									+ new String(readBuf.array()));

							client.register(selector, SelectionKey.OP_READ
									| SelectionKey.OP_WRITE);
							key.attach(readBuf);
						}
						if (key.isWritable()) {
							SocketChannel client = (SocketChannel) key
									.channel();
							ByteBuffer writeBuf = (ByteBuffer) key.attachment();
							writeBuf.flip();
							client.write(writeBuf);
							System.err.println("Sent message: "
									+ new String(writeBuf.array()));

							client.register(selector, SelectionKey.OP_READ);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					selector.close();
					server.socket().close();
					server.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void start() {
		Thread cThread = new Thread(new ServerThread());
		cThread.start();
	}
}