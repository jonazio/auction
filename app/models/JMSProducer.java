package models;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;

public class JMSProducer {
	
	MessageProducer producer;
	Session session;
	
    public void produce() {
       String url = "tcp://localhost:61616";
       ConnectionFactory factory = new ActiveMQConnectionFactory(url);
         try {
             Connection connection = factory.createConnection();
             session = connection.createSession(false,
             Session.AUTO_ACKNOWLEDGE);
             Topic topic = session.createTopic("bid_out");
             producer = session.createProducer(topic);          
       }
        catch(JMSException exp) {
         }
    }
    
    public void sendMsg(String msg_out) throws JMSException{
    	TextMessage msg = session.createTextMessage();
        msg.setText(msg_out);
        producer.send(msg);
    }
}
