package com.ips.TestEpos;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import akka.actor.ActorSystem;

/**
 * Hello world!
 *
 */
public class App {
	
	public static volatile int successful = 0;
	public static volatile int error = 0;
	public static volatile int total;
	public static String terminalIp ;
	public static String terminalPort;
	private final static ActorSystem system = ActorSystem.create();
    
	
	public static void main( String[] args ) throws InterruptedException{
		terminalIp = args[2];
		terminalPort = args[3];
		total = Integer.parseInt(args[4]);
		
		if(args[5].equalsIgnoreCase("S")){
			System.out.println("running serially");
			IntStream.range(1, total+1)/*.parallel()*/.forEach(i -> {
					pedBalance(args[0],args[1],i);	
					try {
						TimeUnit.MILLISECONDS.sleep(50);
					} catch (InterruptedException e) {
						System.exit(1);
					}
			});
		}else if(args[5].equalsIgnoreCase("P")){
			System.out.println("running parallely");
			IntStream.range(1, total+1).parallel().forEach(i -> {
				pedBalance(args[0],args[1],i);	
		});
		}
    }


	private static void pedBalance(String ip,String port, int count) {
		system.actorOf(tcpClient.props(new InetSocketAddress(ip, Integer.parseInt(port))),"TCP-"+count);
    	
		
	}


}