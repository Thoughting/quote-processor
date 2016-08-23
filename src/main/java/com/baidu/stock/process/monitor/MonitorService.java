package com.baidu.stock.process.monitor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import net.sf.ehcache.Cache;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.baidu.stock.material.domain.StockFinance;
import com.baidu.stock.material.domain.StockStopInfo;
import com.baidu.stock.process.asyn.HQAsynWriterTemplate;
import com.baidu.stock.process.cache.AssistDataCache;
import com.baidu.stock.process.cache.FinacialInfoCache;
import com.baidu.stock.process.cache.HQSnapShotCache;
import com.baidu.stock.process.cache.LastPointCache;
import com.baidu.stock.process.cache.RiseFallCache;
import com.baidu.stock.process.cache.StockStatusCache;
import com.baidu.stock.process.cache.mannage.HQCacheManager;
import com.baidu.stock.process.config.HQGloableConfig;
import com.baidu.stock.process.fetch.core.HQThreadListenerContainer;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.process.fetch.meta.LastPoint;
import com.baidu.stock.process.monitor.metrics.MetricsManager;
import com.baidu.stock.process.monitor.snapshot.CacheSnapshot;
import com.baidu.stock.process.monitor.snapshot.LoggerCountSnapshot;
import com.baidu.stock.process.monitor.snapshot.MetricsSnapshot;
import com.baidu.stock.process.monitor.snapshot.TPSSnapshot;
import com.baidu.stock.process.monitor.snapshot.WorkerSnapshot;
import com.baidu.stock.process.monitor.snapshot.InnerBufferWriterSnapshot;
import com.baidu.stock.process.util.CommonHelp;
import com.baidu.stock.process.util.MathUtils;
import com.baidu.stock.quote.asyn.IAsynWriter;
import com.codahale.metrics.Snapshot;

/**
 * 监控服务
 * @author dengjianli
 *
 */
@Component(value="monitorService")
public class MonitorService {
	/**
	 * 行情源数据抓取性能指标
	 * @return
	 */
	public MetricsSnapshot fetchfileMetrics(){
		/*服务的性能指标*/
		Snapshot snapshot=MetricsManager.fetchTimer.getSnapshot();
		MetricsSnapshot ms=new MetricsSnapshot();
		//耗时,单位是毫秒
		double maxMillis=snapshot.getMax();
		double minMillis=snapshot.getMin();
		double meanMillis=snapshot.getMean();
		ms.setMaxMillis(maxMillis*(1.0 / TimeUnit.MILLISECONDS.toNanos(1)));
		ms.setMinMillis(minMillis*(1.0 / TimeUnit.MILLISECONDS.toNanos(1)));
		ms.setMeanMillis(meanMillis*(1.0 / TimeUnit.MILLISECONDS.toNanos(1)));
		//耗时的分布情况
		double p75=snapshot.get75thPercentile();
		double p95=snapshot.get95thPercentile();
		double p98=snapshot.get98thPercentile();
		double p99=snapshot.get99thPercentile();
		double p999=snapshot.get999thPercentile();
		ms.setP75(p75*(1.0 / TimeUnit.MILLISECONDS.toNanos(1)));
		ms.setP95(p95*(1.0 / TimeUnit.MILLISECONDS.toNanos(1)));
		ms.setP98(p98*(1.0 / TimeUnit.MILLISECONDS.toNanos(1)));
		ms.setP99(p99*(1.0 / TimeUnit.MILLISECONDS.toNanos(1)));
		ms.setP999(p999*(1.0 / TimeUnit.MILLISECONDS.toNanos(1)));
		//每秒回调次数统计
		double m1Rate=MetricsManager.fetchTimer.getOneMinuteRate();
		double m5Rate=MetricsManager.fetchTimer.getFiveMinuteRate();
		double m15Rate=MetricsManager.fetchTimer.getFifteenMinuteRate();
		double meanRate=MetricsManager.fetchTimer.getMeanRate();
		ms.setM1Rate(m1Rate);
		ms.setM5Rate(m5Rate);
		ms.setM15Rate(m15Rate);
		ms.setMeanRate(meanRate);
		return ms;
	}
	
	/**
	 * 处理数据性能指标-耗时(毫秒)
	 * @return
	 */
	public long processHQTimeConsuming(){
		return MetricsManager.gauge.getValue().longValue();
	}
	
	/**
	 * 日志监控统计
	 */
	public LoggerCountSnapshot logMetrics(){
		LoggerCountSnapshot loggerCountSnapshot=new LoggerCountSnapshot();
		long warn=MetricsManager.registry.meter("ch.qos.logback.core.Appender.warn").getCount();
		long error=MetricsManager.registry.meter("ch.qos.logback.core.Appender.error").getCount();
		loggerCountSnapshot.setError(error);
		loggerCountSnapshot.setWarn(warn);
		return loggerCountSnapshot;
	}
	
