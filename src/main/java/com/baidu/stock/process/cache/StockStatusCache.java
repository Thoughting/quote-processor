package com.baidu.stock.process.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import com.baidu.stock.material.domain.StockStopInfo;
import com.baidu.stock.process.cache.mannage.HQCacheManager;


/**
 * 股票状态缓存
 * @author dengjianli
 *
 */
public class StockStatusCache{
	
	private static Cache getCache(){
		return HQCacheManager.getCacheManager().getCache("stockStatus_cache");   //获取cache
	}
	
	/**
	 * @param key 市场_股票编码
	 * @param data
	 */
	 public static void add(String key, StockStopInfo data) {
	     	Element element=new Element(key,data);
	    	getCache().put(element);
	    }
	        
	    /**
	     * 查询缓存项
	     *
	     * @param key  市场_股票编码
	     * @return 缓存数据对象
	     */
	    public static StockStopInfo query(String key) {
	    	StockStopInfo data=null;
	        // 读取缓存
	    	if(getCache().isElementInMemory(key)){
	    		 Element element = getCache().get(key);
	    		 if(null!=element){
	    		 Object obj=element.getObjectValue();
	    		 if(null!=obj){
	    			 data=(StockStopInfo)obj; 
	    		 }
	    		 }
	    	}
	        return data;
	    }
	    
	    /**
	     * 是否存在内存中
	     * @param key  市场_股票编码
	     * @return
	     */
	public static boolean isStopStock(String key) {
		if (getCache().isElementInMemory(key)) {
			Element element = getCache().get(key);
			if (null != element) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}