package models;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import models.AuctionRoom.Bid;

import akka.actor.ActorRef;

//JMS Message Listener
public class JMSMessageListener implements MessageListener {
	
	ActorRef defaultItem = AuctionRoom.defaultItem;
	
      @Override
         public void onMessage(javax.jms.Message msg) {
    	  TextMessage textMessage = (TextMessage) msg;
    	  try {
    		  System.out.println(textMessage.getText());
    		  
    		  defaultItem.tell(new Bid("Tomas", "101", 1L));
    	  } catch (JMSException e){
    		  e.printStackTrace();
    	  }
         }
     }