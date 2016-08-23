package com.baidu.stock.process.cache.mannage;

import java.util.Properties;

import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;

/**
 * 缓存监控工厂
 * @author dengjianli
 *
 */
public class ProcessorCacheEventListenerFactory extends CacheEventListenerFactory {
 
   @Override
   public CacheEventListener createCacheEventListener(Properties properties) {
      return new ProcessorCacheEventListener();
   }
 
}