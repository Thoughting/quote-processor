package com.baidu.stock.process.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArraySet;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baidu.stock.common.AssetType;
import com.baidu.stock.jms.Producer;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.process.config.HQGloableConfig;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.quote.protocbuf.BidAskProto;
import com.baidu.stock.quote.protocbuf.SnapShotProto;
import com.baidu.stock.quote.protocbuf.StockBasicProto;

/**
 * 公共方法
 * @author dengjianli
 *
 */
public class CommonHelp {
	public static Logger logger = LoggerFactory.getLogger(CommonHelp.class);
	private static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
	public static String materialManagerUrl;
	public static Producer snapshotProducer;
	public static Producer notifyProducer;
	
	/**
	 * 法定假日
	 */
	public static CopyOnWriteArraySet<String> holidayset = new CopyOnWriteArraySet<String>();
	
	/**
	 * 计算毫秒的时间差
	 * @param dbfTime HHmmss
	 * @param hqTime  HHmmss
	 * @return 毫秒
	 */
	public static long getMilliSecondDiff(String dbfTime, String hqTime){
		long millsSecondDiff = 0;
		try {
			if(StringUtils.isNotEmpty(dbfTime) && StringUtils.isNotEmpty(hqTime)){
				DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date dbfDate = df.parse(dbfTime);
				Date hqDate = df.parse(hqTime);
				millsSecondDiff = dbfDate.getTime() - hqDate.getTime();
			}
		} catch (Exception e) {
			logger.error("计算毫秒的时间差异常");
		}
		return millsSecondDiff;
	}
	public static String compositID(String exchange, String stockCode) {
      return (exchange + "_" + stockCode);
  }
    // 读取A股交易时间 9:30-11:30, 13:00-15:00
    public static int getCNDuration(int eventTime) {
        int duration = 0;
        int hour = eventTime / 10000000;              // 取小时
        int minute = (eventTime % 10000000) / 100000; // 取分钟

        if (eventTime <= 93000000) {
            // 盘前
            duration = 0;
        } else if (eventTime <= 113000000) {
            // 上午： 小时 x 60 + 分钟数 - 570
            duration = hour * 60 + minute - 570;
        } else if (eventTime <= 130000000) {
            // 午间
            duration = 120;
        } else if (eventTime <= 150000000) {
            // 下午： 小时 x 60 + 分钟数 - 570 - 90
            duration = hour * 60 + minute - 570 - 90;
        } else {
            // 盘后
            duration = 240;
        }
        return duration;
    }
    /**
	 * 判断日期是不是交易日
	 * @param a 日期
	 * @return
	 */
	public static boolean isTradeDay(Calendar a){
		int d = a.get(Calendar.DAY_OF_WEEK);
		if(d == 1 || d == 7){
			return false;
		}
		String todayDate = yyyyMMdd.format(a.getTime());
		if(holidayset.contains(todayDate)){
			return false;
		}
		return true;
	}
	


	
	
	
	   /* 深圳:
	                指数：股票代码以39开头
	     A股： 股票代码以000 001 002 003 004或30开头
	     B股： 股票代码以20开头
	                基金：股票代码以15或16或18开头
	                国债：股票代码以10开头
	                企债：股票代码以11或12开头
	                回购：股票代码以13开头
	                上海：
	               指数：股票代码以000开头
	     A股： 股票代码以60开头
	     B股： 股票代码以900开头
	                基金：股票代码以50 51开头
	                回购：股票代码以20开头
	                国债：股票代码以02或01开头
	                企债：股票代码以12或11开头
	                其他：权证，期货，期权*/
	                
