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
import com.baidu.stock.process.config.HQGloableConfig;
import com.baidu.stock.process.fetch.io.BigFileReader;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.process.fetch.reader.AbstractHQReader;
import com.baidu.stock.process.fetch.reader.field.FastHQMessage;
import com.baidu.stock.process.fetch.reader.field.MDStreamType;
import com.baidu.stock.process.util.CommonHelp;
import com.baidu.stock.process.util.FileUtil;
import com.baidu.stock.process.util.HQConstant;

/**
 * sh fast格式数据源解析
 * @author dengjianli
 *
 */
public class SHFastProcessor extends AbstractHQReader{
	private static Logger logger = LoggerFactory.getLogger(SHFastProcessor.class);
//	private SimpleFileReader txtReader;
	private BigFileReader txtReader;
	private String exchangeState = "";
	
	public SHFastProcessor(String hqURL){
		super(hqURL);
//		simpleFileReader = new SimpleFileReader(fasturl);
		//多线程读取文件的线程数2
		txtReader=new BigFileReader("sh",hqURL,"gbk",1024*1024);
	}

	@Override
	public List<HQSnapShot> process() {
		return parseFastData();
	}

	@Override
	public List<HQSnapShot> preFetchHQ() {
		List<HQSnapShot> changeHQList=null;
		try{
			changeHQList = parseFastData();
		}
		catch(Exception ex){
			logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"初始化sh fast行情文件失败\n"+FileUtil.printException(ex)));
		}
		return changeHQList;
	}

	/**
	 * 处理快照解析
	 * @return
	 */
	 private synchronized List<HQSnapShot> parseFastData() {
	    	List<HQSnapShot> changeHQList = new ArrayList<HQSnapShot>(3000);
			Calendar c= Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
			String curSystemDate = yyyyMMddFormat.format(c.getTime());
			//用于判断文件是否更新标志
			long lastUnModifyTimeStamp=-1;
	    	try{
	    		FastHQMessage hqMessage=txtReader.readByMappedByteBuffer();
	    		exchangeState =hqMessage.getExchangeState();
				dbfDate = hqMessage.getDbfDate().substring(0, 8);
				String currentDBFTime=hqMessage.getDbfDate().substring(9, 17).replaceAll(":", "");
				//判断日期是否正确或是否开盘
				if(!dbfDate.equals(curSystemDate)
						||(StringUtils.isNotEmpty(exchangeState)&& exchangeState.equals("E11"))
						|| (exchangeState.length() > 3 && exchangeState.substring(3, 4).equals("1"))){
					
					logger.warn(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_WARN_KEY, LogConstant.DEFAULT_WARN_VALUE,"sh 当前时间:"+currentDBFTime+" 闭市标志:"+exchangeState+"当前 处于闭市"));
				//这里不return,因为sh有闭市收盘价,需要继续下发根据闭市状态来赋值最终的收盘价
				}
				//时间判断是否合理
				if(currentDBFTime.equals(dbfTime)){
					lastUnModifyTimeStamp=System.currentTimeMillis();
					logger.warn(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_WARN_KEY, LogConstant.DEFAULT_WARN_VALUE,"sh 当前时间:"+currentDBFTime+" 上次更新时间: "+dbfTime+" 发现文件没有变化"));
					return null;
				}
				dbfTime =currentDBFTime;
				HQGloableConfig.lastSHHQtime=currentDBFTime;
				List<Map<String,String>> lstHQ = hqMessage.getLstHQ();
				if(null!=lstHQ && lstHQ.size() > 0){
			    	for(int i = 0; i < lstHQ.size();i++){//readByMappedByteBuffer方式时候,i=0开始;readByBuffer方法时候i=1
			    		Map<String,String> m = lstHQ.get(i);
			    		HQSnapShot snapShot = readSSHQValue(m);
					     if(snapShot != null){
					    	 HQSnapShot newSnapshot=snapShot.clone();
					    	 newSnapshot.setStartTimeStamp(System.currentTimeMillis());
							changeHQList.add(newSnapshot);	
					    }
			    	}
				}
			}catch(Exception ex){
			logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"sH parseDBFData error:"+ FileUtil.printException(ex)));
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
				FastHQMessage hqMessage=txtReader.readByMappedByteBuffer();
				List<Map<String,String>> lstHQstr = hqMessage.getLstHQ();
				if(null!=lstHQstr && lstHQstr.size() > 0){
					String currentDBFTime=hqMessage.getDbfDate().substring(9, 17).replaceAll(":", "");
					//修改
					if(currentDBFTime.equals(dbfTime)){
						lastUnModifyTimeStamp=System.currentTimeMillis();
					}
					dbfTime =currentDBFTime;
				}
			}
			catch(Exception ex){
			   logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"sh(fast) parseDBFData error:"+ FileUtil.printException(ex)));
			}finally{
				checkDataRefresh(lastUnModifyTimeStamp);
			}
		}
	 
	@Override
	public HQSnapShot readSSHQValue(Map<String, String> m) {
		if(StringUtils.isNotBlank(m.get("f0"))){
			if(m.get("f0").equals(MDStreamType.TRAILER.toString())){
				return null;
			}
		}
		HQSnapShot snapShot = null;
		//判断是否有变化
		boolean isChange = false;
		float nowprice = 0;
		float preClose=0;
		long volume = 0;
		String stockCode =m.get("f1");
		String stockName=m.get("f2");
		//熔断
		String blown=null;
		String md=m.get("f0");
		if("MD004".equalsIgnoreCase(md)){
			blown=m.get("f33");
		}else if("MD002".equalsIgnoreCase(md) || "MD003".equalsIgnoreCase(md)){
			blown=m.get("f31");
		}
		//过滤股票
		if(CommonHelp.isFilter(HQConstant.shExchangeId, stockCode)){
			return null;
		}
		String compositID=CommonHelp.compositID( HQConstant.shExchangeId ,stockCode);
		snapShot=HQSnapShotCache.querySnapShot(compositID);
		if(snapShot == null){
			snapShot=new HQSnapShot();
			snapShot.setExchange(HQConstant.shExchangeId);
			snapShot.setStockCode(stockCode);
			snapShot.setStockName(stockName);
			snapShot.setCompositID(compositID);
			setAsset(snapShot);
			HQSnapShotCache.add(compositID, snapShot);
		}
		try{
			// 最新价
			nowprice = Float.parseFloat(m.get("f9"));
			// 闭式后读取今收盘价
			if((exchangeState.equals("E11") || (exchangeState.length() > 3 && exchangeState.substring(3, 4).equals("1")))
					&& Float.parseFloat(m.get("f10")) != 0)
			{
				nowprice = Float.parseFloat(m.get("f10"));
			}
		}
		catch(Exception ex){}
		try{
			if( AssetType.INDEX.value()==snapShot.getAsset()){
			volume = Long.parseLong(m.get("f3"))*100;
			}else{
			volume = Long.parseLong(m.get("f3"));
			}
		}
		catch(Exception ex){}
		long[] buyv = new long[5];
		long[] sellv = new long[5];
		float[] buyp = new float[5];
		float[] sellp = new float[5];
	//以下暂时不优化,数据文件中的内容不都是合法的数据,比如:--,所以解析比较特殊,因此暂时这种粗暴方式
		try
		{
			sellp[4] = Float.parseFloat(m.get("f29"));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			sellv[4] = Integer.parseInt(m.get("f30"));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			sellp[3] = Float.parseFloat(m.get("f25"));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			sellv[3] = Integer.parseInt(m.get("f26"));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			sellp[2] = Float.parseFloat(m.get("f21"));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			sellv[2] = Integer.parseInt(m.get("f22"));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			sellp[1] = Float.parseFloat(m.get("f17"));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			sellv[1] = Integer.parseInt(m.get("f18"));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			sellp[0] = Float.parseFloat(m.get("f13"));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			sellv[0] = Integer.parseInt(m.get("f14"));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			buyp[0] = Float.parseFloat(m.get("f11"));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			buyv[0] = Integer.parseInt(m.get("f12"));
		}
		catch (Exception e)
		{
		}
	
		try
		{
			buyp[1] = Float.parseFloat(m.get("f15"));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			buyv[1] = Integer.parseInt(m.get("f16"));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			buyp[2] = Float.parseFloat(m.get("f19"));
		}
		catch (Exception e)
		{
		}
	
		try
		{
			buyv[2] = Integer.parseInt(m.get("f20"));
		}
		catch (Exception e)
		{
		}
	
		try
		{
			buyp[3] = Float.parseFloat(m.get("f23"));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			buyv[3] = Integer.parseInt(m.get("f24"));
		}
		catch (Exception e)
		{
		}
	
		try
		{
			buyp[4] = Float.parseFloat(m.get("f27"));
		}
		catch (Exception e)
		{
		}
	
		try
		{
			buyv[4] = Integer.parseInt(m.get("f28"));
		}
		catch (Exception e)
		{
		}
		
		if(StringUtils.isNotBlank(dbfTime) && !dbfTime.equals(snapShot.getTime())){
			if(null==snapShot.getTime()){
				isChange = true;
			}
			//日期发生变化
			else if(snapShot.getClose() != nowprice){
				isChange = true;
			}else if(snapShot.getVolume() < volume){
				//量变化
				isChange = true;
			}else if(!m.get("f0").toString().equals(MDStreamType.HEADER.toString())&& isWDChange(buyp, buyv, sellp, sellv,snapShot)){
				//五档变化
				isChange = true;
			}else if (StringUtils.isNotEmpty(dbfTime) && StringUtils.isNotEmpty(snapShot.getTime()) && isSendFullHqTime(Integer.parseInt(dbfTime))
					&& CommonHelp.getMilliSecondDiff(dbfDate + dbfTime, dbfDate + snapShot.getTime()) >=HQGloableConfig.applicationSettings.getBaseConfig().getSendFullTimeMillis())
			{
				//时间变化
				isChange = true;
			}
		}

		//修改
		if(!isChange){
		   // logger.warn(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_WARN_KEY, LogConstant.DEFAULT_WARN_VALUE,"当前行情["+compositID+"]没有变化"));
			return null;
		}
		// 按照MD类型赋值time字段
		snapShot.setDate(dbfDate);
		snapShot.setTime(dbfTime);
		// 获取开高低收价格
		try
		{
			preClose=Float.parseFloat(m.get("f5"));
		}catch(Exception e){}
		
		try
		{
			snapShot.setOpen(Float.parseFloat(m.get("f6")));
		}
		catch (Exception e)
		{
		}

		try
		{
			snapShot.setAmount(Double.parseDouble(m.get("f4")));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			snapShot.setHigh(Float.parseFloat(m.get("f7")));
		}
		catch (Exception e)
		{
		}
		
		try
		{
			snapShot.setLow(Float.parseFloat(m.get("f8")));
		}
		catch (Exception e)
		{
		}
		
		snapShot.setClose(nowprice);
		snapShot.setPreClose(preClose);
		snapShot.setVolume(volume);
		
//		long auctionStartTime=HQGloableConfig.applicationSettings.getMarketTime().getAuctionStartTimeForLong();
//		long am=HQGloableConfig.applicationSettings.getMarketTime().getAmTime();
//		long close=HQGloableConfig.applicationSettings.getMarketTime().getClosedTimeForLong();
//		long currentTime=Long.valueOf(snapShot.getTime());
		if(Integer.parseInt(snapShot.getTime()) <HQGloableConfig.applicationSettings.getMarketTime().getAmTime()
				&& Integer.parseInt(snapShot.getTime()) >=HQGloableConfig.applicationSettings.getMarketTime().getAuctionStartTimeForLong()
				&& snapShot.getVolume() > 0 && snapShot.getOpen() == 0 && snapShot.getClose() > 0){
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
				/*if(auctionStartTime<currentTime && currentTime<am ){
					if(sellp[0]!=0 && buyp[0]!=0){
						if(StockStatusCache.isStopStock(compositID)){
							snapShot.setStockStatus(StockStatusType.NORMAL.value());
						 }
					}
				}else if(am<currentTime && currentTime<close ){
					if(sellp[0]!=0 && buyp[0]!=0){
						 if(StockStatusCache.isStopStock(compositID)){
							 snapShot.setStockStatus(StockStatusType.NORMAL.value());
						 }
						 
					}else if(sellp[0]==0 && buyp[0]==0){
						 if(!StockStatusCache.isStopStock(compositID)){
							 snapShot.setStockStatus(StockStatusType.SUSPENDED.value());
						 }
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
		return txtReader.getName();
	}

}
