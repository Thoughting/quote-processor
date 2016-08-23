package com.baidu.stock.process.fetch;

import java.util.List;

import com.baidu.stock.process.fetch.core.AbstractPollingThreadListenerContainer;
public interface ThreadAwareMessageListener<T extends Object>{

	/**
	 * 接收所有数据
	 * @param message
	 * @throws Exception
	 */
	abstract void onMessage(AbstractPollingThreadListenerContainer container,List<T> message)throws Exception;

}
