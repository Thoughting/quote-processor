package com.baidu.stock.process.cron.singal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.material.domain.IndexComponent;
import com.baidu.stock.material.domain.StockExchangeRate;
import com.baidu.stock.material.domain.StockFinance;
import com.baidu.stock.material.domain.StockStopInfo;
import com.baidu.stock.material.facade.MaterialManager;
import com.baidu.stock.process.cache.AssistDataCache;
import com.baidu.stock.process.cache.FinacialInfoCache;
import com.baidu.stock.process.cache.IndexBagCache;
import com.baidu.stock.process.cache.StockStatusCache;
import com.baidu.stock.process.cache.mannage.HQCacheManager;
import com.baidu.stock.process.monitor.zk.ZkMonitorManager;
import com.baidu.stock.process.util.CommonHelp;
import com.baidu.stock.process.util.HQConstant;
import com.baidu.stock.quote.protocbuf.ExchangeStatusProto;
import com.baidu.stock.quote.protocbuf.ExchangeStatusProto.ExchangeStatus.ExchangeStatusType;

/**
 * 状态信号通知
 * @author dengjianli
 *
 */
public class ExchangeStatusSingalNotify{
	public static Logger logger = LoggerFactory.getLogger(ExchangeStatusSingalNotify.class);
	private static SimpleDateFormat dateformat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E");

	/**
	 * 发送交易日
	 */
	public static void sendOpeningSingal() {
		if(ZkMonitorManager.isMaster()){
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		if(CommonHelp.isTradeDay(c)){
			ExchangeStatusProto.ExchangeStatus.Builder exchangeStatus = ExchangeStatusProto.ExchangeStatus.newBuilder();
			exchangeStatus.setExchange("sz");
			exchangeStatus.setExchangeStatusType(ExchangeStatusType.NOTOPENED);
			CommonHelp.sendSingalToMQ("exchangeStatus", exchangeStatus.build().toByteArray());
			exchangeStatus = ExchangeStatusProto.ExchangeStatus.newBuilder();
			exchangeStatus.setExchange("sh");
			exchangeStatus.setExchangeStatusType(ExchangeStatusType.NOTOPENED);
			CommonHelp.sendSingalToMQ("exchangeStatus", exchangeStatus.build().toByteArray());
			logger.info(dateformat.format(new Date())+"  定时任务-发送交易日未开盘信号");
		}
		else{
			//非交易日
			ExchangeStatusProto.ExchangeStatus.Builder exchangeStatus = ExchangeStatusProto.ExchangeStatus.newBuilder();
			exchangeStatus.setExchange("sz");
			exchangeStatus.setExchangeStatusType(ExchangeStatusType.FESTIVELSBREAK);
			CommonHelp.sendSingalToMQ("exchangeStatus", exchangeStatus.build().toByteArray());
			exchangeStatus = ExchangeStatusProto.ExchangeStatus.newBuilder();
			exchangeStatus.setExchange("sh");
			exchangeStatus.setExchangeStatusType(ExchangeStatusType.FESTIVELSBREAK);
			CommonHelp.sendSingalToMQ("exchangeStatus", exchangeStatus.build().toByteArray());
			logger.info(dateformat.format(new Date())+" 定时任务-发送非交易日不开盘信号");
		}
	    }else{
	    	logger.info(dateformat.format(new Date())+" 判断当前是备服务,不执行-定时任务-发送非交易日信号");
	    }
	}
	
