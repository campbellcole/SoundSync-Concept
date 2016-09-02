package com.tachestudios.soundsync.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
	private ArrayList<ConnectionToClient> clientList;
	private LinkedBlockingQueue<Object> messages;
	private ServerSocket serverSocket;

	public Server(int port) {
		clientList = new ArrayList<ConnectionToClient>();
		messages = new LinkedBlockingQueue<Object>();
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Thread accept = new Thread() {
			public void run() {
				while (true) {
					try {
						Socket s = serverSocket.accept();
						clientList.add(new ConnectionToClient(s));
						System.out.println("Client Connected...");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};

		accept.start();

		Thread messageHandling = new Thread() {
			public void run() {
				while (true) {
					try {
						Object message = messages.take();
						// Do some handling here...
						System.out.println("Message Received: " + message);
					} catch (InterruptedException e) {
					}
				}
			}
		};

		messageHandling.start();
	}

	private class ConnectionToClient {
		ObjectInputStream in;
		ObjectOutputStream out;
		Socket socket;
		ConnectionToClient self;

		ConnectionToClient(Socket socket) throws IOException {
			this.socket = socket;
			out = new ObjectOutputStream(socket.getOutputStream());
			self = this;

			Thread read = new Thread() {
				public void run() {
					try {
						in = new ObjectInputStream(socket.getInputStream());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					while (true) {
						try {
							Object obj = in.readObject();
							messages.put(obj);
						} catch (IOException | ClassNotFoundException | InterruptedException e) {
							try {
								self.socket.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							clientList.remove(self);
							System.out.println("Client Disconnected...");
							break;
						}
					}
				}
			};
			read.start();
		}

		public void write(Object obj) {
			try {
				out.writeObject(obj);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendToOne(int index, Object message) throws IndexOutOfBoundsException {
		clientList.get(index).write(message);
	}

	public void sendToAll(Object message) {
		for (ConnectionToClient client : clientList) {
			client.write(message);
		}
	}

}