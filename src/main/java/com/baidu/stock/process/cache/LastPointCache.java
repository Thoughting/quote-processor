package com.baidu.stock.process.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import com.baidu.stock.process.cache.mannage.HQCacheManager;
import com.baidu.stock.process.fetch.meta.LastPoint;


/**
 * 最后一笔行情缓存
 * <p/>
 * @author dengjianli
 *
 */
public class LastPointCache {
	
	public static Cache getCache(){
		return HQCacheManager.getCacheManager().getCache("lastPoint_cache");   //获取cache
	}
	
	/**
	 * 
	 * @param key 格式:exchange + "_" + stockCode
	 * @param data
	 */
    public static void add(String key, LastPoint data) {
     	Element element=new Element(key,data);
    	getCache().put(element);
    }
        
    /**
     * 查询缓存项
     *
     * @param key 格式:exchange + "_" + stockCode
     * @return 缓存数据对象
     */
    public static LastPoint query(String key) {
    	LastPoint data=null;
        // 读取缓存
    	if(getCache().isElementInMemory(key)){
    		 Element element = getCache().get(key);
    		 if(null!=element){
    		 Object obj=element.getObjectValue();
    		 if(null!=obj){
    		 data=(LastPoint)obj;
    		 }
    		 }
    	}
        return data;
    }
    
}