	/**
	 * 加载基础数据,不判断主备关系,都必须加载到各自jvm内存中
	 */
	public static void loadMaterial() {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		if(CommonHelp.isTradeDay(c)){
		//清理所有的缓存
		HQCacheManager.getCacheManager().clearAll();
		logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,dateformat.format(new Date())+" 定时任务-清理所有的缓存完成 "));
		logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,dateformat.format(new Date())+" 定时任务-加载依赖基础数据开始."));
		//初始化依赖数据
		MaterialManager materialManager=new MaterialManager(CommonHelp.materialManagerUrl);
		//初始化节假日信息
		Set<String>hilidayset=materialManager.getHolidays(null);
		if(hilidayset!=null && hilidayset.size()>0){
		CommonHelp.holidayset.clear();
		CommonHelp.holidayset.addAll(hilidayset);
		logger.info(dateformat.format(new Date())+" 定时任务-初始化节假日信息");
		}else{
			logger.warn(dateformat.format(new Date())+" 定时任务-初始化节假日信息列表为空");
		}
		 //初始化财务信息 key:市场_股票编码 [sh_600000]
		 Map<String,StockFinance> financesMap=materialManager.getStockFinances(null);
		 if(null!=financesMap && financesMap.size()>0){
			 for(String key:financesMap.keySet()){
			 StockFinance stockFinance=financesMap.get(key);
			 FinacialInfoCache.add(key, stockFinance);
			 }
		logger.info(dateformat.format(new Date())+" 定时任务-初始化财务信息");
		 }else{
			 logger.warn(dateformat.format(new Date())+" 定时任务-初始化财务信息为空");
		 }
		
		 //汇率
		 StockExchangeRate stockExchangeRate=materialManager.getForeignExchange(null);
		 if(stockExchangeRate!=null){
		 AssistDataCache.add( HQConstant.FOREIGNEXCHANGE_KEY, "USD", stockExchangeRate.getUsd());
		 AssistDataCache.add( HQConstant.FOREIGNEXCHANGE_KEY, "HKDOLLAR", stockExchangeRate.getHkd());
		 logger.info(dateformat.format(new Date())+" 定时任务-初始化汇率 ");
		 }else{
			 logger.warn(dateformat.format(new Date())+" 定时任务-初始化汇率为空");
		 }
		 //5日均价
		 Map<String, Long> fiveVolumeMap=materialManager.getStockMaFiveVolume(null);
		 if(null!=fiveVolumeMap && fiveVolumeMap.size()>0){
			 for(String key:fiveVolumeMap.keySet()){
				 Long value=fiveVolumeMap.get(key);
				 AssistDataCache.add(key,  HQConstant.DAY5_AVERAGE_VOLUME_KEY, value.doubleValue());
			 }
		 logger.info(dateformat.format(new Date())+" 定时任务-初始化5日均价");
		 }else{
			 logger.warn(dateformat.format(new Date())+" 定时任务-初始化5日均价列表为空");
		 }
		
		 List<StockStopInfo>lstStopStock=materialManager.getStockStopInfos(null);
		 if(lstStopStock!=null && lstStopStock.size()>0){
		 for(StockStopInfo ssi:lstStopStock){
			 String key=CommonHelp.compositID(ssi.getExchange(), ssi.getStockCode());
			 StockStatusCache.add(key, ssi); 
		 }
		 logger.info(dateformat.format(new Date())+" 定时任务-初始化停牌股票列表"); 
	     }else{
	    	 logger.warn(dateformat.format(new Date())+" 定时任务-初始化停牌股票列表为空");  
	     }
		 
		 Map<String,IndexComponent> indexBag=materialManager.getIndexComponents(null);
		 if(indexBag!=null && indexBag.size()>0){
		 for(String key:indexBag.keySet()){
			 IndexComponent indexComponent=indexBag.get(key);
			 IndexBagCache.add(key, indexComponent);
		 }
		 logger.info(dateformat.format(new Date())+" 定时任务-初始化指数的成份股列表"); 
			logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,dateformat.format(new Date())+" 定时任务-加载依赖基础数据结束."));
		 }else{
			 logger.warn(dateformat.format(new Date())+" 定时任务-初始化指数的成份股列表为空");  
		 }
		}else{
			logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,dateformat.format(new Date())+" 非交易日,忽略 定时任务-加载依赖基础数据"));
		}
	}
	/**
	 * 发送集合计价
	 */
	public static void sendAuctionSingal() {
		if(ZkMonitorManager.isMaster()){
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		if(CommonHelp.isTradeDay(c)){
//			long currentTime=Long.valueOf(HHmmssFormat.format(new Date(System.currentTimeMillis())));
//			CountDownLatch latch=new CountDownLatch(2);
//			while(latch.getCount()!=0){
//				if(StringUtils.isNotBlank(HQGloableConfig.lastSHHQtime)){
//				long fileCurrentTime=Long.valueOf(HQGloableConfig.lastSHHQtime);
//				   if(currentTime<fileCurrentTime){
//					   try{
//						ExchangeStatusProto.ExchangeStatus.Builder exchangeStatus = ExchangeStatusProto.ExchangeStatus.newBuilder();
//						exchangeStatus.setExchange("sz");
//						exchangeStatus.setExchangeStatusType(ExchangeStatusType.AUCTION);
//						CommonHelp.sendSingalToMQ("exchangeStatus", exchangeStatus.build().toByteArray());
//				       }catch(Exception e){
//				 			logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.MQ_ERROR_KEY , LogConstant.MQ_ERROR_VALUE ,"定时任务-sz 发送集合竞价信息到MQ "+FileUtil.printException(e)));
//					   }finally{
//						 latch.countDown();
//					    }
//				   }else{
//					   try {
//						   logger.info("进入发送sz集合竞价信号信息,发现文件时间和当前时间不一致且小于当前时间则处于等待状态.");
//						Thread.sleep(1000);
//					} catch (InterruptedException e) {
//						Thread.currentThread().interrupt();
//					}
//				   }
//				}
//				if(StringUtils.isNotBlank(HQGloableConfig.lastSZHQTime)){
//					long fileCurrentTime=Long.valueOf(HQGloableConfig.lastSZHQTime);
//					   if(currentTime<fileCurrentTime){
//						   try{
//						    ExchangeStatusProto.ExchangeStatus.Builder  exchangeStatus = ExchangeStatusProto.ExchangeStatus.newBuilder();
//							exchangeStatus.setExchange("sh");
//							exchangeStatus.setExchangeStatusType(ExchangeStatusType.AUCTION);
//							CommonHelp.sendSingalToMQ("exchangeStatus", exchangeStatus.build().toByteArray());
//					         }catch(Exception e){
//					 			logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.MQ_ERROR_KEY , LogConstant.MQ_ERROR_VALUE ,"定时任务-sh 发送集合竞价信息到MQ异常\n "+FileUtil.printException(e)));
//					         }finally{
//						     latch.countDown();
//					         }
//					   }else{
//						   try {
//							   logger.info("进入发送sz集合竞价信号信息,发现文件时间和当前时间不一致且小于当前时间则处于等待状态.");
//								Thread.sleep(1000);
//							} catch (InterruptedException e) {
//								Thread.currentThread().interrupt();
//							}
//					   }
//					}
//			}
			ExchangeStatusProto.ExchangeStatus.Builder exchangeStatus = ExchangeStatusProto.ExchangeStatus.newBuilder();
			exchangeStatus.setExchange("sz");
			exchangeStatus.setExchangeStatusType(ExchangeStatusType.AUCTION);
			CommonHelp.sendSingalToMQ("exchangeStatus", exchangeStatus.build().toByteArray());
			exchangeStatus = ExchangeStatusProto.ExchangeStatus.newBuilder();
			exchangeStatus.setExchange("sh");
			exchangeStatus.setExchangeStatusType(ExchangeStatusType.AUCTION);
			CommonHelp.sendSingalToMQ("exchangeStatus", exchangeStatus.build().toByteArray());
			logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,dateformat.format(new Date())+" 定时任务-发送集合竞价信息到MQ"));
		}else{
			//这里的判断其实没有用
			logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,dateformat.format(new Date())+" 定时任务-非交易日,不发送集合竞价信息到MQ"));
		}
		}else{
			logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,dateformat.format(new Date())+" 判断当前是备服务,不执行-定时任务,不发送集合竞价信息到MQ"));
		}
	}
	
	
	/**
	 * 发送交易信号
	 * @param transactionMsg
	 */
	public static void sendTransactionSingal(String transactionMsg){
		if(ZkMonitorManager.isMaster()){
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		if(CommonHelp.isTradeDay(c)){
			ExchangeStatusProto.ExchangeStatus.Builder exchangeStatus = ExchangeStatusProto.ExchangeStatus.newBuilder();
			exchangeStatus.setExchange("sz");
			exchangeStatus.setExchangeStatusType(ExchangeStatusType.TRANSACTION);
			CommonHelp.sendSingalToMQ("exchangeStatus", exchangeStatus.build().toByteArray());
			exchangeStatus = ExchangeStatusProto.ExchangeStatus.newBuilder();
			exchangeStatus.setExchange("sh");
			exchangeStatus.setExchangeStatusType(ExchangeStatusType.TRANSACTION);
			CommonHelp.sendSingalToMQ("exchangeStatus", exchangeStatus.build().toByteArray());
			logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,dateformat.format(new Date())+"定时任务-发送"+transactionMsg+"交易信息到MQ"));
		}
		else{//这里的判断其实没有用
			logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,dateformat.format(new Date())+"定时任务-非交易日,不发送交易信息到MQ"));
		}
		}else{
			logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,dateformat.format(new Date())+"判断当前是备服务,不执行-定时任务,不发送交易信息到MQ"));
		}
	}
	
	/**
	 * 发送午休信号
	 */
	public static void sendLunchSingal(){
		if(ZkMonitorManager.isMaster()){
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		if(CommonHelp.isTradeDay(c)){
			ExchangeStatusProto.ExchangeStatus.Builder exchangeStatus = ExchangeStatusProto.ExchangeStatus.newBuilder();
			exchangeStatus.setExchange("sz");
			exchangeStatus.setExchangeStatusType(ExchangeStatusType.LUNCHBREAK);
			CommonHelp.sendSingalToMQ("exchangeStatus", exchangeStatus.build().toByteArray());
			exchangeStatus = ExchangeStatusProto.ExchangeStatus.newBuilder();
			exchangeStatus.setExchange("sh");
			exchangeStatus.setExchangeStatusType(ExchangeStatusType.LUNCHBREAK);
			CommonHelp.sendSingalToMQ("exchangeStatus", exchangeStatus.build().toByteArray());
			logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,dateformat.format(new Date())+"定时任务-发送中午暂时停盘信息到MQ"));
		}
		else{//这里的判断其实没有用
			logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,dateformat.format(new Date())+"定时任务-非交易日,不发送中午暂时停盘信息到MQ"));
		}
		}else{
			logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,dateformat.format(new Date())+"判断当前是备服务,不执行-定时任务,不发送中午暂时停盘信息到MQ"));
		}
	}
	
	/**
	 * 发送闭盘信号
	 */
	public static void sendCloseSingal() {
		if(ZkMonitorManager.isMaster()){
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		if(CommonHelp.isTradeDay(c)){
			ExchangeStatusProto.ExchangeStatus.Builder exchangeStatus = ExchangeStatusProto.ExchangeStatus.newBuilder();
			exchangeStatus.setExchange("sz");
			exchangeStatus.setExchangeStatusType(ExchangeStatusType.CLOSED);
			CommonHelp.sendSingalToMQ("exchangeStatus", exchangeStatus.build().toByteArray());
			exchangeStatus = ExchangeStatusProto.ExchangeStatus.newBuilder();
			exchangeStatus.setExchange("sh");
			exchangeStatus.setExchangeStatusType(ExchangeStatusType.CLOSED);
			CommonHelp.sendSingalToMQ("exchangeStatus", exchangeStatus.build().toByteArray());
			logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,dateformat.format(new Date())+" 定时任务-发送闭盘信息到MQ"));
		}
		else{//这里的判断其实没有用
			logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,dateformat.format(new Date())+" 定时任务-非交易日,不发送闭盘信息到MQ"));
		}
		}else{
			logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,dateformat.format(new Date())+" 判断当前是备服务,不执行-定时任务,不发送闭盘信息到MQ"));
		}
	}
}
