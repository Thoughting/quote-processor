package com.baidu.stock.process.handler;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.material.domain.IndexComponent;
import com.baidu.stock.material.domain.StockExchangeRate;
import com.baidu.stock.material.domain.StockFinance;
import com.baidu.stock.material.domain.StockStopInfo;
import com.baidu.stock.material.facade.MaterialManager;
import com.baidu.stock.material.inform.InformListener;
import com.baidu.stock.process.cache.AssistDataCache;
import com.baidu.stock.process.cache.FinacialInfoCache;
import com.baidu.stock.process.cache.IndexBagCache;
import com.baidu.stock.process.cache.StockStatusCache;
import com.baidu.stock.process.util.CommonHelp;
import com.baidu.stock.process.util.HQConstant;

/**
 * 基础信息通知
 * @author dengjianli
 *
 */
public class MaterialDataListener extends InformListener{
	private static Logger logger = LoggerFactory.getLogger(MaterialDataListener.class);  
	@Override
	protected void doConcept(long updateTime) {
		
	}

	@Override
	protected void doDelistStock(long updateTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doFinance(long updateTime) {
		MaterialManager materialManager=new MaterialManager(CommonHelp.materialManagerUrl);
		 //初始化财务信息 key:市场_股票编码 [sh_600000]
		 Map<String,StockFinance> financesMap=materialManager.getStockFinances(updateTime);
		 if(null!=financesMap && financesMap.size()>0){
			 for(String key:financesMap.keySet()){
			 StockFinance stockFinance=financesMap.get(key);
			 FinacialInfoCache.add(key, stockFinance);
			 }
			 logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"通知-初始化财务信息"));
		 }else{
			 logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"通知-初始化财务信息 为空"));
		 }
		 
	}

	@Override
	protected void doForeign(long updateTime) {
		MaterialManager materialManager=new MaterialManager(CommonHelp.materialManagerUrl);
		 //汇率
		 StockExchangeRate stockExchangeRate=materialManager.getForeignExchange(updateTime);
		 if(stockExchangeRate!=null){
		 AssistDataCache.add( HQConstant.FOREIGNEXCHANGE_KEY, "USD", stockExchangeRate.getUsd());
		 AssistDataCache.add( HQConstant.FOREIGNEXCHANGE_KEY, "HKDOLLAR", stockExchangeRate.getHkd());
		 logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"通知-初始化汇率"));
		 }else{
		 logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"通知-初始化汇率为空"));
		 }
	}

	@Override
	protected void doHoliday(long updateTime) {
		MaterialManager materialManager=new MaterialManager(CommonHelp.materialManagerUrl);
		//初始化节假日信息
		Set<String>holidays=materialManager.getHolidays(updateTime);
		if(holidays!=null && holidays.size()>0){
		CommonHelp.holidayset.clear();
		CommonHelp.holidayset.addAll(holidays);
		 logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"通知-初始化节假日信息"));
		}else{
		logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"通知-初始化节假日信息为空"));
		}
	}

	@Override
	protected void doIndexComonent(long updateTime) {
		MaterialManager materialManager=new MaterialManager(CommonHelp.materialManagerUrl);
		 Map<String,IndexComponent> indexBag=materialManager.getIndexComponents(updateTime);
		 if(indexBag!=null && indexBag.size()>0){
		 for(String key:indexBag.keySet()){
			 IndexComponent indexComponent=indexBag.get(key);
			 IndexBagCache.add(key, indexComponent);
		 }
		 logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"通知-初始化指数的成份股列表"));
		 }else{
		 logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"通知-初始化指数的成份股列表为空"));
		 }
	}

	@Override
	protected void doIndustry(long updateTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doIpoStock(long updateTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doMafiveVolume(long updateTime) {
		MaterialManager materialManager=new MaterialManager(CommonHelp.materialManagerUrl);
		 //5日均价
		 Map<String, Long> fiveVolumeMap=materialManager.getStockMaFiveVolume(updateTime);
		 if(null!=fiveVolumeMap && fiveVolumeMap.size()>0){
			 for(String key:fiveVolumeMap.keySet()){
				 Long value=fiveVolumeMap.get(key);
				 AssistDataCache.add(key,  HQConstant.DAY5_AVERAGE_VOLUME_KEY, value.doubleValue());
			 }
			 logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"通知-初始化5日均价"));
		 }else{
			 logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"通知-初始化5日均价 为空"));
		 }
		
	}

	@Override
	protected void doSecondStock(long updateTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doStopStock(long updateTime) {
		MaterialManager materialManager=new MaterialManager(CommonHelp.materialManagerUrl);
		 List<StockStopInfo>lstStopStock=materialManager.getStockStopInfos(updateTime);
		 if(lstStopStock!=null && lstStopStock.size()>0){
		 for(StockStopInfo ssi:lstStopStock){
			 String key=CommonHelp.compositID(ssi.getExchange(), ssi.getStockCode());
			 StockStatusCache.add(key, ssi); 
		 }
		 logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"通知-初始化停牌股票列表"));
		 }else{
		 logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"通知-初始化停牌股票列表为空"));
		 }
	}

	@Override
	protected void doTradeday(long updateTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doHgt(long updateTime) {
		// TODO Auto-generated method stub
		
	}

}
