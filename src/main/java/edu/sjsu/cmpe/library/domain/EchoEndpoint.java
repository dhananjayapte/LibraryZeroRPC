package edu.sjsu.cmpe.library.domain;

import java.io.IOException;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/echo")
public class EchoEndpoint {
   @OnMessage
   public void onMessage(Session session, String msg) {
      try {
    	  System.out.println("Test Array");
         session.getBasicRemote().sendText(msg);
      } catch (IOException e) { 
    	  e.printStackTrace();
      }
   }
}