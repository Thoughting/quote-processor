package com.baidu.stock.quote.process.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import com.baidu.stock.jms.Producer;

public class JmsTemplateTest {

	
	private JmsTemplate jmsTemplate;
	private  String brokerUrl="failover:(tcp://10.46.219.23:8261,tcp://10.46.219.43:8261,tcp://10.46.219.43:8262)"; 

	@Before
	public void init(){
		ActiveMQConnectionFactory activeMQConnectionFactory=new ActiveMQConnectionFactory(brokerUrl);
		CachingConnectionFactory cachingConnectionFactory=new CachingConnectionFactory(activeMQConnectionFactory);
		cachingConnectionFactory.setSessionCacheSize(30);
		jmsTemplate=new JmsTemplate();
		jmsTemplate.setConnectionFactory(activeMQConnectionFactory);
		ActiveMQQueue destination=new ActiveMQQueue("VirtualTopic.quotes");
		jmsTemplate.setDefaultDestination(destination);
	}
	
	@Test
	public void testReceive(){
		System.out.println(jmsTemplate.receive());
	}

}
