package com.tachestudios.soundsync;

import java.io.IOException;
import java.util.Scanner;

import com.tachestudios.soundsync.client.Client;
import com.tachestudios.soundsync.server.Server;
import com.tachestudios.soundsync.soundcloud.SoundCloudHandler;

public class Main {

	static String host;
	static int port;

	public static void main(String[] args) {
		SoundCloudHandler.init();
		if (args.length == 0) {
			printHelp();
			System.exit(1);
		}
		if (args[0].equals("-s")) {
			Integer port = 10422;
			if (args.length == 2) {
				port = Integer.parseInt(args[1]);
			}
			Main.port = port;
			try {
				runServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (args[0].equals("-c")) {
			if (args.length < 2) {
				printHelp();
			} else {
				String host = args[1];
				Integer port = 10422;
				if (args.length == 3) {
					port = Integer.parseInt(args[2]);
				}
				Main.host = host;
				Main.port = port;
				try {
					runClient();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void printHelp() {
		System.out.println("Invalid syntax:");
		System.out.println("Usage: soundsync -c <host> [port] or soundsync -s [port]");
	}

	public static void runServer() throws IOException {
		System.out.println("Starting server on port " + port);
		Server server = new Server(port);
		Thread scanner = new Thread() {
			public void run() {
				Scanner s = new Scanner(System.in);
				while (true) {
					String entered = s.nextLine();
					if (entered.equals("EXIT")) {
						s.close();
						System.exit(0);
						break; // unreachable
					}
					server.sendToAll(entered); // send in URLs from soundcloud
				}
			}
		};
		scanner.start();
	}

	@SuppressWarnings("unused")
	public static void runClient() throws IOException {
		System.out.println("Starting client with host " + host + " and port " + port);
		Client client = new Client(host, port);
	}

}