	/**
	 * 根据股票代码和交易所区分股票的类型
	 * 证券类型定义 0:A股票 1：期货，2：期权 3：外汇，4指数，5：场内基金，6：债券、7：认购权证，8：认沽权证，9： 牛证，10：熊证， 11：其他，12：表示场外，13：表示货币，14：表示B股，15：回购，16：场内的货币基金，17：港股基金
	 * @param SC 交易所 sh/sz
	 * @param ZQDM 股票代码
	 * @return
	 */
	public static int requireTypes(String SC, String ZQDM){
		if (SC.equalsIgnoreCase("sz")){
			if (ZQDM.startsWith("39")){
				return AssetType.INDEX.value();
			}
			else if (ZQDM.startsWith("000") || ZQDM.startsWith("001")|| ZQDM.startsWith("002")|| ZQDM.startsWith("003")|| ZQDM.startsWith("004")||ZQDM.startsWith("30")){
				return AssetType.A_STOCK.value();
			}else if (ZQDM.startsWith("20")){
				return AssetType.B_STOCK.value();
			}
            else if (ZQDM.startsWith("15") || ZQDM.startsWith("16")||ZQDM.startsWith("18")){
            	return AssetType.FUND.value();
			}else if (ZQDM.startsWith("13")){
				return AssetType.REPURCHASE.value();
			}
		}else if(SC.equalsIgnoreCase("sh")){
			if (ZQDM.startsWith("000")){
				return AssetType.INDEX.value();
			}
			else if (ZQDM.startsWith("60")){
				return AssetType.A_STOCK.value();
			}
           else if (ZQDM.startsWith("900")){
        	   return AssetType.B_STOCK.value();
			}
            else if (ZQDM.startsWith("50")||ZQDM.startsWith("51")){
            	return AssetType.FUND.value();
			}else if (ZQDM.startsWith("20")){
				return AssetType.REPURCHASE.value();
			}
		}
		return AssetType.OTHERS.value();
	}

	/**
	 * 指出里了处理: 指数  A股 B股  基金 回购
	 * 
	 * @param exchange
	 * @param stockCode
	 * @return
	 */
	public static boolean isFilter(String exchange,String stockCode){
		int type=requireTypes(exchange,stockCode);
		if(AssetType.OTHERS.value()==type){
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * 发送消息到activemq
	 * @param dataType   数据类型
	 * @param context        内容
	 */
	public static void sendSingalToMQ(String dataType,byte[] context){
		String mqurl=null;
		try{
			CommonHelp.notifyProducer.send(dataType,context);
		}
		catch(Exception ex){
			logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.MQ_ERROR_KEY, LogConstant.MQ_ERROR_VALUE,"发送消息[dataType: "+dataType+"]到MQ["+mqurl+"] error:" + FileUtil.printException(ex)));
		}
	}
	
