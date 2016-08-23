package com.baidu.stock.process.fetch.reader;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.process.config.HQGloableConfig;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.process.util.CommonHelp;

/**
 * 抽象数据源处理接口
 * @author dengjianli
 *
 */
public abstract class AbstractHQReader implements HQReader{
	public static Logger logger = LoggerFactory.getLogger(AbstractHQReader.class);
	protected static SimpleDateFormat yyyyMMddFormat = new SimpleDateFormat("yyyyMMdd");
	protected static SimpleDateFormat HHmmssFormat = new SimpleDateFormat("HHmmss");
	//dbf文件的日期时间hhmmss
	protected String dbfTime = "";
	//dbf文件的日期时间yyyymmdd
	protected String dbfDate = "";
	//记录上一次的成功处理时间戳
	private long lastDataNotChangeTime=0;
	//判断文件是否持续更新
	protected boolean fileDataIsRefresh=true;
    private String hqURL;
    
	public AbstractHQReader(String hqURL){
		this.hqURL=hqURL;
	}
	
	public String getHQurl(){
		return this.hqURL;
	}

/**
 * 判断文件是否有刷新
 * @param currenUnModifyTimeStamp
 */
	protected synchronized void checkDataRefresh(long currenUnModifyTimeStamp) {
		if(currenUnModifyTimeStamp!=-1){
			long amTime=HQGloableConfig.applicationSettings.getMarketTime().getAmTime();
			long launch=HQGloableConfig.applicationSettings.getMarketTime().getLunchbreakTimeForLong();
			long pmTime=HQGloableConfig.applicationSettings.getMarketTime().getPmTime();
			long closeMarket=HQGloableConfig.applicationSettings.getMarketTime().getClosedTimeForLong();
			long dataExceptionTimeSecond=HQGloableConfig.applicationSettings.getBaseConfig().getDataExceptionTimeSecond();
			if(lastDataNotChangeTime>0){
				long intervalSeconds=(currenUnModifyTimeStamp-lastDataNotChangeTime)/1000;
				//超过n秒文件没有更新
				if(intervalSeconds>dataExceptionTimeSecond){
					Long curNotChangeTime=Long.valueOf(HHmmssFormat.format(new Date(currenUnModifyTimeStamp)));
					//格式:HHmmss   curNotChangeTime>113030 && curNotChangeTime< 112945
					if(curNotChangeTime >amTime+30 && curNotChangeTime< launch-55)
					{
						fileDataIsRefresh = false;
						logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.QUOTE_FILE_ERROR_KEY, LogConstant.QUOTE_FILE_ERROR_VALUE,getReadName()+"行情不更新超过"+dataExceptionTimeSecond+"秒,currentTime="+curNotChangeTime+",dataChangeTime=="+dbfTime+" 行情文件路径:"+hqURL));
					}
					else if(curNotChangeTime >pmTime+30 && curNotChangeTime <closeMarket)
					{
						fileDataIsRefresh = false;
						logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.QUOTE_FILE_ERROR_KEY, LogConstant.QUOTE_FILE_ERROR_VALUE,getReadName()+"行情不更新超过"+dataExceptionTimeSecond+"秒,currentTime="+curNotChangeTime+",dataChangeTime=="+dbfTime+" 行情文件路径:"+hqURL));
					}else {
						fileDataIsRefresh = true;
                    }
					lastDataNotChangeTime=currenUnModifyTimeStamp;
				}
			}else{
				lastDataNotChangeTime=currenUnModifyTimeStamp;
			}
		}else{
			lastDataNotChangeTime=0;
			fileDataIsRefresh = true;
		}
	}


	public boolean isFileDataIsRefresh() {
		return fileDataIsRefresh;
	}


	protected boolean isWDChange(float[] buyp,long[] buyv,float[] sellp,long[] sellv,HQSnapShot snapShot){
		for(int i = 0; i < buyp.length;i++)
		{
			if(buyp[i] != snapShot.buyp[i])
			{
				return true;
			}
		}
		
		for(int i = 0; i < buyv.length;i++)
		{
			if(buyv[i] != snapShot.buyv[i])
			{
				return true;
			}
		}
		
		for(int i = 0; i < sellp.length;i++)
		{
			if(sellp[i] != snapShot.sellp[i])
			{
				return true;
			}
		}
		
		for(int i = 0; i < sellv.length;i++)
		{
			if(sellv[i] != snapShot.sellv[i])
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 下发行情的时间区间9:00- 15:04
	 * @param dbfTime
	 * @return
	 */
	protected  boolean isSendFullHqTime(int dbfTime){
		boolean flag = false;
		// 从系统初始化后开始计算
		if ((dbfTime >= HQGloableConfig.applicationSettings.getMarketTime().getExchageInitTimeForLong()
				&& dbfTime <=HQGloableConfig.applicationSettings.getMarketTime().getLunchbreakTimeForLong())
			|| (dbfTime >=HQGloableConfig.applicationSettings.getMarketTime().getPmTime() 
			&& dbfTime <=HQGloableConfig.applicationSettings.getMarketTime().getStopSendFullHQTimeForLong())) {
			flag = true;
		}
		return flag;
	}
	
	
	/**
	 * 证券类型定义 0:A股票 1：期货，2：期权 3：外汇，4指数，5：场内基金，6：债券、7：认购权证，8：认沽权证，9： 牛证，10：熊证， 11：其他，12：表示场外，13：表示货币，14：表示B股，15：回购，16：场内的货币基金，17：港股基金
	 * @param stockBasic
	 * @return
	 */
	protected HQSnapShot setAsset(HQSnapShot snapShot){
	int asset=CommonHelp.requireTypes(snapShot.getExchange(),snapShot.getStockCode());
	snapShot.setAsset(asset);
	return snapShot;
	}
	
	public abstract String getReadName();
}
