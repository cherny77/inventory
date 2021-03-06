package inventory.shared.impl;

import inventory.shared.api.IProcessor;
import inventory.shared.api.ISender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StoreServerTCP extends Thread {
	private ServerSocket serverSocket;
	private Map<InetAddress, TCPSocketThread> socketThreads = new ConcurrentHashMap<>();
	private volatile boolean running;
	private IProcessor processor;

	public StoreServerTCP(int port, String key, IProcessor processor) throws IOException {
		serverSocket = new ServerSocket(port);
		ISender sender = new Sender(new Encryptor(key));
		processor.setSender(sender);
		this.processor = processor;
	}

	public void run() {
		running = true;
		System.out.println("SERVER STARTED");
		while (running) {
			try {
				Socket clientSocket = serverSocket.accept();
				TCPSocketThread socketThread = new TCPSocketThread(clientSocket, processor);
				InetAddress inetAddress = clientSocket.getInetAddress();
				socketThreads.put(inetAddress, socketThread);
				socketThread.start();
			} catch (IOException ignore) {
			}
		}
	}

	public void close() throws IOException {
		running = false;
		for (TCPSocketThread tcpSocketThread : socketThreads.values())
			tcpSocketThread.close();
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class Sender implements ISender {
		Encryptor encryptor;

		public Sender(Encryptor encryptor) {
			this.encryptor = encryptor;
		}

		@Override
		public void sendMessage(Packet packet, InetAddress target) {
			TCPSocketThread tcpSocketThread = socketThreads.get(target);
			if (tcpSocketThread != null) {
				System.out.println("Send message: " + packet);
				tcpSocketThread.send(packet.encode());
			} else {
				System.out.println("Incoming connection is not found!");
			}
		}
	}

}
