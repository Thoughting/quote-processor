package com.baidu.stock.process.fetch.reader.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baidu.stock.common.AssetType;
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

/**
 * 深交所dbf 文件读取解析处理
 * @author dengjianli
 *
 */
public class SZProcessor extends AbstractHQReader {
	private static Logger logger = LoggerFactory.getLogger(SZProcessor.class);
	private DBFFileReader dbfReader;
	
	public SZProcessor(String hqURL){
		super(hqURL);
		dbfReader = new DBFFileReader("sz",hqURL);
	}

	@Override
	public List<HQSnapShot> process() {
		return parseDBFData();
	}

	/**
	 * 预抓取行情源,判断是否开盘新数据
	 */
	@Override
	public List<HQSnapShot> preFetchHQ() {
		List<HQSnapShot> changeHQList =null;
		try{
			boolean initDbfFlag = dbfReader.init();
			if(!initDbfFlag){
				return null;
			}
			  changeHQList = this.parseDBFData();
		}
		catch(Exception ex){
	         logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"初始化sz行情dbf文件失败/n" + FileUtil.printException(ex)));
		}
		return changeHQList;
	}

	/**
	 * 处理快照解析
	 * @return
	 */
	private  List<HQSnapShot> parseDBFData(){
		List<HQSnapShot> changeHQList = new ArrayList<HQSnapShot>(3000);
		Calendar c= Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		String curSystemDate = yyyyMMddFormat.format(c.getTime());
		//用于判断文件是否更新标志
		long lastUnModifyTimeStamp=-1;
		try{
			List<Map<String,String>>lstHQstr = dbfReader.read();
			for(int i = 0; i <lstHQstr.size();i++){
				Map<String,String> hqMap = lstHQstr.get(i);
				String zeroCode = "000000";
				//判断是否头行信息
				if(hqMap.get("HQZQDM").equals(zeroCode)){
					String hqcjsl = hqMap.get("HQCJSL").toString();
					String bs = "0";
					//行情日期
					dbfDate = hqMap.get("HQZQJC");
					String currentdbfTime = hqMap.get("HQCJBS");
					//判断日期是否正确或是否开盘
					if(!dbfDate.equals(curSystemDate)||(!hqcjsl.equals(bs))){
						logger.warn(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_WARN_KEY, LogConstant.DEFAULT_WARN_VALUE,"sz 当前时间:"+currentdbfTime+" 闭市标志:"+hqcjsl+"当前 处于闭市"));
						return null;
					}
					//时间hhmmss，默认时间是6位,简单的时间处理
					if(currentdbfTime.length() ==5){
						currentdbfTime = "0" + currentdbfTime;
					}
					//时间判断是否合理
					if(currentdbfTime.equals(dbfTime)){
						lastUnModifyTimeStamp=System.currentTimeMillis();
						logger.warn(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_WARN_KEY, LogConstant.DEFAULT_WARN_VALUE,"sz 当前时间:"+currentdbfTime+" 上次更新时间: "+dbfTime+" 发现文件没有变化"));
						return null;
					}
					dbfTime = currentdbfTime;
					HQGloableConfig.lastSZHQTime=currentdbfTime;
				}else{
					HQSnapShot snapShot= readSSHQValue(hqMap);
					if(null != snapShot){
						HQSnapShot newSnapshot=snapShot.clone();
						newSnapshot.setStartTimeStamp(System.currentTimeMillis());
      				    changeHQList.add(newSnapshot);
					}
				}
			}
		}
		catch(Exception ex){
		   logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"sz parseDBFData error:"+ FileUtil.printException(ex)));
		}finally{
		   checkDataRefresh(lastUnModifyTimeStamp);
		}
		return changeHQList;
	}
	
	
	/**
	 * 检查文件是否更新
	 */
	public void checkRefresh(){
		long lastUnModifyTimeStamp=-1;
		try{
			List<Map<String,String>>lstHQstr = dbfReader.read();
				Map<String,String> m = lstHQstr.get(0);
				String zeroCode = "000000";
				if(m.get("HQZQDM").equals(zeroCode)){
					//行情时间
					String time = m.get("HQCJBS");
					if(time.length() ==5){
						time = "0" + time;
					}
					//修改
					if(time.equals(dbfTime)){
						lastUnModifyTimeStamp=System.currentTimeMillis();
					}
					dbfTime = time;
				}else{
					logger.error("读取的第一行不是头信息异常.");
				}
		}
		catch(Exception ex){
		   logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"sz parseDBFData error:"+ FileUtil.printException(ex)));
		}finally{
			checkDataRefresh(lastUnModifyTimeStamp);
		}
	}
	
	
	
	@Override
	public HQSnapShot readSSHQValue(Map<String, String> m) {
		HQSnapShot snapShot = null;
		boolean isChange = false;
		String stockCode = m.get("HQZQDM");
		String stockName=m.get("HQZQJC");
		//过滤股票
		if(CommonHelp.isFilter(HQConstant.szExchangeId, stockCode)){
			return null;
		}
		String compositID=CommonHelp.compositID( HQConstant.szExchangeId ,stockCode);
		snapShot=HQSnapShotCache.querySnapShot(compositID);
		if(snapShot == null){
			snapShot=new HQSnapShot();
			snapShot.setExchange(HQConstant.szExchangeId);
			snapShot.setStockCode(stockCode);
			snapShot.setStockName(stockName);
			snapShot.setCompositID(compositID);
			setAsset(snapShot);
			HQSnapShotCache.add(compositID, snapShot);
			isChange = true;
		}
		//判断是否有变化
		float nowprice=StringUtils.isNotBlank(m.get("HQZJCJ"))==true?Float.parseFloat(m.get("HQZJCJ").replaceAll( "\\s", "")):0;
		long volume=StringUtils.isNotBlank(m.get("HQCJSL"))==true?Long.parseLong(m.get("HQCJSL").replaceAll( "\\s", "")):0L;
		long[] buyv = new long[5];
		long[] sellv = new long[5];
		float[] buyp = new float[5];
		float[] sellp = new float[5];
			sellp[4]=StringUtils.isNotBlank(m.get("HQSJW5"))==true?Float.parseFloat(m.get("HQSJW5").replaceAll( "\\s", "")):0;
			sellv[4]=StringUtils.isNotBlank(m.get("HQSSL5"))==true?Integer.parseInt(m.get("HQSSL5").replaceAll( "\\s", "")):0;
			sellp[3]=StringUtils.isNotBlank(m.get("HQSJW4"))==true?Float.parseFloat(m.get("HQSJW4").replaceAll( "\\s", "")):0;
			sellv[3]=StringUtils.isNotBlank(m.get("HQSSL4"))==true?Integer.parseInt(m.get("HQSSL4").replaceAll( "\\s", "")):0;
			sellp[2]=StringUtils.isNotBlank(m.get("HQSJW3"))==true?Float.parseFloat(m.get("HQSJW3").replaceAll( "\\s", "")):0;
			sellv[2]=StringUtils.isNotBlank(m.get("HQSSL3"))==true?Integer.parseInt(m.get("HQSSL3").replaceAll( "\\s", "")):0;
			sellp[1]=StringUtils.isNotBlank(m.get("HQSJW2"))==true?Float.parseFloat(m.get("HQSJW2").replaceAll( "\\s", "")):0;
			sellv[1]=StringUtils.isNotBlank(m.get("HQSSL2"))==true?Integer.parseInt(m.get("HQSSL2").replaceAll( "\\s", "")):0;
			sellp[0]=StringUtils.isNotBlank(m.get("HQSJW1"))==true?Float.parseFloat(m.get("HQSJW1").replaceAll( "\\s", "")):0;
			sellv[0]=StringUtils.isNotBlank(m.get("HQSSL1"))==true?Integer.parseInt(m.get("HQSSL1").replaceAll( "\\s", "")):0;
			buyp[0] =StringUtils.isNotBlank(m.get("HQBJW1"))==true?Float.parseFloat(m.get("HQBJW1").replaceAll( "\\s", "")):0;
			buyv[0] =StringUtils.isNotBlank(m.get("HQBSL1"))==true?Integer.parseInt(m.get("HQBSL1").replaceAll( "\\s", "")):0;
			buyp[1]=StringUtils.isNotBlank(m.get("HQBJW2"))==true?Float.parseFloat(m.get("HQBJW2").replaceAll( "\\s", "")):0;
			buyv[1]=StringUtils.isNotBlank(m.get("HQBSL2"))==true?Integer.parseInt(m.get("HQBSL2").replaceAll( "\\s", "")):0;
			buyp[2]=StringUtils.isNotBlank(m.get("HQBJW3"))==true?Float.parseFloat(m.get("HQBJW3").replaceAll( "\\s", "")):0;
			buyv[2]=StringUtils.isNotBlank(m.get("HQBSL3"))==true?Integer.parseInt(m.get("HQBSL3").replaceAll( "\\s", "")):0;
			buyp[3]=StringUtils.isNotBlank(m.get("HQBJW4"))==true?Float.parseFloat(m.get("HQBJW4").replaceAll( "\\s", "")):0;
			buyv[3]=StringUtils.isNotBlank(m.get("HQBSL4"))==true?Integer.parseInt(m.get("HQBSL4").replaceAll( "\\s", "")):0;
			buyp[4]=StringUtils.isNotBlank(m.get("HQBJW5"))==true?Float.parseFloat(m.get("HQBJW5").replaceAll( "\\s", "")):0;
			buyv[4]=StringUtils.isNotBlank(m.get("HQBSL5"))==true?Integer.parseInt(m.get("HQBSL5").replaceAll( "\\s", "")):0;

		if(StringUtils.isNotBlank(dbfTime) && !dbfTime.equals(snapShot.getTime())){
			if(null==snapShot.getTime()){
				isChange = true;
			}
			//日期发生变化
			else if(snapShot.getClose() != nowprice){
				//价格变化
				isChange = true;
			}
			else if(snapShot.getVolume() < volume){
				//量变化
				isChange = true;
			}
			else if(isWDChange(buyp, buyv, sellp, sellv,snapShot)){
				//五档变化
				isChange = true;
			}else if (StringUtils.isNotEmpty(dbfTime) && StringUtils.isNotEmpty(snapShot.getTime()) && isSendFullHqTime(Integer.parseInt(dbfTime))
						&& CommonHelp.getMilliSecondDiff(dbfDate + dbfTime, dbfDate + snapShot.getTime()) >= HQGloableConfig.applicationSettings.getBaseConfig().getSendFullTimeMillis()){
				//时间变化
				isChange = true;
			}
		}

		if(!isChange){
           // logger.warn(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_WARN_KEY, LogConstant.DEFAULT_WARN_VALUE,"当前行情["+compositID+"]没有变化"));
			return null;
		}
		snapShot.setDate(dbfDate);
		snapShot.setTime(dbfTime);
		float preClose=StringUtils.isNotBlank(m.get("HQZRSP").replaceAll( "\\s", ""))==true?Float.parseFloat(m.get("HQZRSP").replaceAll( "\\s", "")):0;
		snapShot.setOpen(StringUtils.isNotBlank(m.get("HQJRKP").replaceAll( "\\s", ""))==true?Float.parseFloat(m.get("HQJRKP").replaceAll( "\\s", "" )):0);
		snapShot.setAmount(StringUtils.isNotBlank(m.get("HQCJJE").replaceAll( "\\s", ""))==true?Float.parseFloat(m.get("HQCJJE").replaceAll( "\\s", "")):0);
		snapShot.setHigh(StringUtils.isNotBlank(m.get("HQZGCJ").replaceAll( "\\s", ""))==true?Float.parseFloat(m.get("HQZGCJ").replaceAll( "\\s", "")):0);
		snapShot.setLow(StringUtils.isNotBlank(m.get("HQZDCJ").replaceAll( "\\s", ""))==true?Float.parseFloat(m.get("HQZDCJ").replaceAll( "\\s", "")):0);
			
		if(m.get("HQZDCJ").equals(HQConstant.SZMINVALUE)){
				snapShot.setLow(0);
			}
			snapShot.setClose(nowprice);
			snapShot.setPreClose(preClose);
			snapShot.setDealCount(StringUtils.isNotBlank(m.get("HQCJBS"))==true?Integer.parseInt(m.get("HQCJBS").replaceAll( "\\s", "")):0);
			snapShot.setNowVolume((volume - snapShot.getVolume()));
			snapShot.setVolume(volume);
			long auctionStartTime=HQGloableConfig.applicationSettings.getMarketTime().getAuctionStartTimeForLong();
			long am=HQGloableConfig.applicationSettings.getMarketTime().getAmTime();
			long closedAuctionTime=HQGloableConfig.applicationSettings.getMarketTime().getClosedAuctionTimeForLong();
//			long close=HQGloableConfig.applicationSettings.getMarketTime().getClosedTimeForLong();
			long currentTime=Long.valueOf(snapShot.getTime());
		   if(currentTime<am &&currentTime >=auctionStartTime
				&& snapShot.getVolume() > 0 && snapShot.getOpen() == 0 && snapShot.getClose() > 0){
			logger.info("hq data: compositID = " + snapShot.getCompositID()
					   + " time = " + snapShot.getDate()+ snapShot.getTime() + " preClose = " + snapShot.getClose()
					   + " high = " + snapShot.getHigh() + " open = "+ snapShot.getOpen() + " low = " + snapShot.getLow()
					   + " close = " + snapShot.getClose()+ " volume = " + snapShot.getVolume() + " nowVol = " + snapShot.getNowVolume()
					   + " amount = " + snapShot.getAmount() +"  >>>>> set new open = "+snapShot.getClose());
			snapShot.setOpen(snapShot.getClose());
		}
		//买卖5档
		snapShot.setSellp(sellp);
		snapShot.setSellv(sellv);
		
		snapShot.setBuyp(buyp);
		snapShot.setBuyv(buyv);
		
		 //股票状态 9:30之后判断
		 //9点之前  系统初始化停牌的股票列表  
		 //9:15 到9:30   如果买一 卖一不为0    如果这只股票在停牌的列表里  则修正为正常 
		 //9:30 －15:00    规则有两个  
         // 1: 如果买一 卖一不为0    如果这只股票在停牌的列表里  则修正为正常
         // 2: 如果买一买一都为0  且不在停牌列表里  则判断为 停盘
		 if (snapShot.getAsset()!= AssetType.INDEX.value()) {
		if(auctionStartTime<=currentTime && currentTime<am ){
				if(StockStatusCache.isStopStock(compositID)){
					if(sellp[0]!=0 || buyp[0]!=0){
					snapShot.setStockStatus(StockStatusType.NORMAL.value());
					}else{
					snapShot.setStockStatus(StockStatusType.SUSPENDED.value());
				 }
			}else{
					snapShot.setStockStatus(StockStatusType.NORMAL.value());	 
			}
		}else if(am<=currentTime){
			if(sellp[0]!=0 || buyp[0]!=0){ 
				 snapShot.setStockStatus(StockStatusType.NORMAL.value()); 
			}else {
			    if(am>=closedAuctionTime && StockStatusCache.isStopStock(compositID)){
			        snapShot.setStockStatus(StockStatusType.SUSPENDED.value()); 
			    }else {
			        snapShot.setStockStatus(StockStatusType.NORMAL.value()); 
                }
			}
		}
	}
		return snapShot;
	}

	@Override
	public String getReadName() {
		return dbfReader.getName();
	}
	
}
