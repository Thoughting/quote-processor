package com.baidu.stock.process.fetch.adapter;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.process.channel.HQExecutorOutChannel;
import com.baidu.stock.process.fetch.IFetchAction;
import com.baidu.stock.process.fetch.core.HQThreadListenerContainer;
import com.baidu.stock.process.fetch.reader.HQReader;
import com.baidu.stock.process.monitor.ext.ExtTimer;
import com.baidu.stock.process.monitor.metrics.MetricsManager;
import com.baidu.stock.process.monitor.zk.ZkMonitorManager;
import com.baidu.stock.process.util.FileUtil;

/**
 * 
 * @author dengjianli
 *
 * @param <T>
 */
public abstract class AbstractDataSourcesAdapterHandler<T> implements DataSourcesAdapterHandler<T>{
	protected static Logger logger = LoggerFactory.getLogger(AbstractDataSourcesAdapterHandler.class);
	protected HQExecutorOutChannel executorOutChannel;
	//构造各自sh和sz各自的线程容器
	protected HQThreadListenerContainer container;
	protected int fetchIntervalMillis; 
	protected HQReader[]hqReader=null;//默认3个专线通道
	public AbstractDataSourcesAdapterHandler(HQReader[]hqReader){
		this.hqReader=hqReader;
	}
	/**
	 * 从文件抓取数据
	 * @param action
	 */
	protected List<T> fetchDataFromFile(IFetchAction<T> action){
		//间隔时间控制，如果不是master，则间隔时间5秒
		try {
			if(ZkMonitorManager.isMaster()){
			Thread.sleep(fetchIntervalMillis);
			}else{
			Thread.sleep(2000);
			logger.info("当前服务状态是备服务状态,因此间隔时间2秒抓取一次(主服务是0.5秒)计算但不发送MQ.");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		List<T> result=null;
		ExtTimer.Context context = MetricsManager.fetchTimer.time();
		try{
		 result=action.doFetch();
		}catch(Exception ex){
			logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"从文件抓取数据异常:\n"+ FileUtil.printException(ex)));
		}finally{
			context.stop();	
		}
		 return (List<T>) result;
	}
	
	
	private void checkIsRefresh(HQReader... hqReader){
		for(final HQReader reader:hqReader){
		new Thread(new Runnable() {
			@Override
			public void run() {
				reader.checkRefresh();
			}
		}).start();
		}
	}
	
	
	/**
	 * 至少一个配置通道配置
	 * 1-->2
	 * |   |
	 * 2<--1
	 */
	protected  HQReader getHQReader() {
		final HQReader firstHqReader=getHqReaderArray()[0];
		if(!firstHqReader.isFileDataIsRefresh() && getHqReaderArray().length>1){
			HQReader thenHqReader=getHqReaderArray()[1];
			if(!thenHqReader.isFileDataIsRefresh()){
				logger.warn(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_WARN_KEY, LogConstant.DEFAULT_WARN_VALUE,"发现行情文件[通道1:"+firstHqReader.getHQurl()+" 通道2:"+thenHqReader.getHQurl()+"]都不刷新异常,请立刻检查."));
				checkIsRefresh(getHqReaderArray()[0],getHqReaderArray()[1]);
			 }else{
				logger.warn(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_WARN_KEY, LogConstant.DEFAULT_WARN_VALUE,"发现行情文件[通道1:"+firstHqReader.getHQurl()+"]不刷新异常,请立刻检查."));
				checkIsRefresh(getHqReaderArray()[0]);
				return thenHqReader;
			 }
		    }
		    return firstHqReader;
		  }
	
	public HQReader[] getHqReaderArray() {
		return hqReader;
	}
}
