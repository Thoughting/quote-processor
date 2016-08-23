   package com.baidu.stock.quote.process.cache;   
  
import java.net.URL;

import com.baidu.stock.process.cache.LastPointCache;
import com.baidu.stock.process.cache.mannage.HQCacheManager;
import com.baidu.stock.process.fetch.meta.LastPoint;

import net.sf.ehcache.Cache;   
import net.sf.ehcache.CacheManager;   
import net.sf.ehcache.Element;   


public class SimpleCachesTest {   
    public static void main(String[] args) throws InterruptedException {   
    	
//    	 URL url = SimpleCachesTest.class.getClassLoader().getResource("ehcache.xml"); 
//         CacheManager.create(url);   
//         CacheManager manager= CacheManager.getInstance();   
        //1.取得Cache   
//        Cache cache = manager.getCache("lastPoint_cache");   //获取cache
    	HQCacheManager.initCacheManagerInstance();
        int i=1;
        while(true){
        	 i++;
        	 LastPoint cb=new LastPoint();
        	 cb.setAmount(4+i);
        	 LastPointCache.add("ddddd", cb);
//        	 Element element = new Element("key"+0, "value"+i);
//             cache.put(element);
//        	 cache.putIfAbsent(element,false);
             System.out.println(LastPointCache.query("ddddd").getAmount());
             System.out.println("   "+LastPointCache.getCache().getSize());
             Thread.sleep(1000);
        }
        
        
     	/*Element element = new Element("key0", "value1");   
        cache.put(element); 
        int i=0;
         while(true){
        	 Element e= cache.get("key0");
        	 if(e!=null){
        		 System.out.println(e.getObjectValue());
        		 i=0;
        	 }else{
        		 i++;
        		 if(i>10){
        		 element = new Element("key0", "value1");   
        	     cache.put(element);   
        		 }else{
        			 if(e==null){
        			 System.out.println(i+" ");
        			 }
        		 }
        	 }
            Thread.sleep(1000);
         }*/
         
        //2.执行 CRUD 操作   
//         for(int i=0;i<1;i++){
//         Element element = new Element("key"+0, "value1"+i);   
//         cache.put(element);   
//         int elements = cache.getSize();   
//         long elementsInMemory = cache.getMemoryStoreSize();   
//         System.out.println("elements"+elements+ "  elementsInMemory"+elementsInMemory);  
//         Element key0 = cache.get("key0");   
//         if(null!=key0){
//         System.out.println("key0---"+key0.getObjectValue());
//         }else{
//        	 System.out.println("key0过期");
//         }
//         Element key101 = cache.get("key101"); 
//         if(null!=key101){
//         System.out.println("key101---"+key101.getObjectValue());
//         }
//         Thread.sleep(200);
//         }
         
         
//         cache.put(new Element("key1", "value1u"));   
        //2.3 取值   
        //取得一个序列化了的值   
//         Element element231 = cache.get("key1");   
//         Serializable value231 = element231.getValue();   
//         System.out.println(value231);   
//        //取得一个不是序列化的值   
//         Element element232 = cache.get("key1");   
//         Object value232 = element232.getObjectValue();   
//         System.out.println(value232);   
//        //3.4 删除一个元素   
//         cache.remove("key2");   
//         System.out.println(cache.get("key2") == null);   
           
        //4. 取得缓存的大小   
        //得到总的缓存数目   
//        int elements = cache.getSize();   
//         System.out.println(elements);   
//        //得到在内存中的元素数量   
//        long elementsInMemory = cache.getMemoryStoreSize();   
//         System.out.println(elementsInMemory);   
     }   
} 