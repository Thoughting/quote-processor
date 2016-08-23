package com.baidu.stock.process.cache.mannage;

import java.net.URL;
import org.apache.log4j.Logger;

import com.baidu.stock.process.util.HQConstant;

import net.sf.ehcache.CacheManager;

/**
 * 统一的全局缓存管理器
 * @author dengjianli
 *
 */
public class HQCacheManager {
	private static Logger logger = Logger.getLogger(HQCacheManager.class);  
	private static CacheManager cacheManager=null;
	private static Object notify=new Object();
	
	public static CacheManager initCacheManagerInstance(){
		if(cacheManager==null){
			synchronized (notify) {
				if(cacheManager==null){
					 URL url = HQCacheManager.class.getClassLoader().getResource(HQConstant.CACHE_FILENAME); 
				     CacheManager.create(url);   
				     cacheManager= CacheManager.getInstance();  
				     logger.info("加载ecache.xml配置文件，初始化本地进程缓存服务.");
				}
			}
		}
		return cacheManager;
	}

	public static CacheManager getCacheManager() {
		return cacheManager;
	}
}
