package com.baidu.stock.process.config;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.jms.DeliveryMode;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import com.baidu.stock.jms.Consumer;
import com.baidu.stock.jms.Producer;
import com.baidu.stock.process.channel.HQExecutorOutChannel;
import com.baidu.stock.process.channel.HQDirectChannel;
import com.baidu.stock.process.config.setting.ActiveMQConfig;
import com.baidu.stock.process.context.HQProcessContext;
import com.baidu.stock.process.handler.MaterialDataListener;
import com.baidu.stock.util.IpUtil;

/**
 * 注解配置bean，单例模式
 * 
 * @author dengjianli
 * 
 */

@Configuration
@PropertySource({
    "classpath:application.yaml" //如果是相同的key，则最后一个起作用
  })
public class HQConfiguration {
	private Logger logger = LoggerFactory.getLogger(HQConfiguration.class); 
	@Resource
	private ActiveMQConfig activeMQConfig;
	
	/**
	 * 行情服务上下文对象
	 * @return
	 */
	@Bean(name="hqProcessContext")
	public HQProcessContext getHQDataProcessContext(){
		return new HQProcessContext();
	}
	
	/**
	 * 线程池异步输出通道-股票代码处理
	 * @return
	 */
	@Bean(name = "stockCodeExecutorOutChannel")
	public HQExecutorOutChannel getStockCodeExecutorOutChannel() {
		ThreadPoolExecutor executor = new ThreadPoolExecutor(1,1, 300,
				TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(12));
		return new HQExecutorOutChannel(executor);
	}
	
	/**
	 * API回调(同步)输出通道
	 * @return
	 */
	@Bean(name = "hqDirectChannel")
	public HQDirectChannel getHQDirectChannel() {
		return new HQDirectChannel();
	}
	
	/**
	 * 基础数据监听器
	 * @return
	 */
	@Bean(name = "basicInfoListener")
	public MaterialDataListener getMaterialDataListener(){
		return new MaterialDataListener();
	}
	
	
	/**
	 * ActiveMQ连接工程配置
	 * @return
	 */
	@Bean(name="cachingConnectionFactory",destroyMethod="destroy")
	public CachingConnectionFactory getCachingConnectionFactory(){
		ActiveMQConnectionFactory activeMQConnectionFactory=new ActiveMQConnectionFactory(activeMQConfig.getBrokerUrl());
		CachingConnectionFactory cachingConnectionFactory=new CachingConnectionFactory(activeMQConnectionFactory);
		cachingConnectionFactory.setSessionCacheSize(activeMQConfig.getMaxConnection());
		activeMQConnectionFactory.setUserName(activeMQConfig.getUserName());
		activeMQConnectionFactory.setPassword(activeMQConfig.getPassword());
		return cachingConnectionFactory;
	}
	
	/**
	 * 行情快照 生产者配置
	 * @return
	 */
	@Bean(name="snapshotJmsTemplate")
	public JmsTemplate getSnapshotJmsTemplate(){
		JmsTemplate jmsTemplate=new JmsTemplate();
		jmsTemplate.setConnectionFactory(getCachingConnectionFactory());
		ActiveMQTopic destination=new ActiveMQTopic(activeMQConfig.getSnapshotDestination());
		jmsTemplate.setDefaultDestination(destination);
		jmsTemplate.setPubSubDomain(activeMQConfig.isTopic());
		jmsTemplate.setTimeToLive(activeMQConfig.getTimetolive());
		jmsTemplate.setDeliveryMode(activeMQConfig.isPersistent()?DeliveryMode.PERSISTENT:DeliveryMode.NON_PERSISTENT);
		jmsTemplate.setDeliveryPersistent(activeMQConfig.isPersistent());
		return jmsTemplate;
	}
	
	/**
	 * 通知快照 生产者配置
	 * @return
	 */
	@Bean(name="notifymsTemplate")
	public JmsTemplate getNotifymsTemplate(){
		JmsTemplate jmsTemplate=new JmsTemplate();
		jmsTemplate.setConnectionFactory(getCachingConnectionFactory());
		ActiveMQTopic destination=new ActiveMQTopic(activeMQConfig.getNotifyDestination());
		jmsTemplate.setDefaultDestination(destination);
		jmsTemplate.setPubSubDomain(activeMQConfig.isTopic());
		jmsTemplate.setTimeToLive(activeMQConfig.getTimetolive());
		jmsTemplate.setDeliveryMode(activeMQConfig.isPersistent()?DeliveryMode.PERSISTENT:DeliveryMode.NON_PERSISTENT);
		jmsTemplate.setDeliveryPersistent(activeMQConfig.isPersistent());
		return jmsTemplate;
	}
	
	@Bean(name="snapshotProducer")
	public Producer getSnapshotProducer(){
		Producer producer=new Producer();
		producer.setJmsTemplate(getSnapshotJmsTemplate());
		logger.info("启动初始化快照 Producer ActiveMQ[BrokerUrl:"+activeMQConfig.getBrokerUrl()+" Destination:"+activeMQConfig.getSnapshotDestination()+"]完成." );
		return producer;
	}
	
	@Bean(name="notifyProducer")
	public Producer getNotifyProducer(){
		Producer producer=new Producer();
		producer.setJmsTemplate(getNotifymsTemplate());
		logger.info("启动初始化通知&代码表 Producer  ActiveMQ[BrokerUrl:"+activeMQConfig.getBrokerUrl()+" Destination:"+activeMQConfig.getNotifyDestination()+"]完成." );
		return producer;
	}
	
	/**
	 * 基础依赖数据 消费者配置
	 * @return
	 */
	@Bean(name="materialConsumer",initMethod="start")
	public Consumer getMaterialConsumer(){
		Consumer consumer=new Consumer();
		consumer.setUrl(activeMQConfig.getBrokerUrl());
		consumer.setUser(activeMQConfig.getUserName());
		consumer.setPassword(activeMQConfig.getPassword());
		consumer.setPersistent(activeMQConfig.isPersistent());
		consumer.setSubject(activeMQConfig.getMaterialDestination());
		consumer.setClientid("quote-processor_"+IpUtil.getLocalIP());
		consumer.setClientName("quote-processor_"+IpUtil.getLocalIP());
		consumer.setTopic(activeMQConfig.isTopic());
		consumer.setTransacted(activeMQConfig.isTransacted());
		consumer.addMessageListener(getMaterialDataListener());
		return consumer;
	}
	
	
	
	
}