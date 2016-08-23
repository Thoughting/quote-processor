package com.baidu.stock.process.cache;

import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import com.baidu.stock.process.cache.mannage.HQCacheManager;

/**
 * 内外盘数据
 * @author dengjianli
 *
 */
public class InOutDataCache {

	public  static Cache getCache(){
		return HQCacheManager.getCacheManager().getCache("innerout_cache");   //获取cache
	}
	
    /**
     * 添加缓存项
     *
     * @param key 格式:exchange + "_" + stockCode
     * @param field 数据项
     * @param value 度量值
     */
    @SuppressWarnings("unchecked")
	public static void add(String key, String field, long value) {
    	 Map<String, Long> map=null;
	      if(getCache().isElementInMemory(key)){
	    	  Element element=getCache().get(key);
	          if(null!=element){
	          map =((Map<String, Long>) element.getObjectValue());
	          Long oldVal = map.get(field);
	          if(null!=oldVal){
	            map.put(field, (oldVal + value));
	          }else{
	   	    	  map.put(field, value);
	          }
	          }
	      }else{
	      map=new HashMap<String,Long>();
	      map.put(field, value);
       	  Element element=new Element(key,map);
       	  getCache().put(element);
	      }
    }
    
    /**
     * 查询缓存项
     *
     * @param key 格式:exchange + "_" + stockCode
     * @return 缓存数据对象
     */
    @SuppressWarnings("unchecked")
	public static Map<String, Long> query(String key) {
    	 Map<String, Long> map=null;
	      if(getCache().isElementInMemory(key)){
	    	  Element element=getCache().get(key);
	          if(null!=element){
	          map= ((Map<String, Long>) element.getObjectValue());
	          }
	      }
        return map;
    }
    
    
}
