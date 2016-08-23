package com.baidu.stock.process.config;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.env.PropertyResolver;

import com.baidu.stock.process.asyn.HQAsynWriterTemplate;
import com.baidu.stock.process.fetch.core.HQThreadListenerContainer;

/**
 * 全局参数
 * @author dengjianli
 *
 */
public class HQGloableConfig {
	public static ConcurrentHashMap<String, HQThreadListenerContainer>workerCacheInMemory=new ConcurrentHashMap<String, HQThreadListenerContainer>();
	public static ConcurrentHashMap<String, HQAsynWriterTemplate>asynWriterCacheInMemory=new ConcurrentHashMap<String, HQAsynWriterTemplate>();

	public static String lastSZHQTime="0";
	public static String lastSHHQtime="0";
	/**
	 * 行情运行参数
	 */
	public static  ApplicationSettings applicationSettings;
	/**
	 * application参数
	 */
	public static PropertyResolver propertyResolver;
	
}
