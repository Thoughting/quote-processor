package com.baidu.stock.process.fetch.reader.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.baidu.stock.common.StockStatusType;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.process.cache.HQSnapShotCache;
import com.baidu.stock.process.cache.StockStatusCache;
import com.baidu.stock.process.config.HQGloableConfig;
import com.baidu.stock.process.fetch.io.DBFFileReader;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.process.fetch.reader.AbstractHQReader;
import com.baidu.stock.process.util.CommonHelp;
import com.baidu.stock.process.util.FileUtil;
import com.baidu.stock.process.util.HQConstant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * sh dbf 文件读取解析处理
 * @author dengjianli
 *
 */
public class SHProcessor extends AbstractHQReader{
	private static Logger logger =LoggerFactory.getLogger(SHProcessor.class);
	private DBFFileReader dbfReader;
	public SHProcessor(String hqURL){
		super(hqURL);
		dbfReader = new DBFFileReader("sh",hqURL);
	}

	
	@Override
	public List<HQSnapShot> process() {
		return parseDBFData();
	}

	@Override
	public List<HQSnapShot> preFetchHQ() {
		List<HQSnapShot> changeHQList=null;
		try{
			changeHQList = parseDBFData();
		}
		catch(Exception ex){
	         logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"初始化sh行情dbf文件失败/n" + FileUtil.printException(ex)));
		}
		return changeHQList;
	}
	
	