	/**
	 * 服务内部线程池监控
	 * @return
	 */
	public List<InnerBufferWriterSnapshot> innQueueuMetrics(){
		ConcurrentHashMap<String, HQAsynWriterTemplate>writerMap=HQGloableConfig.asynWriterCacheInMemory;
		List<InnerBufferWriterSnapshot>lstWriterSnapshot=new ArrayList<InnerBufferWriterSnapshot>();
		for(String key :writerMap.keySet()){
			String exchange=key;
			HQAsynWriterTemplate writer= writerMap.get(key);
			IAsynWriter[] writers=writer.getAsynWriters();
			InnerBufferWriterSnapshot ws=new InnerBufferWriterSnapshot();
			ws.setWriterNum(writers.length);
			ws.setExchange(exchange);
			Map<String,Integer>map=new HashMap<String,Integer>();
			for(IAsynWriter iw:writers){
				 LinkedBlockingQueue<Object> queue=iw.getDataQueue();
				 int queueSize=queue.size();
				 map.put(iw.getName(), queueSize);
			}
			 ws.setQueueMapper(map);
			 lstWriterSnapshot.add(ws);
		}
		return lstWriterSnapshot;
	}
	class NumComparator implements Comparator<String> {
	    public int compare (String left,String right) {
	    	int l=Integer.parseInt(left.split("-")[1]);
	    	int r=Integer.parseInt(right.split("-")[1]);
	        return(l-r);
	    }
	} 
	
	/**
	 * 写入MQ的tps监控
	 */
	public TPSSnapshot writeTpsMetrics(){
		//tps统计
		long requestCount=MetricsManager.mqRequest.getCount();
		double meanRate=MetricsManager.mqRequest.getOneMinuteRate();
		TPSSnapshot tpsSnapshot=new TPSSnapshot();
		tpsSnapshot.setRequestCount(requestCount);
		tpsSnapshot.setMeanRate(meanRate);
		return tpsSnapshot;
	}
	
	/**
	 * worker指标监控
	 */
	public List<WorkerSnapshot> workerMetrics(){
		/*文件抓取worker的状态监控*/
		ConcurrentHashMap<String, HQThreadListenerContainer>workerMap=HQGloableConfig.workerCacheInMemory;
		List<WorkerSnapshot>lstWorker=new ArrayList<WorkerSnapshot>();
		for(String key :workerMap.keySet()){
			WorkerSnapshot workerSnapshot=new WorkerSnapshot();	
			HQThreadListenerContainer worker=workerMap.get(key);
			workerSnapshot.setMaxConcumer(worker.getMaxConcurrentConsumers());
			workerSnapshot.setConcumer(worker.getConcurrentConsumers());
			workerSnapshot.setWorkerName(worker.getWorkName());
			workerSnapshot.setRunStats(worker.isRunning()?"运行":"停止");
			workerSnapshot.setStockCodeSendStatus(worker.isExchangeOpenFlag?"初始化&发送代码表完成":"待初始化发送代码表");
			lstWorker.add(workerSnapshot);
		}
		return lstWorker;
	}
	
	/**
	 * 缓存缓存率指标监控
	 * @return
	 */
	public List<CacheSnapshot> cacheStatisticsMetrics(){
		String[]cacheName=HQCacheManager.getCacheManager().getCacheNames();
		List<CacheSnapshot>lstCacheSnapshot=new ArrayList<CacheSnapshot>();
		for(String name:cacheName){
			CacheSnapshot snapshot=new CacheSnapshot();
			Cache cache=HQCacheManager.getCacheManager().getCache(name);
			int cacheSize=cache.getSize();
			Double hitRatio=cache.getStatistics().cacheHitRatio();
			float hitRatioPercent=0;
			if (!hitRatio.isNaN()) {
				hitRatioPercent = MathUtils.leaveDigits(hitRatio, 2);
			}
			snapshot.setName(name);
			snapshot.setCacheSize(cacheSize);
			snapshot.setHitRatio(hitRatioPercent*100);
			lstCacheSnapshot.add(snapshot);
		}
		return lstCacheSnapshot;
	}
	
	public Map<String,String>getHQProcessTime(){
		Map<String,String>timeMap=new HashMap<String,String>();
		timeMap.put("sh", StringUtils.isNotBlank(HQGloableConfig.lastSHHQtime)?HQGloableConfig.lastSHHQtime:"---");
		timeMap.put("sz", StringUtils.isNotBlank(HQGloableConfig.lastSZHQTime)?HQGloableConfig.lastSZHQTime:"---");
		return timeMap;
	}
	
	
	public String getHolidayset(){
		String result="";
		if(CommonHelp.holidayset.size()>0){
			result=JSON.toJSONString(CommonHelp.holidayset);
		}
		return result;
	}
	
	//获得缓存的值
	public String getFinacialInfoCache(String key){
		String result="";
		StockFinance finace=FinacialInfoCache.query(key);
		if(null!=finace){
			result=JSON.toJSONString(finace);
		}
		return result;
	}
	public String getAssistDataCache(String key, String field){
		String result="";
		double value=AssistDataCache.query(key,field);
			result=JSON.toJSONString(value);
		return result;
	}
	public String getHQSnapShotCache(String key){
		String result="";
		HQSnapShot snapShot=HQSnapShotCache.querySnapShot(key);
		if(null!=snapShot){
			result=JSON.toJSONString(snapShot);
		}
		return result;
	}
	public String getRiseFallCache(String key,String exchange){
		String result="";
		Integer value=RiseFallCache.query(key,exchange);
			result=JSON.toJSONString(value);
		return result;
	}
	public String getStockStatusCache(String key){
		String result="";
		StockStopInfo stockStopInfo=StockStatusCache.query(key);
		if(null!=stockStopInfo){
			result=JSON.toJSONString(stockStopInfo);
		}
		return result;
	}
	public String getLastPointCache(String key){
		String result="";
		LastPoint candleBar=LastPointCache.query(key);
		if(null!=candleBar){
			result=JSON.toJSONString(candleBar);
		}
		return result;
	}
}
