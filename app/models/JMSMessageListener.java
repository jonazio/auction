package models;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

//JMS Message Listener
public class JMSMessageListener implements MessageListener {
      @Override
         public void onMessage(javax.jms.Message msg) {
    	  TextMessage textMessage = (TextMessage) msg;
    	  try {
    		  System.out.println(textMessage.getText());
    	  } catch (JMSException e){
    		  e.printStackTrace();
    	  }
         }
     }