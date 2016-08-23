package com.baidu.stock.process.fetch.core;

import java.util.List;
import com.baidu.stock.process.fetch.ThreadAwareMessageListener;
import com.baidu.stock.process.fetch.ThreadExceptionListener;
import com.baidu.stock.process.fetch.adapter.DataSourcesAdapterHandler;
import com.baidu.stock.process.fetch.core.HQThreadListenerContainer.AsyncMessageListenerInvoker;
import com.baidu.stock.process.fetch.exception.ThreadContainerException;
import com.baidu.stock.process.fetch.meta.HQSnapShot;

/**
 * 基于轮询抓取数据源调度
 * @author dengjianli
 *
 */
public abstract class AbstractPollingThreadListenerContainer extends ThreadContainer{
	/*异常监听器*/
	private ThreadExceptionListener exceptionListener=new ExceptionMessageListener();
    /*处理数据源*/
	private DataSourcesAdapterHandler<HQSnapShot> dataSourcesAdapterHandler; 
	private ThreadAwareMessageListener<HQSnapShot> hqlistener;

	/**
	 * 判断消息监听接口
	 */
	protected void checkMessageListener(Object messageListener) {
		if (!(messageListener instanceof ThreadAwareMessageListener)) {
			throw new IllegalArgumentException(
					"Message listener needs to be of type  [" + ThreadAwareMessageListener.class.getName() + "]");
		}
	}


	/**
	 * 设置异常监听实现
	 */
	public void setExceptionListener(ThreadExceptionListener exceptionListener) {
		this.exceptionListener = exceptionListener;
	}


	public ThreadExceptionListener getExceptionListener() {
		return this.exceptionListener;
	}
	
	/**
	 * 初始化
	 */
	public void initialize() {
		try {
			super.initialize();
		} catch (ThreadContainerException e) {
			if(this.exceptionListener!=null){
				exceptionListener.onException(e);
			}
		}
	
	}


	/**
	 * woker接收数据入口开始
	 */
	protected boolean receiveAndExecute(Object invoker)throws ThreadContainerException {
			return doReceiveAndExecute(invoker);
	}

	/**
	 * 回调异常监听器
	 * @param ex
	 */
	protected void invokeExceptionListener(Throwable ex) {
		ThreadExceptionListener exceptionListener = getExceptionListener();
		if (exceptionListener != null) {
			exceptionListener.onException(ex);
		}
	}

	
	/**
	 * 处理监听异常
	 * @param ex
	 */
	protected void handleListenerException(Throwable ex) {
			invokeExceptionListener(ex);
		if (isActive()) {
			logger.warn("Execution of IngerfaceService message listener failed", ex);
		}
		else {
			logger.debug("Listener exception after container shutdown", ex);
		}
	}
	
	/**
	 * 处理接收数据和变化线程参数
	 */
	protected boolean doReceiveAndExecute(Object invoker)throws ThreadContainerException {
		List<HQSnapShot>lstHq=dataSourcesAdapterHandler.fetchHQ();		
		if (null!=lstHq && lstHq.size()>0) {
			/* 存在数据可以抓取了 */
			try {
				//发送到监听实现类接口中
				doExecuteListener(invoker,lstHq);
				if (!isRunning()) {
					throw new RuntimeException("worker 运行异常.");
				}
			} catch (Throwable ex) {
				if (ex instanceof ThreadContainerException) {
					throw (ThreadContainerException) ex;
				}
			}
			messageReceived(invoker);
			return true;
		}else {
			noMessageReceived(invoker);
			return false;
		}
	}

	
	/**
	 * 回调处理监听器发送消息
	 * @param invoker
	 * @param message
	 * @throws Exception
	 */
	
	protected void doExecuteListener(Object invoker,List<HQSnapShot>lstHq) throws Exception {
		try {
			invokeListener(invoker,lstHq);
		}
		catch (RuntimeException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 回调路由业务接口
	 * @param invoker
	 * @param message
	 * @throws Exception
	 */
	protected void invokeListener(Object invoker,List<HQSnapShot>lstHq) throws Exception {
			ThreadAwareMessageListener<HQSnapShot> listener = getHqlistener();
				if(listener!=null){
					if (listener instanceof ThreadAwareMessageListener) {
						doInvokeListener((AsyncMessageListenerInvoker) invoker,(ThreadAwareMessageListener<HQSnapShot>) listener,lstHq);
					} else {
						throw new IllegalArgumentException(
								"Only MessageListener and AsyncMessageListenerInvoker supported: "
										+ listener);
					}	
				}
	}

	
	/**
	 * 进入处理监听
	 * @param invoker
	 * @param listener
	 * @param message
	 * @throws Exception
	 */
	protected void doInvokeListener(AsyncMessageListenerInvoker invoker,ThreadAwareMessageListener<HQSnapShot> listener,List<HQSnapShot>lstHq) throws Exception {
		listener.onMessage(this,lstHq);
	}
	

	/**
	 * 有消息接收处理
	 * @param invoker
	 */
	protected void messageReceived(Object invoker) {
	}

	/**
	 * 无消息接收处理,如果有消息,则不进入该方法,如果消息很多,则性能越高.
	 * @param invoker
	 */
	protected void noMessageReceived(Object invoker) {
	}
	
	public DataSourcesAdapterHandler<HQSnapShot> getDataSourcesAdapterHandler() {
		return dataSourcesAdapterHandler;
	}


	public void setDataSourcesAdapterHandler(
			DataSourcesAdapterHandler<HQSnapShot> dataSourcesAdapterHandler) {
		this.dataSourcesAdapterHandler = dataSourcesAdapterHandler;
	}


	public ThreadAwareMessageListener<HQSnapShot> getHqlistener() {
		return hqlistener;
	}


	public void setHqlistener(ThreadAwareMessageListener<HQSnapShot> hqlistener) {
		this.hqlistener = hqlistener;
	}

}
