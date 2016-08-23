package com.baidu.stock.process.cache;


import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import com.baidu.stock.material.domain.IndexComponent;
import com.baidu.stock.process.cache.mannage.HQCacheManager;

/**
 * 指数成份股缓存
 * @author dengjianli
 *
 */
public class IndexBagCache {
	
	private static Cache getCache(){
		return HQCacheManager.getCacheManager().getCache("indexBag_cache");   //获取cache
	}
	
	
	 /**
     * 添加缓存项
     * @param uuid 
     * @param data 缓存数据对象
     */
    public static void add(String key, IndexComponent data) {
    	 Element element=new Element(key,data);
   	     getCache().put(element);
    }


    /**
     * 查询缓存项
     *
     * @param key 格式:exchange + "_" + stockCode
     * @return 缓存数据对象
     */
    public static IndexComponent query(String key) {
    	IndexComponent data=null;
    	if(getCache().isElementInMemory(key)){
            Element element=getCache().get(key);
            if(null!=element){
            	data=(IndexComponent) element.getObjectValue();
            }
    	}
       return data;
    }
    
    
}
