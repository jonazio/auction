package models;

import javax.jms.*;  
import org.apache.activemq.ActiveMQConnectionFactory;

import akka.actor.ActorRef;

//JMS Consumer
public class JMSConsumer {
         public void consume() {
             String url = "tcp://localhost:61616";
             ConnectionFactory factory = new ActiveMQConnectionFactory(url);
             try {
                 Connection connection = factory.createConnection();
                  Session session = connection.createSession(false,
                      Session.AUTO_ACKNOWLEDGE);
                   Topic topic = session.createTopic("bid_in");
                   MessageConsumer consumer = session.createConsumer(topic);
                   JMSMessageListener listener = new JMSMessageListener();
                   consumer.setMessageListener(listener);
                   connection.start();
             }
             catch(JMSException exp) {
             }
         }
     }
