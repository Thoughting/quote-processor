package com.baidu.stock.quote.process.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import com.baidu.stock.jms.Producer;
import com.baidu.stock.process.util.HQConstant;
import com.baidu.stock.quote.protocbuf.SnapShotProto;
import com.baidu.stock.quote.protocbuf.StockBasicProto;

public class MockProducer {
	
	private JmsTemplate jmsTemplate;
	private Producer producer;
	private  String brokerUrl="failover:(tcp://10.46.219.23:8261,tcp://10.46.219.43:8261,tcp://10.46.219.43:8262)?randomize=false&useExponentialBackOff=true&maxReconnectAttempts=3&initialReconnectDelay=100&maxReconnectDelay=3000&jms.prefetchPolicy.all=100&jms.dispatchAsync=false&jms.optimizeAcknowledge=false&jms.optimizeAcknowledgeTimeOut=500&jms.redeliveryPolicy.maximumRedeliveries=6&jms.exclusiveConsumer=true";

	@Before
	public void init(){
		ActiveMQConnectionFactory activeMQConnectionFactory=new ActiveMQConnectionFactory(brokerUrl);
		CachingConnectionFactory cachingConnectionFactory=new CachingConnectionFactory(activeMQConnectionFactory);
		cachingConnectionFactory.setSessionCacheSize(30);
		JmsTemplate jmsTemplate=new JmsTemplate();
		jmsTemplate.setConnectionFactory(activeMQConnectionFactory);
		ActiveMQTopic destination=new ActiveMQTopic("test-xxx");
		jmsTemplate.setDefaultDestination(destination);
		jmsTemplate.setPubSubDomain(true);
		jmsTemplate.setTimeToLive(180000);
		producer=new Producer();
		producer.setJmsTemplate(jmsTemplate);
	}
	
	@Test
	public void testSend(){
		String msg="haha";
		producer.send(msg);
		
		SnapShotProto.SnapShot.Builder builder=SnapShotProto.SnapShot.newBuilder();
		builder.setDate("dd");
		builder.setTime("ddddd");
		StockBasicProto.StockBasic.Builder stockBasic=StockBasicProto.StockBasic.newBuilder();
		stockBasic.setExchange(HQConstant.szExchangeId);
		stockBasic.setStockCode("33");
		builder.setStockBasic(stockBasic);
		producer.send(com.baidu.stock.jms.model.Message.SNAPSHOT_TYPE,builder.build().toByteArray());
	}
	
	
}
