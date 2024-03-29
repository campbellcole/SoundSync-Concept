package com.tachestudios.soundsync.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import com.tachestudios.soundsync.soundcloud.SoundCloudHandler;

public class Client {
	private ConnectionToServer server;
	private LinkedBlockingQueue<Object> messages;
	private Socket socket;

	public Client(String IPAddress, int port) throws IOException {
		socket = new Socket(IPAddress, port);
		messages = new LinkedBlockingQueue<Object>();
		server = new ConnectionToServer(socket);

		Thread messageHandling = new Thread() {
			public void run() {
				while (true) {
					try {
						Object message = messages.take();
						String command = (String) message;
						System.out.println("Message Received: " + message);
						switch (command.split(" ")[0]) {
						case "PLAY":
							System.out.println("Playing song from URL " + command.split(" ")[1]);
							SoundCloudHandler.playSong(SoundCloudHandler.getTrackFromUrl(command.split(" ")[1]));
							break;
						case "PLAYNEXT":
							System.out.println("Playing song next from URL " + command.split(" ")[1]);
							SoundCloudHandler.playSongNext(command.split(" ")[1]);
							break;
						}
					} catch (InterruptedException e) {
					}
				}
			}
		};

		messageHandling.start();
	}

	private class ConnectionToServer {
		ObjectInputStream in;
		ObjectOutputStream out;
		@SuppressWarnings("unused")
		Socket socket;

		ConnectionToServer(Socket socket) throws IOException {
			this.socket = socket;
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());

			Thread read = new Thread() {
				public void run() {
					while (true) {
						try {
							Object obj = in.readObject();
							messages.put(obj);
						} catch (IOException | ClassNotFoundException | InterruptedException e) {
							System.out.println("cannot connect to server. exiting.");
							System.exit(1);
							break; // unreachable
						}
					}
				}
			};

			read.start();
		}

		private void write(Object obj) {
			try {
				out.writeObject(obj);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void send(Object obj) {
		server.write(obj);
	}
}
