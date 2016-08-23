package com.baidu.stock.process.cache;

import com.baidu.stock.process.cache.mannage.HQCacheManager;
import com.baidu.stock.process.util.HQConstant;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * 涨跌状态记录
 * 1 表示上涨, 0 表示平盘, -1 表示下跌
 * @author dengjianli
 *
 */
public class RiseFallCache {
	
	public static Cache getSHCache(){
		return HQCacheManager.getCacheManager().getCache("riseFall_sh_cache");   //获取cache
	}
	public static Cache getSZCache(){
		return HQCacheManager.getCacheManager().getCache("riseFall_sz_cache");
	}
	
	  /**
     * 修改缓存项
     *
     * @param uuid 证券标识
     * @param data 缓存数据对象
     */
    public static void addOrUpdate(String uuid, Integer data,String exchange) {
    	Element element=new Element(uuid,data);
    	if(HQConstant.shExchangeId.equals(exchange)){
    		getSHCache().put(element);
    	}else if(HQConstant.szExchangeId.equals(exchange)){
    		getSZCache().put(element);
    	}
    }
    
    /**
     * 查询缓存项
     *
     * @param uuid 证券唯一标识
     * @return 缓存数据对象
     */
    public static Integer query(String uuid,String exchange) {
        Integer result = null;
        Element element=null;
        if(HQConstant.shExchangeId.equals(exchange)){
        	element=getSHCache().get(uuid);
        }else if(HQConstant.szExchangeId.equals(exchange)){
        	element=getSZCache().get(uuid);
        }
        if (null!=element) {
            result =(Integer) element.getObjectValue();
        }
        return result;
    }
    
}
