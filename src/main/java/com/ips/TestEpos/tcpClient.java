package com.ips.TestEpos;



import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.*;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.actor.*;
import akka.io.*;
import akka.util.ByteString;
import akka.io.Tcp.*;

public class tcpClient extends AbstractActor {
	
	private final static Logger log = LogManager.getLogger(tcpClient.class);
	    private final ActorRef tcpActor;
	    private final InetSocketAddress remote;
	    String ipport;
	    public static Props props(InetSocketAddress remote) {
	        return Props.create(tcpClient.class, remote);
	    }

	    private tcpClient( InetSocketAddress terminalIPandPort) {
	        this.remote = terminalIPandPort;
	        this.tcpActor = Tcp.get(getContext().system()).manager();
                this.tcpActor.tell(TcpMessage.connect(remote), getSelf());
	      }
	    

		@Override
		public Receive createReceive() {
			return receiveBuilder()
					.match(CommandFailed.class, conn->{
						log.trace("command failed "+conn.causedByString());
						getContext().stop(getSelf());
                        getContext().getSystem().terminate();
                                                
					})
					.match(Connected.class, conn->{
						//log.trace(conn.toString());
						pedBalance(getSelf());
						ipport = conn.localAddress().toString();
                        getSender().tell(TcpMessage.register(getSelf()), getSelf());
                        getContext().become(connected(getSender()));
                                           
					}).build();
		}
		 private Receive connected(ActorRef Serverconnection) {
		        return receiveBuilder()
		               .match(Received.class, msg->{
		            	   String message = msg.data().utf8String();
		            	   if(!message.contains("statusMessage")){
		            		   log.trace(getSelf().path().name()+ ipport+" received:-> "+message);
		            	   }
		            	   if(message.contains("errorText")){
		            		   App.error ++;
		            		   context().system().stop(getSelf());
		            	   }else if(message.contains("Transaction Successful")){
		            		   App.successful ++;
		            		   context().system().stop(getSelf());		            
		            		}
                                
						})
		               .match(String.class,msg->{
							
							Serverconnection.tell(TcpMessage.write(ByteString.fromString(msg)), getSelf());
						})
		               .match(ConnectionClosed.class, closed->{
		            	   //log.trace("closed-> "+closed.getErrorCause());
                                    getContext().stop(getSelf());
                                    getContext().getSystem().terminate();
		               }).match(CommandFailed.class, conn->{
                                    
                                    getContext().stop(getSelf());
                                    getContext().getSystem().terminate();
				})
		               .build();
		    }
                @Override
                public void postStop() throws Exception {
                	 
                	 if(App.successful+App.error == App.total){
                		 log.info("Successfuls -> "+App.successful +" "+ "Errors-> "+App.error);
                		 context().system().terminate();
                	 }
                }
                private static void pedBalance(ActorRef self) throws InterruptedException {
                	
                	ObjectMapper mapper = new ObjectMapper();
                	
                		String	printFlag = "0";
                		mapper.setSerializationInclusion(Include.NON_NULL);
                		IpsJson message = new IpsJson();

                		message.setOperationType("PedBalance");
                		message.setPrintFlag(printFlag);
                		message.setPedIp(App.terminalIp);
                		message.setPedPort(App.terminalPort);

                		String statusMessageIp = null;
                		String statusMessagePort = null;
                		
                		message.setStatusMessageIp(statusMessageIp);
                		message.setStatusMessagePort(statusMessagePort);
                		message.setTimeOut("60");
                		String json;
                		try {
                			json = mapper.writeValueAsString(message);
                			self.tell(json, ActorRef.noSender());
                		} catch (JsonProcessingException ex) {
                			System.exit(0);
                		}
                	} 
	}