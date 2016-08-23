package com.baidu.stock.process.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import com.baidu.stock.process.cache.mannage.HQCacheManager;
import com.baidu.stock.process.fetch.meta.HQSnapShot;

/**
 * 行情快照缓存
 * @author dengjianli
 *
 */
public class HQSnapShotCache {
	public static Cache getCache(){
		return HQCacheManager.getCacheManager().getCache("hqSnapShot_cache");   //获取cache
	}
	
	/**
	 * 添加和更新
	 * @param key 格式:exchange + "_" + stockCode
	 * @param snapshot
	 */
    public static void add(String key, HQSnapShot snapshot) {
     	Element element=new Element(key,snapshot);
    	getCache().put(element);
    }
        
    /**
     * 查询缓存项
     * @param key 格式:exchange + "_" + stockCode
     * @return 缓存数据对象
     */
    public static HQSnapShot querySnapShot(String key) {
    	HQSnapShot snapshot=null;
        // 读取缓存
    	if(getCache().isElementInMemory(key)){
    		 Element element = getCache().get(key);
    		 if(null!=element){
    		 Object obj=element.getObjectValue();
    		 if(null!=obj){
    			 snapshot=(HQSnapShot)obj;
    		 }
    		 }
    	}
        return snapshot;
    }
}
