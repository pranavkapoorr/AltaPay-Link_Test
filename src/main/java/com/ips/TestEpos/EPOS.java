package com.ips.TestEpos;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.stream.IntStream;

public class EPOS {
	private static final String terminalIp = "81.144.248.51";
	private static final String terminalPort = "40003";
	private static final String serviceIp = "94.236.18.34";
	private static final int servicePort = 40001;
	public static volatile int successful = 0;
	public static volatile int error = 0;
	public static volatile int total = 1000;
public static void main(String[] args){
	Runnable task = new Runnable() {
		
		@Override
		public void run() {
			try {
				pedBalance();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
			
		}
	};
	IntStream.range(1, total+1).parallel().forEach(i -> {
		
			new Thread(task).start();
		
	});
}
protected static void pedBalance() throws UnknownHostException, IOException {
	Socket sock = new Socket("192.168.0.160", servicePort);
	DataInputStream in = new DataInputStream(sock.getInputStream());
	DataOutputStream out = new DataOutputStream(sock.getOutputStream());
	String msg = "{\"printFlag\":\"1\",\"operationType\":\"PedBalance\",\"pedIp\":\""+terminalIp+"\",\"pedPort\":\""+terminalPort+"\",\"timeOut\":\"60\"}";
	out.write(msg.getBytes());
	do{
		
		System.out.println(in.read());
	}while(in.readUTF().contains("Transaction Successful")||in.readUTF().contains("error"));
	sock.close();
}
}