/**
 * 处理快照解析
 * @return
 */
	private List<HQSnapShot> parseDBFData(){
		List<HQSnapShot> changeHQList = new ArrayList<HQSnapShot>(3000);
		Calendar c= Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		String curSystemDate = yyyyMMddFormat.format(c.getTime());
		//用于判断文件是否更新标志
		long lastUnModifyTimeStamp=-1;
		try{
			List<Map<String,String>> lstHQ = dbfReader.read();
			for(int i = 0; i < lstHQ.size();i++){
				Map<String,String> hqMap = lstHQ.get(i);
				String zeroCode = "000000";
				//判断是否头行信息
				if(hqMap.get("S1").equals(zeroCode)){
					String bs = "0";
					dbfDate = hqMap.get("S6");
					String exchangeState =hqMap.get("S11");
					String currentDBFTime =hqMap.get("S2");
					//判断日期是否正确或是否开盘
					if(!dbfDate.equals(curSystemDate)||(!exchangeState.equals(bs))){
						logger.warn(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_WARN_KEY, LogConstant.DEFAULT_WARN_VALUE,"sh 当前时间:"+currentDBFTime+" 闭市标志:"+exchangeState+"当前 处于闭市"));
						return null;
					}
					if(currentDBFTime.equals(dbfTime)){
						lastUnModifyTimeStamp=System.currentTimeMillis();
						logger.warn(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_WARN_KEY, LogConstant.DEFAULT_WARN_VALUE,"sh 当前时间:"+currentDBFTime+" 上次更新时间: "+dbfTime+" 发现文件没有变化"));
						return null; 
					}
					dbfTime =currentDBFTime;
					HQGloableConfig.lastSHHQtime=currentDBFTime;
				}else{
					HQSnapShot snapShot= readSSHQValue(hqMap);
					if(snapShot != null){
						//等于null 表示数据没有发生变化
						HQSnapShot newSnapshot=snapShot.clone();
						 newSnapshot.setStartTimeStamp(System.currentTimeMillis());
						changeHQList.add(newSnapshot);
					}
				}
			}
		}
		catch(Exception ex){
			logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"sH parseDBFData error:"+ FileUtil.printException(ex)));
		}finally{
	        checkDataRefresh(lastUnModifyTimeStamp);
		}
		return changeHQList;
	}
	
	public void checkRefresh(){
		long lastUnModifyTimeStamp=-1;
		try{
			List<Map<String,String>>lstHQstr = dbfReader.read();
				Map<String,String> m = lstHQstr.get(0);
				String zeroCode = "000000";
				if(m.get("S1").equals(zeroCode)){
					String currentDBFTime=m.get("S2");
					if(currentDBFTime.equals(dbfTime)){
						lastUnModifyTimeStamp=System.currentTimeMillis();
					}
					dbfTime =currentDBFTime;
				}else{
					logger.error("读取的第一行不是头信息异常.");
				}
		}
		catch(Exception ex){
		   logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"sh parseDBFData error:"+ FileUtil.printException(ex)));
		}finally{
			checkDataRefresh(lastUnModifyTimeStamp);
		}
	}
	
	@Override
	public HQSnapShot readSSHQValue(Map<String, String> m) {
		HQSnapShot snapShot = null;
		String stockCode = m.get("S1");
		String stockName=m.get("S2");
		//熔断
		String blown=m.get("f33");
		//过滤股票
		if(CommonHelp.isFilter(HQConstant.shExchangeId, stockCode)){
			return null;
		}
		String compositID=CommonHelp.compositID( HQConstant.shExchangeId ,stockCode);
		snapShot=HQSnapShotCache.querySnapShot(compositID);
		if(null==snapShot){
			snapShot=new HQSnapShot();
			snapShot.setExchange(HQConstant.szExchangeId);
			snapShot.setStockCode(stockCode);
			snapShot.setStockName(stockName);
			snapShot.setCompositID(compositID);
			setAsset(snapShot);
			 if(StockStatusCache.isStopStock(compositID)){
			snapShot.setStockStatus(StockStatusType.SUSPENDED.value());
			 }else{
			snapShot.setStockStatus(StockStatusType.NORMAL.value());
			 }
			HQSnapShotCache.add(compositID, snapShot);
		}
		//判断是否有变化
		boolean isChange = false;
		float nowprice = 0;
		float preClose=0;
		long volume = 0;
		//以下暂时不优化,数据文件中的内容不都是合法的数据,比如:--,所以解析比较特殊,因此暂时这种粗暴方式
		try
		{
			nowprice = Float.parseFloat(m.get("S8"));
		}
		catch(Exception ex){}
		
		try
		{
			volume = Long.parseLong(m.get("S11"));
		}
		catch(Exception ex){}
		

		long[] buyv = new long[5];
		long[] sellv = new long[5];
		float[] buyp = new float[5];
		float[] sellp = new float[5];

		try
		{
			sellp[4] = Float.parseFloat(m.get("S32"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			sellv[4] = Integer.parseInt(m.get("S33"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			sellp[3] = Float.parseFloat(m.get("S30"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			sellv[3] = Integer.parseInt(m.get("S31"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			sellp[2] = Float.parseFloat(m.get("S24"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			sellv[2] = Integer.parseInt(m.get("S25"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			sellp[1] = Float.parseFloat(m.get("S22"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			sellv[1] = Integer.parseInt(m.get("S23"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			sellp[0] = Float.parseFloat(m.get("S10"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			sellv[0] = Integer.parseInt(m.get("S21"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			buyp[0] = Float.parseFloat(m.get("S9"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			buyv[0] = Integer.parseInt(m.get("S15"));
		}
		catch (Exception e)
		{
		}

		try
		{
			buyp[1] = Float.parseFloat(m.get("S16"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			buyv[1] = Integer.parseInt(m.get("S17"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			buyp[2] = Float.parseFloat(m.get("S18"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			buyv[2] = Integer.parseInt(m.get("S19"));
		}
		catch (Exception e)
		{
		}

		try
		{
			buyp[3] = Float.parseFloat(m.get("S26"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			buyv[3] = Integer.parseInt(m.get("S27"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			buyp[4] = Float.parseFloat(m.get("S28"));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			buyv[4] = Integer.parseInt(m.get("S29"));
		}
		catch (Exception e)
		{
		}
		if(null==snapShot.getDate() || !dbfDate.equals(snapShot.getDate())){
			//日期发生变化
			isChange = true;
		}
		else if(snapShot.getClose()!= nowprice){
			//价格变化
			isChange = true;
		}
		else if(snapShot.getVolume() < volume){
			//量变化
			isChange = true;
		}else if(isWDChange(buyp, buyv, sellp, sellv, snapShot))
		{
			//五档变化
			isChange = true;
		}else if (StringUtils.isNotEmpty(dbfTime) && isSendFullHqTime(Integer.parseInt(dbfTime))
					&& CommonHelp.getMilliSecondDiff(dbfDate + dbfTime, dbfDate + snapShot.getTime()) >=HQGloableConfig.applicationSettings.getBaseConfig().getSendFullTimeMillis())
		{
			//时间变化
			isChange = true;
		}
		if(!isChange){
         logger.warn(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_WARN_KEY, LogConstant.DEFAULT_WARN_VALUE,"当前行情["+compositID+"]没有变化"));
			return null;
		}
		snapShot.setDate(dbfDate);
		snapShot.setTime(dbfTime);
		int timeLen = 5;
		if(snapShot.getTime().length() == timeLen){
			snapShot.setTime("0"+dbfTime);
		}
		
		try
		{
			preClose=Float.parseFloat(m.get("S3"));
		}catch(Exception e){}
		
		
		try
		{
			snapShot.setOpen(Float.parseFloat(m.get("S4")));
		}
		catch (Exception e)
		{
		}

		
		try
		{
			snapShot.setAmount(Double.parseDouble(m.get("S5")));
		}
		catch (Exception e)
		{
		}
		
		
		
		try
		{
			snapShot.setHigh(Float.parseFloat(m.get("S6")));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			snapShot.setLow(Float.parseFloat(m.get("S7")));
		}
		catch (Exception e)
		{
		}
		
		snapShot.setClose(nowprice);
		snapShot.setPreClose(preClose);
		snapShot.setNowVolume((volume - snapShot.getVolume()));
		snapShot.setVolume(volume);
		
		//买卖5档
		snapShot.setSellp(sellp);
		snapShot.setSellv(sellv);
				
		snapShot.setBuyp(buyp);
		snapShot.setBuyv(buyv);
//		long auctionStartTime=HQGloableConfig.applicationSettings.getMarketTime().getAuctionStartTimeForLong();
//		long am=HQGloableConfig.applicationSettings.getMarketTime().getAmTime();
//		long close=HQGloableConfig.applicationSettings.getMarketTime().getClosedTimeForLong();
//		long currentTime=Long.valueOf(snapShot.getTime());
		 //股票状态 9:30之后判断
		 //9点之前  系统初始化停牌的股票列表  
		 //9:15 到9:30   如果买一 卖一不为0    如果这只股票在停牌的列表里  则修正为正常 
		 //9:30 －15:00    规则有两个  
        // 1: 如果买一 卖一不为0    如果这只股票在停牌的列表里  则修正为正常
        // 2: 如果买一买一都为0  且不在停牌列表里  则判断为 停盘
		/*if(auctionStartTime<currentTime && currentTime<am ){
			if(sellp[0]!=0 && buyp[0]!=0){
				if(StockStatusCache.isStopStock(compositID)){
					snapShot .setStockStatus(StockStatusType.NORMAL.value());
				 }
			}
		}else if(am<currentTime && currentTime<close ){
			if(sellp[0]!=0 && buyp[0]!=0){
				 if(StockStatusCache.isStopStock(compositID)){
					 snapShot .setStockStatus(StockStatusType.NORMAL.value());
				 }
				 
			}else if(sellp[0]==0 && buyp[0]==0){
				 if(!StockStatusCache.isStopStock(compositID)){
					 snapShot.setStockStatus(StockStatusType.SUSPENDED.value());
				 }
			}*/
		//熔断
        if(StringUtils.isNotBlank(blown)){
        	if(blown.toLowerCase().startsWith("p")){
        		snapShot.setStockStatus(StockStatusType.SUSPENDED.value());
        	}else if(blown.toLowerCase().startsWith("m")){
        		snapShot.setStockStatus(StockStatusType.PBLOWN.value());	
        	}else if(blown.toLowerCase().startsWith("n")){
        		snapShot.setStockStatus(StockStatusType.NBLOWN.value());	
        	}else{
        		 snapShot.setStockStatus(StockStatusType.NORMAL.value());	
        	}
        }else{
        	 snapShot.setStockStatus(StockStatusType.NORMAL.value());
        }
		return snapShot;
	}


	@Override
	public String getReadName() {
		return dbfReader.getName();
	}

}
