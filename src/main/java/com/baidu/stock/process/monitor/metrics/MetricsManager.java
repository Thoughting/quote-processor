package com.baidu.stock.process.monitor.metrics;

import java.util.SortedMap;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.baidu.stock.process.monitor.ext.ExtMeter;
import com.baidu.stock.process.monitor.ext.ExtTimer;
import com.baidu.stock.process.monitor.ext.InstrumentedAppender;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

/**
 * 
 * @author dengjianli
 *
 */
public class MetricsManager {
	//性能监控
	public final static MetricRegistry registry = new MetricRegistry();
	//抓取文件耗时分布统计
	public final static ExtTimer fetchTimer = new ExtTimer();//行情数据抓取时间分布统计
	public final static ExtMeter mqRequest =new ExtMeter();//数据处理TPS统计");	//tps统计
	//日志统计
	private final static LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
	private final static Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);
	private final static InstrumentedAppender loggerAppender = new InstrumentedAppender(registry);
	public volatile static long timeConsuming=0L;
	public static Gauge<Long> gauge =null;
	static{
	 gauge = new Gauge<Long>() {
        @Override
        public Long getValue() {
            return timeConsuming;
        }
    };
    registry.register("耗时：", gauge);
	}
    
    
	/**
	 * 启动监控日志调用统计
	 */
	public static void startLoggerMonitor(){
		MetricsManager.loggerAppender.setContext(root.getLoggerContext());
    	root.addAppender(MetricsManager.loggerAppender);
    	MetricsManager.loggerAppender.start();
	}
	
	public static InstrumentedAppender getLoggerappender() {
		return loggerAppender;
	}

	/**
	 * 重置couter计数
	 */
	public static void resetCounter(){
		SortedMap<String, Counter> map=MetricsManager.registry.getCounters();
		for(String key:map.keySet()){
			Counter couter=map.get(key);
			couter.dec(couter.getCount());
		}
	}

}
