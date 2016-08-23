package com.baidu.stock.process.cron;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.process.cache.mannage.HQCacheManager;
import com.baidu.stock.process.config.HQGloableConfig;
import com.baidu.stock.process.cron.singal.ExchangeStatusSingalNotify;
import com.baidu.stock.process.fetch.core.HQThreadListenerContainer;
import com.baidu.stock.process.monitor.metrics.MetricsManager;
import com.baidu.stock.process.util.CacheDiskStoreUtil;
import com.baidu.stock.process.util.CommonHelp;
import com.baidu.stock.process.util.HQStatus;

/**
 * 
 * @author dengjianli
 *
 */
public class ScheduleSignalTaskJob {
	private Logger logger = LoggerFactory.getLogger(ScheduleSignalTaskJob.class); 
	private HQStatus status;
	private static SimpleDateFormat dateformat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E");

	public ScheduleSignalTaskJob(HQStatus status){
		this.status=status;
	}
	
	public void execute(){
			if(HQStatus.INIT.equals(status)){
				ExchangeStatusSingalNotify.loadMaterial();
			}else if(HQStatus.NOTOPEN.equals(status)){
			    ExchangeStatusSingalNotify.sendOpeningSingal();
		    }else if(HQStatus.AUCTION.equals(status)){
			    ExchangeStatusSingalNotify.sendAuctionSingal();
		    }else if(HQStatus.TRANSACTIONAM.equals(status)){
			    ExchangeStatusSingalNotify.sendTransactionSingal("am");
		    }else if(HQStatus.LUNCHBREAK.equals(status)){
			    ExchangeStatusSingalNotify.sendLunchSingal();
		    }else if(HQStatus.TRANSACTIONPM.equals(status)){
			    ExchangeStatusSingalNotify.sendTransactionSingal("pm");
		    }else if(HQStatus.CLOSE.equals(status)){
			    ExchangeStatusSingalNotify.sendCloseSingal();
		    }else if(HQStatus.STARTWORKER.equals(status)){
				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
				if(CommonHelp.isTradeDay(c)){
			//启动开始前调用销毁方法,防止在非交易时间内重启服务导致定时任务没有执行destoryAll方法
		    	resetAllRecord();
			//start work
			ConcurrentHashMap<String, HQThreadListenerContainer>workerCacheInMemory=HQGloableConfig.workerCacheInMemory;
			for(String key:workerCacheInMemory.keySet()){
				HQThreadListenerContainer worker=workerCacheInMemory.get(key);
				if(!worker.isRunning()){
					worker.isExchangeOpenFlag=false;
					worker.start();
					logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,dateformat.format(new Date())+" 定时任务-启动抓取行情worker:"+worker.getWorkName()));
				}
			}
				}
		}
		   else if(HQStatus.STOPWORKER.equals(status)){
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
			if(CommonHelp.isTradeDay(c)){
			//stop work
			ConcurrentHashMap<String, HQThreadListenerContainer>workerCacheInMemory=HQGloableConfig.workerCacheInMemory;
			for(String key:workerCacheInMemory.keySet()){
				HQThreadListenerContainer worker=workerCacheInMemory.get(key);
				if(worker.isRunning()){
					worker.stop();
					//防止停止的同时正在抓取导致isExchangeOpenFlag值不一致
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					worker.isExchangeOpenFlag=false;
					logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,dateformat.format(new Date())+" 定时任务-停止抓取行情worker:"+worker.getWorkName()));
				    }
			     }
		      }
		    }
		    else if(HQStatus.CLEAN.equals(status)){
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
			if(CommonHelp.isTradeDay(c)){
			ConcurrentHashMap<String, HQThreadListenerContainer>workerCacheInMemory=HQGloableConfig.workerCacheInMemory;
			for(String key:workerCacheInMemory.keySet()){
				HQThreadListenerContainer worker=workerCacheInMemory.get(key);
				//还原抓取条件判断状态,true:已经第一次抓取以及发送股票代码表，false：未开始第一次抓取以及发送股票代码表
					worker.isExchangeOpenFlag=false;
			}
			resetAllRecord();
			}
		  }
	}
	
	
	/**
	 * 清理服务内存数据
	 */
	private void resetAllRecord(){
		//重置耗时时间
		MetricsManager.timeConsuming=0;
		//清理mq统计的写入数据
		MetricsManager.mqRequest.reset();
		//重置日志统计计数器,清理前一日的异常信息
		MetricsManager.resetCounter();
		//清理磁盘缓存
		CacheDiskStoreUtil.cleanDiskCache();
		//重置记录的下发行情时间点
		HQGloableConfig.lastSHHQtime=null;
		HQGloableConfig.lastSZHQTime=null;
	}
}
