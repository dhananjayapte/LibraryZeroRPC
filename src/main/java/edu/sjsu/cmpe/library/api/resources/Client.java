package edu.sjsu.cmpe.library.api.resources;

import org.zeromq.ZMQ;

public class Client {

	ZMQ.Context context = null;
	ZMQ.Socket socket = null;

	public void connect(String address) {
		this.context = ZMQ.context(1);
		this.socket = context.socket(ZMQ.REQ);
		// Socket to talk to server
		this.socket.connect(address);
	}

	public void saveBook(){
		
	}
	
}
