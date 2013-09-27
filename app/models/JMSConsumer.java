package models;

import javax.jms.*;  
import org.apache.activemq.ActiveMQConnectionFactory;

//JMS Consumer
public class JMSConsumer {
         public void consume() {
             String url = "tcp://localhost:61616";
             ConnectionFactory factory = new ActiveMQConnectionFactory(url);
             try {
                 Connection connection = factory.createConnection();
                  Session session = connection.createSession(false,
                      Session.AUTO_ACKNOWLEDGE);
                   Topic topic = session.createTopic("stock");
                   MessageConsumer consumer = session.createConsumer(topic);
                   JMSMessageListener listener = new JMSMessageListener();
                   consumer.setMessageListener(listener);
                   connection.start();
             }
             catch(JMSException exp) {
             }
         }
     }
