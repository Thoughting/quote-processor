package com.baidu.stock.process.cache;

import java.util.HashMap;
import java.util.Map;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import com.baidu.stock.process.cache.mannage.HQCacheManager;


/**
 * 买一卖一缓存
 * @author dengjianli
 *
 */
public class AskBidDataCache{
	
	private static Cache getCache(){
		return HQCacheManager.getCacheManager().getCache("askbid_cache");   //获取cache
	}
	
    /**
     * 添加缓存项
     *
     * @param key 格式:exchange + "_" + stockCode
     * @param field 数据项
     * @param value 度量值
     */
    @SuppressWarnings("unchecked")
	public static void add(String key, String field, double value) {
    	      Map<String, Double> map=null;
    	      if(getCache().isElementInMemory(key)){
    	    	  Element element=getCache().get(key);
    	          if(null!=element){
    	          map =((Map<String, Double>) element.getObjectValue());
    	          map.put(field, value);
    	          }else{
    	        	  map=new HashMap<String,Double>();
        	    	  map.put(field, value);
                	  element=new Element(key,map);
                	  getCache().put(element);
    	          }
    	      }else{
    	    	  map=new HashMap<String,Double>();
    	    	  map.put(field, value);
            	  Element element=new Element(key,map);
            	  getCache().put(element);
    	      }
        }
    
    /**
     * 查询缓存项
     *
     * @param key 格式:exchange + "_" + stockCode
     * @param field 数据项
     * @return 缓存数据值
     */
    @SuppressWarnings("unchecked")
	public static Double query(String key, String field) {
    	Double value = 0.00;
        if(getCache().isElementInMemory(key)){
        Element element=getCache().get(key);
        if(null!=element){
        Map<String, Double> map =((Map<String, Double>) element.getObjectValue());
        if (null!=map && map.size() > 0) {
        	 value = map.get(field);	
        }
        }
        }
        if(null==value){
        	value=0.00;
        }
        return value;
    }
    
}