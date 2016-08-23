package com.baidu.stock.process.channel;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.integration.channel.AbstractSubscribableChannel;
import org.springframework.integration.channel.MessagePublishingErrorHandler;
import org.springframework.integration.context.IntegrationProperties;
import org.springframework.integration.dispatcher.LoadBalancingStrategy;
import org.springframework.integration.dispatcher.UnicastingDispatcher;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.integration.util.ErrorHandlingTaskExecutor;
import org.springframework.util.ErrorHandler;

/**
 * 作为扩展-异步通道
 * @author dengjianli
 *
 */
public class HQExecutorOutChannel extends AbstractSubscribableChannel {

	private volatile UnicastingDispatcher dispatcher;

	private volatile Executor executor;

	private volatile boolean failover = true;

	private volatile Integer maxSubscribers;

	private volatile LoadBalancingStrategy loadBalancingStrategy;
	
	public HQExecutorOutChannel(Executor executor) {
		//配置为最小最大3，建议不暴漏出去
		this.executor=executor;
		this.dispatcher = new UnicastingDispatcher(executor);
		if (loadBalancingStrategy != null) {
			this.loadBalancingStrategy = loadBalancingStrategy;
			this.dispatcher.setLoadBalancingStrategy(loadBalancingStrategy);
		}
	}

	/**
	 * Specify whether the channel's dispatcher should have failover enabled.
	 * By default, it will. Set this value to 'false' to disable it.
	 *
	 * @param failover The failover boolean.
	 */
	public void setFailover(boolean failover) {
		this.failover = failover;
		this.dispatcher.setFailover(failover);
	}

	/**
	 * Specify the maximum number of subscribers supported by the
	 * channel's dispatcher.
	 *
	 * @param maxSubscribers The maximum number of subscribers allowed.
	 */
	public void setMaxSubscribers(int maxSubscribers) {
		this.maxSubscribers = maxSubscribers;
		this.dispatcher.setMaxSubscribers(maxSubscribers);
	}

	@Override
	protected UnicastingDispatcher getDispatcher() {
		return this.dispatcher;
	}

	@Override
	public final void onInit() {
		if (!(this.executor instanceof ErrorHandlingTaskExecutor)) {
			ErrorHandler errorHandler = new MessagePublishingErrorHandler(new BeanFactoryChannelResolver(this.getBeanFactory()));
			this.executor = new ErrorHandlingTaskExecutor(this.executor, errorHandler);
		}
		this.dispatcher = new UnicastingDispatcher(this.executor);
		this.dispatcher.setFailover(this.failover);
		if (this.maxSubscribers == null) {
			this.maxSubscribers = this.getIntegrationProperty(IntegrationProperties.CHANNELS_MAX_UNICAST_SUBSCRIBERS, Integer.class);
		}
		this.dispatcher.setMaxSubscribers(this.maxSubscribers);
		if (this.loadBalancingStrategy != null) {
			this.dispatcher.setLoadBalancingStrategy(this.loadBalancingStrategy);
		}
	}

	public ThreadPoolExecutor getExecutor() {
		return (ThreadPoolExecutor) executor;
	}

	
	
}
