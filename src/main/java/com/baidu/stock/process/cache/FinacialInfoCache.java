package com.baidu.stock.process.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import com.baidu.stock.material.domain.StockFinance;
import com.baidu.stock.process.cache.mannage.HQCacheManager;


/**
 * 财务信息缓存
 * @author dengjianli
 *
 */
public class FinacialInfoCache{
	
	public static Cache getCache(){
		return HQCacheManager.getCacheManager().getCache("finacialInfo_cache");   //获取cache
	}
    
	/**
	 * 
	 * @param key 格式:exchange + "_" + stockCode
	 * @param stockFinance
	 */
    public static void add(String key,StockFinance stockFinance) {
    	 Element element=new Element(key,stockFinance);
    	 getCache().put(element);
    }
    
    /**
     * 
     * @param key 格式:exchange + "_" + stockCode
     * @return
     */
	public static StockFinance query(String key) {
		StockFinance stockFinance =null;
        if(getCache().isElementInMemory(key)){
        Element element=getCache().get(key);
        if(null!=element){
        Object obj=element.getObjectValue();
        if(null!=obj){
        	stockFinance =(StockFinance)obj;
        }
        }
        }
        return stockFinance;
    }
}