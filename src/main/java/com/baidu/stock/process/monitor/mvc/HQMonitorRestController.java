package com.baidu.stock.process.monitor.mvc;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.baidu.stock.process.monitor.MonitorService;
import com.baidu.stock.process.monitor.snapshot.CacheSnapshot;
import com.baidu.stock.process.monitor.snapshot.InnerBufferWriterSnapshot;
import com.baidu.stock.process.monitor.snapshot.LoggerCountSnapshot;
import com.baidu.stock.process.monitor.snapshot.MetricsSnapshot;
import com.baidu.stock.process.monitor.snapshot.TPSSnapshot;
import com.baidu.stock.process.monitor.snapshot.WorkerSnapshot;

/**
 * 
 * @author dengjianli
 *
 */

@RestController
public class HQMonitorRestController {
	private static Logger logger = LoggerFactory.getLogger(HQMonitorRestController.class);
	@Resource
	private MonitorService monitorService;
	private static ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 300,TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(12),new ThreadPoolExecutor.DiscardPolicy());
	
	@RequestMapping(value="/processor", method=RequestMethod.GET)
	public Map<String, Object> json() {
		Future<Map<String, Object>>future=executor.submit(new StatisticsMetrics());
		Map<String,Object>result=null;
			try {
				if(!future.isCancelled()){
				result=future.get(12, TimeUnit.SECONDS);
				}
			} catch (InterruptedException e) {
				logger.error("线程中断异常.",e);
			} catch (ExecutionException e) {
				logger.error("访问执行线程异常.",e);
			} catch (TimeoutException e) {
				logger.error("访问超时异常.",e);
			}finally{
				future.cancel(true);
			}
		return result;
	}
	
	class StatisticsMetrics implements Callable<Map<String,Object>>{
		@Override
		public Map<String, Object> call() throws Exception {
			SortedMap<String, Object> result=new TreeMap<String,Object>();
			/*性能指标[耗时:单位毫秒,比率:调用次数/秒]*/
			MetricsSnapshot fetchFileMetricsSnapshot=monitorService.fetchfileMetrics();
			/*行情计算耗时指标[耗时:单位毫秒]*/
			long timeConsuming=monitorService.processHQTimeConsuming();
			/*服务内部线程池监控*/	
			List<InnerBufferWriterSnapshot>lstWriter=monitorService.innQueueuMetrics();
			/*行情数据计算输出TPS监控*/
			TPSSnapshot tpsSnapshot=monitorService.writeTpsMetrics();
			/*worker状态监控*/
			List<WorkerSnapshot> lstWorker=monitorService.workerMetrics();
			/*日志统计*/
			LoggerCountSnapshot loggerCountSnapshot=monitorService.logMetrics();
			/*缓存状态监控*/
			List<CacheSnapshot>  lstCacheSnapshot=monitorService.cacheStatisticsMetrics();
			Map<String,String> hqTime=monitorService.getHQProcessTime();
			result.put("A Time(Millis)",timeConsuming);
			result.put("B TPS(MQ)",tpsSnapshot);
			result.put("C FileUpdateTime(HHmmSS)",hqTime);
			result.put("D FetchFile",fetchFileMetricsSnapshot);
			result.put("E Logger(warn,error)",loggerCountSnapshot);
			result.put("F Writer",lstWriter);
			result.put("G Worker",lstWorker);
			result.put("H MemoryCache",lstCacheSnapshot);
			return result;
		}
	}
	
	/**
	 * 获取缓存数据,建议删掉
	 * @param key
	 * @return
	 */
	@RequestMapping(value="/getFinacialInfoCache", method=RequestMethod.GET)
	public String getFinacialInfoCache(String key) {
		return monitorService.getFinacialInfoCache(key);
	}
	@RequestMapping(value="/getAssistDataCache", method=RequestMethod.GET)
	public String getAssistDataCache(String key,String field) {
		return monitorService.getAssistDataCache(key,field);
	}
	@RequestMapping(value="/getHQSnapShotCache", method=RequestMethod.GET)
	public String getHQSnapShotCache(String key) {
		return monitorService.getHQSnapShotCache(key);
	}
	@RequestMapping(value="/getRiseFallCache", method=RequestMethod.GET)
	public String getRiseFallCache(String key,String exchange) {
		return monitorService.getRiseFallCache(key,exchange);
	}
	@RequestMapping(value="/getStockStatusCache", method=RequestMethod.GET)
	public String getStockStatusCache(String key) {
		return monitorService.getStockStatusCache(key);
	}
	@RequestMapping(value="/getLastPointCache", method=RequestMethod.GET)
	public String getLastPointCache(String key) {
		return monitorService.getLastPointCache(key);
	}
	@RequestMapping(value="/getHolidayset", method=RequestMethod.GET)
	public String getHolidayset() {
		return monitorService.getHolidayset();
	}
}