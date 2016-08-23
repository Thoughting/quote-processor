package com.baidu.stock.process.cache.mannage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

/**
 * 缓存cache的操作监控
 * @author dengjianli
 *
 */
public class ProcessorCacheEventListener implements CacheEventListener {
	private static Logger logger = LoggerFactory.getLogger(ProcessorCacheEventListener.class);  
   @Override
   public void notifyElementRemoved(Ehcache cache, Element element)
         throws CacheException {
      logger.warn("缓存名称:"+cache.getName()+" key:"+element.getObjectKey()+" 被移除.");
   }
 
   @Override
   public void notifyElementPut(Ehcache cache, Element element)
         throws CacheException {
	   logger.info("缓存名称:"+cache.getName()+" key:"+element.getObjectKey()+" 被添加.");
   }
 
   @Override
   public void notifyElementUpdated(Ehcache cache, Element element)
         throws CacheException {
	   //频率高，这里就不监听了
   }
 
   @Override
   public void notifyElementExpired(Ehcache cache, Element element) {
      logger.info("缓存名称:"+cache.getName()+" key:"+element.getObjectKey()+" 被过期.");
   }
 
   @Override
   public void notifyElementEvicted(Ehcache cache, Element element) {
      logger.info("缓存名称:"+cache.getName()+" key:"+element.getObjectKey()+" 被驱除,可能因为内存不够.");
   }
 
   @Override
   public void notifyRemoveAll(Ehcache cache) {
      logger.info("缓存名称:"+cache.getName()+" 全部被删除.");
   }
 
   @Override
   public void dispose() {
 
   }
  
   public Object clone() throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
   }
 
}