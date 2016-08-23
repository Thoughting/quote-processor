package com.baidu.stock.process.fetch.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.stock.process.fetch.ThreadExceptionListener;


/**
 * 
 * @author dengjianli
 *
 */
public class ExceptionMessageListener implements ThreadExceptionListener{
	protected final Logger logger = LoggerFactory.getLogger(ExceptionMessageListener.class);
	public void onException(Throwable e) {
			logger.error(e.getCause().getMessage());
	}

}
