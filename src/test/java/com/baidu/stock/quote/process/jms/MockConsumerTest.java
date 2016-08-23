package com.baidu.stock.quote.process.jms;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import com.baidu.stock.jms.Consumer;
import com.baidu.stock.jms.QuoteMessageListener;
import com.baidu.stock.jms.model.Message;
import com.baidu.stock.jms.model.QuoteBytesMessage;
import com.baidu.stock.jms.model.QuoteTextMessage;
import com.baidu.stock.quote.protocbuf.ExchangeStatusProto;
import com.baidu.stock.quote.protocbuf.SnapShotProto;
import com.google.protobuf.InvalidProtocolBufferException;

public class MockConsumerTest {

	private static String brokerUrl="failover:(tcp://10.46.219.23:8261,tcp://10.46.219.43:8261,tcp://10.46.219.43:8262)?randomize=false&useExponentialBackOff=true&maxReconnectAttempts=3&initialReconnectDelay=100&maxReconnectDelay=3000&jms.prefetchPolicy.all=100&jms.dispatchAsync=false&jms.optimizeAcknowledge=false&jms.optimizeAcknowledgeTimeOut=500&jms.redeliveryPolicy.maximumRedeliveries=6&jms.exclusiveConsumer=true";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Consumer consumer=new Consumer();
		consumer.setUrl(brokerUrl);
		consumer.setPersistent(true);
		consumer.setSubject("test-xxx");
		consumer.setClientid("processor1-"+System.currentTimeMillis() );
		consumer.setClientName("processor1-"+System.currentTimeMillis());
		consumer.setTopic(true);
		consumer.setTransacted(false);
		consumer.addMessageListener(new DefaultMessageListener());
		try {
			consumer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	static class DefaultMessageListener implements QuoteMessageListener{

		@Override
		public void receive(Message message) {
			System.out.println(message);
			if(message instanceof QuoteTextMessage){
				//信号
			System.out.println(((QuoteTextMessage)message).getText());
			}else if(message instanceof QuoteBytesMessage){
				try {
					System.out.println(message.getType());//通过type来区分
					//股票代码表
//					ExchangeStatusProto.ExchangeStatus es=ExchangeStatusProto.ExchangeStatus.parseFrom(((QuoteBytesMessage)message).getMessage());
					//快照
					SnapShotProto.SnapShot ss=SnapShotProto.SnapShot.parseFrom(((QuoteBytesMessage)message).getMessage());
					//股票代码表
//					StockListProto.StockList sl= StockListProto.StockList.parseFrom(((QuoteBytesMessage)message).getMessage());
					System.out.println(ss.getStockBasic().getStockCode());
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}
				}
		}
	}
	
}