	public static void sendSnapshotToMQ(String groupId,String dataType,byte[] context){
		String mqurl=null;
		try{
			CommonHelp.snapshotProducer.send(groupId, dataType,context);
		}
		catch(Exception ex){
			logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.MQ_ERROR_KEY, LogConstant.MQ_ERROR_VALUE,"发送消息[dataType: "+dataType+"]到MQ["+mqurl+"] error:" + FileUtil.printException(ex)));
		}
	}
	
	
	/**
	 * 时间转换
	 * @param time  时间 格式HH:mm
	 * @return
	 */
	public static Calendar setTimeHHmm(String time){
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		try{
			String[] tmp = time.split(":");
			int hour = Integer.parseInt(tmp[0]);
			int minute = Integer.parseInt(tmp[1]);
			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, minute);
			c.set(Calendar.SECOND, 0);
		}
		catch (Exception e){
			logger.error(FileUtil.printException(e));
		}
		return c;
	}
	
	public static boolean isWorkerFetchTimeRange(long curMillis){
		//start
		String startTime=HQGloableConfig.applicationSettings.getAppScheduleConfig().getWorker_start_time();//8:59
		Calendar initCalendar =setTimeHHmm(startTime);
		long initMillis = initCalendar.getTimeInMillis();
		//stop
		String stopTime=HQGloableConfig.applicationSettings.getAppScheduleConfig().getWorker_stop_time();//15:10
		Calendar shutdownCalendar=setTimeHHmm(stopTime);
		long shutdownMillis = shutdownCalendar.getTimeInMillis();
		if (initMillis < curMillis && curMillis<shutdownMillis) {
			return true;
		}
		return false;
	}
	
	public static boolean isLoadDiskCacheTimeRange(long currentMillis){
		//start
		String startTime=HQGloableConfig.applicationSettings.getMarketTime().getTransactionAMTime();//9:30
		Calendar startCalendar =setTimeHHmm(startTime);
		long startTimeInMillis = startCalendar.getTimeInMillis();
		//end
		String endTime=HQGloableConfig.applicationSettings.getMarketTime().getStopSendFullHQTime();//15:04
		Calendar endCalendar=setTimeHHmm(endTime);
		long endTimeInMillis= endCalendar.getTimeInMillis();
		if (startTimeInMillis < currentMillis && currentMillis<endTimeInMillis) {
			return true;
		}
		return false;
	}
	
	public static void setSnapshotProducer(Producer snapshotProducer) {
		CommonHelp.snapshotProducer = snapshotProducer;
	}
	public static void setNotifyProducer(Producer notifyProducer) {
		CommonHelp.notifyProducer = notifyProducer;
	}
	
	/**
	 * 组装SnapShotProto.SnapShot类型对象
	 * @param snapShot
	 * @return
	 */
	public static SnapShotProto.SnapShot convertSnapShot(HQSnapShot snapShot){
		SnapShotProto.SnapShot.Builder build=SnapShotProto.SnapShot.newBuilder();
		StockBasicProto.StockBasic.Builder stockBasic=StockBasicProto.StockBasic.newBuilder();
		stockBasic.setExchange(snapShot.getExchange());
		stockBasic.setStockCode(snapShot.getStockCode());
		stockBasic.setStockName(snapShot.getStockName());
		stockBasic.setAsset(snapShot.getAsset());
		stockBasic.setStockStatus(snapShot.getStockStatus());
		build.setStockBasic(stockBasic);
		build.setDate(snapShot.getDate());
		build.setTime(snapShot.getTime());
		build.setHigh(snapShot.getHigh());
		build.setLow(snapShot.getLow());
		build.setOpen(snapShot.getOpen());
		build.setClose(snapShot.getClose());
		build.setPreClose(snapShot.getPreClose());
		build.setDealCount(snapShot.getDealCount());
		build.setNowVol(snapShot.getNowVolume());
		build.setVolume(snapShot.getVolume());
		build.setAmount(snapShot.getAmount());
		build.setTotalShareCapital(snapShot.getTotalAmount());
		build.setCirculatingCapital(snapShot.getTradeVolume());
		build.setNetAssetsPerShare(snapShot.getNetAssets());
		build.setPerShareEarn(snapShot.getStockProfit());
		build.setInside(snapShot.getInSide());
		build.setOutside(snapShot.getOutSide());
		
		for(int i = 0;snapShot.getSellp()!= null && i < snapShot.getSellp().length;i++){
			BidAskProto.BidAsk.Builder askProto=BidAskProto.BidAsk.newBuilder();
			askProto.setPrice(snapShot.getSellp()[i]);
			askProto.setVolume(Long.valueOf(snapShot.getSellv()[i]).intValue());
			build.addAsk(askProto);
		}
		
		for(int i = 0; snapShot.getBuyp() != null && i < snapShot.getBuyp().length;i++){
			BidAskProto.BidAsk.Builder bidProto=BidAskProto.BidAsk.newBuilder();
			bidProto.setPrice(snapShot.getBuyp()[i]);
			bidProto.setVolume(Long.valueOf(snapShot.getBuyv()[i]).intValue());
			build.addBid(bidProto);
		}
		build.setVolumeRatio(snapShot.getVolumeRatio());
		build.setCapitalization(snapShot.getCapitalization());
		build.setCurrencyValue(snapShot.getCurrencyValue());
		build.setTurnoverRatio(snapShot.getTurnoverRatio());
		build.setMrqPeratio(snapShot.getMrqPeratio());
		build.setTtmPeratio(snapShot.getTtmPeratio());
		build.setLyrPeratio(snapShot.getLyrPeratio());
		build.setBvRatio(snapShot.getBvRatio());
		build.setPsRatio(snapShot.getPsRatio());
		build.setVolumeRatio(snapShot.getVolumeRatio());
		build.setWeibiRatio(snapShot.getWeibiRatio());
		build.setNetFundsFlow(snapShot.getBigNetflows());
		build.setNetChange(snapShot.getNetChange());
		build.setNetChangeRatio(snapShot.getNetChangeRatio());
		build.setAmplitudeRatio(snapShot.getAmplitudeRatio());
		build.setLimitUp(snapShot.getLimitUp());
		build.setLimitDown(snapShot.getLimitDown());
		build.setRiseNum(snapShot.getRiseNum());
		build.setFallNum(snapShot.getFallNum());
		build.setFairNum(snapShot.getFairNum());
		//性能测试
		build.setScanTime(snapShot.getStartTimeStamp());
		return build.build();
	}
}
