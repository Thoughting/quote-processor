package com.baidu.stock.process.wrap.impl;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baidu.stock.common.AssetType;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.process.cache.AskBidDataCache;
import com.baidu.stock.process.cache.BigFundSDataCache;
import com.baidu.stock.process.cache.InOutDataCache;
import com.baidu.stock.process.cache.IndexBagCache;
import com.baidu.stock.process.config.HQGloableConfig;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.process.util.CacheDiskStoreUtil;
import com.baidu.stock.process.util.CommonHelp;
import com.baidu.stock.process.util.FileUtil;
import com.baidu.stock.process.util.HQConstant;
import com.baidu.stock.process.wrap.AbstractSnapshotHandler;

/**
 * 资金流向计算
 * @author dengjianli
 *
 */
public class FundsFlowHandler extends AbstractSnapshotHandler{
	private static Logger logger = LoggerFactory.getLogger(FundsFlowHandler.class);
	private static ThreadPoolExecutor cacheTaskExecutor = new ThreadPoolExecutor(1, 1, 300,TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1),new ThreadPoolExecutor.DiscardOldestPolicy());

    // 大资金阀值: 数量
    private float volumeThreshold = 100000.00f;
    // 大资金阀值: 金额
    private float amountThreshold = 200000.00f;
    
	@Override
	public HQSnapShot handleRequest(HQSnapShot snapShot) {
		HQSnapShot newSnapShot=process(snapShot);
		if(null!=getNextHandler()){
			newSnapShot=getNextHandler().handleRequest(newSnapShot);
		}
		if (isFilterTrace(snapShot)) {
            return null;
		}
		return newSnapShot;
	}
	
    private HQSnapShot process(HQSnapShot snapShot){
        try {
            // 跳过指数类型,更新资金流向缓存: 把当前交易的数量更新并累加进去
            if (snapShot.getNowVolume() > 0 && snapShot.getAsset() != AssetType.INDEX.value()) {
                updateFundsCache(snapShot);
            }
            // 资金流向的度量: 提取从开市到当前的累计数据
            checkFundsChange(snapShot);
            
            // 更新买卖报价缓存
            updateAskBidPrice(snapShot);
            
            //异步缓存长周期缓存
            asynStoreCacheSnapshot();
        } catch (Exception e) {
			 logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY , LogConstant.DEFAULT_ERROR_VALUE ," exchange="+snapShot.getExchange()+" stockCode="+snapShot.getStockCode()+"计算资金流向计算数据异常:\n:" + FileUtil.printException(e)));
        }
		return snapShot;
    }
    
    /**
     * 更新资金流向缓存
     */
    private void updateAskBidPrice(HQSnapShot snapShot) throws Exception {
        float currAsk1Price = 0;
        float currBid1Price = 0;
        
        // 存入当前行情的卖一数据
        if(null!=snapShot.getSellp() && null!=snapShot.getSellv()){
         currAsk1Price=snapShot.getSellp()[0];
         AskBidDataCache.add(snapShot.getCompositID(), HQConstant.ASK_ONE_PRICE_KEY, (double)currAsk1Price);
        }
        // 存入当前行情的买一数据
        if(null!=snapShot.getBuyp() && null!=snapShot.getBuyv()){
         currBid1Price=snapShot.getBuyp()[0];
         AskBidDataCache.add(snapShot.getCompositID(), HQConstant.BID_ONE_PRICE_KEY, (double)currBid1Price);
        }
    }
    
    
    /**
     * 更新资金流向缓存
     */
    private  void updateFundsCache(HQSnapShot snapShot) throws Exception {
        String compositid = snapShot.getCompositID();
        long nowVolume = snapShot.getNowVolume();
        double nowAmount = snapShot.getNowAmount(); 
        float lastAsk1Price = 0;
        float lastBid1Price = 0;
        float currAsk1Price = 0;
        float currBid1Price = 0;
        double bigInflows = 0;
        double bigOutflows = 0;
        double sumInflows = 0;
        double sumOutflows = 0;
        // 提取上笔行情的买卖报价
        lastAsk1Price = (float) (AskBidDataCache.query(compositid,HQConstant.ASK_ONE_PRICE_KEY) * 1.00f);
        lastBid1Price = (float) (AskBidDataCache.query(compositid,HQConstant.BID_ONE_PRICE_KEY) * 1.00f);
        // 存入当前行情的卖一数据
        if(null!=snapShot.getSellp() && null!=snapShot.getSellv()){
         currAsk1Price=snapShot.getSellp()[0];
        }
        // 存入当前行情的买一数据
        if(null!=snapShot.getBuyp() && null!=snapShot.getBuyv()){
         currBid1Price=snapShot.getBuyp()[0];
        }
        // Funds flow -----------------------------------------------------------------------------
        // 大资金判断
        boolean bigDeal = (nowVolume > volumeThreshold) || (nowAmount > amountThreshold);
        // 当前成交价
        float currPrice = snapShot.getClose();
        // 变动方向: 0 - 不明, 1 - 买盘, 2 - 卖盘
        int direction = 0;
        if (lastBid1Price > 0.00001f && currPrice <= lastBid1Price) {
            direction = 2;
        } else if (lastAsk1Price > 0.00001f && currPrice >= lastAsk1Price) {
            direction = 1;
        }
        // 涨停、跌停标识 0 - 未涨停跌停 ,1 - 跌停 , 2 - 涨停
        int toplimit = 0;
        // 涨跌停的特别处理 (跌停是外盘，涨停是内盘)
        if (currBid1Price < 0.00001f && currAsk1Price > 0.00001f) {
            // 跌停： 有买入报价, 全部计入卖盘 && touchFallLimit(event)
            toplimit = 1;
            direction = 2;
        } else if (currAsk1Price < 0.00001f && currBid1Price > 0.00001f) {
            // 涨停： 有卖出报价, 全部计入买盘 && touchRiseLimit(event)
            toplimit = 2;
            direction = 1;
        }
        // 依据买卖盘方向, 更新内部缓存
        if (direction == 2) { // 卖盘
            if (toplimit == 1) {
                // 跌停入外盘
            	InOutDataCache.add(compositid, HQConstant.SUM_OUTER_VOLUME, nowVolume);
            } else {
                // 当前价 <= 前一笔行情的买一价， 价格下行，内盘卖出
            	InOutDataCache.add(compositid, HQConstant.SUM_INNER_VOLUME, nowVolume);
            }
            // 总资金流出
            sumOutflows = nowAmount;
            // 大资金流出
            if (bigDeal) {
                bigOutflows = nowAmount;
            }
        } else if (direction == 1) { // 买盘

            if (toplimit == 2) {
                // 涨停入内盘
            	InOutDataCache.add(compositid, HQConstant.SUM_INNER_VOLUME, nowVolume);
            } else {
                // 当前价 >= 前一笔行情的卖一价， 价格上行，外盘买入
            	InOutDataCache.add(compositid, HQConstant.SUM_OUTER_VOLUME, nowVolume);
            }
            // 总资金流入
            sumInflows = nowAmount;
            // 大资金流入
            if (bigDeal) {
                bigInflows = nowAmount;
            }
        } else { // 方向不明
            // 价格落在中部，内外各一半
        	InOutDataCache.add(compositid, HQConstant.SUM_INNER_VOLUME, nowVolume / 2);
        	InOutDataCache.add(compositid, HQConstant.SUM_OUTER_VOLUME, nowVolume / 2);

            // 价格落在中部，流入流出各一半
            sumInflows = nowAmount / 2;
            sumOutflows = nowAmount / 2;

            // 大资金流入流出各一半
            if (bigDeal) {
                bigInflows = nowAmount / 2;
                bigOutflows = nowAmount / 2;
            }
        }

        BigFundSDataCache.add(compositid, HQConstant.BIG_FUNDS_INFLOWS, bigInflows);
        BigFundSDataCache.add(compositid, HQConstant.BIG_FUNDS_OUTFLOWS, bigOutflows);

        String indexUUID = null;
        // 更新上证指数的资金流向
        if (snapShot.getExchange().equals(HQConstant.shExchangeId) && snapShot.getStockCode().startsWith("6")) {
            indexUUID = CommonHelp.compositID(HQConstant.shExchangeId, HQConstant.SH_MASTER_INDEX);
            updateIndexFunds(indexUUID, bigInflows, bigOutflows, sumInflows, sumOutflows);
        }
        // 更新深证成指的资金流向
        if (snapShot.getExchange().equals(HQConstant.szExchangeId) && snapShot.getStockCode().startsWith("0")) {
            indexUUID =CommonHelp.compositID(HQConstant.szExchangeId, HQConstant.SZ_MASTER_INDEX);
            updateIndexFunds(indexUUID, bigInflows, bigOutflows, sumInflows, sumOutflows);
        }
        // 更新深圳创业板的资金流向
        if (snapShot.getExchange().equals(HQConstant.szExchangeId) && snapShot.getStockCode().startsWith("30")) {
            indexUUID =CommonHelp.compositID(HQConstant.szExchangeId, HQConstant.SZC_INDEX_KEY);
            updateIndexFunds(indexUUID, bigInflows, bigOutflows, sumInflows, sumOutflows);
        }
        // 更新深证-沪深300的资金流向
        indexUUID =CommonHelp.compositID(HQConstant.szExchangeId, HQConstant.SZ_HS300_INDEX_KEY);
        if (snapShot.getExchange().equals(HQConstant.szExchangeId) && (null!=IndexBagCache.query(indexUUID))) {
            updateIndexFunds(indexUUID, bigInflows, bigOutflows, sumInflows, sumOutflows);
        }
        // 更新上证-沪深300的资金流向
        indexUUID = CommonHelp.compositID(HQConstant.shExchangeId, HQConstant.SH_HS300_INDEX_KEY);
        if (snapShot.getExchange().equals(HQConstant.shExchangeId)&& (null!=IndexBagCache.query(indexUUID))) {
            updateIndexFunds(indexUUID, bigInflows, bigOutflows, sumInflows, sumOutflows);
        }
    }
    
    private void updateIndexFunds(String uuid, double bigInflows, double bigOutflows, double sumInflows, double sumOutflows) {
    	BigFundSDataCache.add(uuid, HQConstant.BIG_FUNDS_INFLOWS, bigInflows);
    	BigFundSDataCache.add(uuid, HQConstant.BIG_FUNDS_OUTFLOWS, bigOutflows);
    }
    
    
    /**
     * 资金变化的度量： 资金流向，内外盘, 成交笔数
     */
    private  void checkFundsChange(HQSnapShot snapShot) throws Exception {
    	String key=snapShot.getCompositID();
        Map<String, Double> flowMap = BigFundSDataCache.query(key);
        if (flowMap != null) {
            double bigInflows = 0;
            double bigOutflows = 0;
            if (flowMap.containsKey(HQConstant.BIG_FUNDS_INFLOWS)) {
                bigInflows = flowMap.get(HQConstant.BIG_FUNDS_INFLOWS);
            }
            if (flowMap.containsKey(HQConstant.BIG_FUNDS_OUTFLOWS)) {
                bigOutflows = flowMap.get(HQConstant.BIG_FUNDS_OUTFLOWS);
            }
            //大资金净流入
            snapShot.setBigNetflows((float) (bigInflows - bigOutflows));
        }
        //因为指数没有内外盘
        if (snapShot.getAsset() != AssetType.INDEX.value()) {
        Map<String, Long> inneroutMap = InOutDataCache.query(key);
        long outerVolume = 0;
        long innerVolume = 0;
        if (inneroutMap != null) {
            if (inneroutMap.containsKey(HQConstant.SUM_OUTER_VOLUME)) {
                outerVolume = inneroutMap.get(HQConstant.SUM_OUTER_VOLUME);
                snapShot.setOutSide(Double.valueOf(outerVolume).intValue()); // 外盘
            }
            if (inneroutMap.containsKey(HQConstant.SUM_INNER_VOLUME)) {
                innerVolume = inneroutMap.get(HQConstant.SUM_INNER_VOLUME);
                snapShot.setInSide(Double.valueOf(innerVolume).intValue()); // 内盘
            }
        }
        }
    }
    
	
    /**
     * 异步临时缓存长周期的缓存(包括：大基金和内外盘),默认缓存周期是9:30~15:00
     */
    private void asynStoreCacheSnapshot(){
    	cacheTaskExecutor.execute(new Runnable() {
			@SuppressWarnings("unchecked")
			public void run(){
				List<String>lstKey=BigFundSDataCache.getCache().getKeys();
				Properties pres=new Properties();
				if(null!=lstKey && lstKey.size()>0){
					for(String key:lstKey){
						StringBuffer sb=new StringBuffer();
						Map<String, Long> inouter=InOutDataCache.query(key);
						Map<String, Double> bigFund = BigFundSDataCache.query(key);
						if((null!=inouter && inouter.size()>0) ||(null!=bigFund && bigFund.size()>0) ){
						if(null!=inouter && inouter.size()>0){
							sb.append(inouter.get(HQConstant.SUM_INNER_VOLUME)==null?0:inouter.get(HQConstant.SUM_INNER_VOLUME)).append("|")
							.append(inouter.get(HQConstant.SUM_OUTER_VOLUME)==null?0:inouter.get(HQConstant.SUM_OUTER_VOLUME)).append("|");
						}else{
							sb.append("0|0|");
						}
						if(null!=bigFund && bigFund.size()>0){
							 sb.append(bigFund.get(HQConstant.BIG_FUNDS_INFLOWS)==null?0:bigFund.get(HQConstant.BIG_FUNDS_INFLOWS)).append("|")
							.append(bigFund.get(HQConstant.BIG_FUNDS_OUTFLOWS)==null?0:bigFund.get(HQConstant.BIG_FUNDS_OUTFLOWS));
						}else{
							sb.append("0|0");
						}
						pres.setProperty(key, sb.toString());
					}else{
						continue;
					}
					}
					CacheDiskStoreUtil.writeCache2Disk(pres);
					 try {
						Thread.sleep(120*1000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
    	});
    }
    
    
   /**
    * 数据过滤：检查字段合法性
    */
    public  boolean isFilterTrace(HQSnapShot snapShot) {
	   if(HQGloableConfig.applicationSettings.getBaseConfig().getTraceStockSet().contains(snapShot.getCompositID())){
			logger.info("traceStocCode's hq data: stockCode = " + snapShot.getStockCode() +" exchange="+snapShot.getExchange()
					   + " date = " + snapShot.getDate() +"time = " + snapShot.getTime() + " preClose = " + snapShot.getPreClose()
					   + " high = " + snapShot.getHigh() + " open = "+ snapShot.getOpen() + " low = " + snapShot.getLow()
					   + " close = " + snapShot.getClose() + " volume = " + snapShot.getVolume() + " nowVol = " + snapShot.getNowVolume()
					   + " amount = " + snapShot.getAmount() +" netChange = " +snapShot.getNetChange() +" netChangeRatio = " +snapShot.getNetChangeRatio()
					   + " stockStatus="+snapShot.getStockStatus()+" startTime:"+snapShot.getStartTimeStamp());
		}
       if (snapShot.getOpen() < 0.00f) {
           logger.warn("params is  [snapShot.getOpen() < 0 ]invalid,SnapShot check hq.code =" + snapShot.getStockCode()+ ",open=" + snapShot.getOpen());
           return true;
       }
       if (snapShot.getHigh() < 0.00f) {
           logger.warn("params is  [snapShot.getHigh() < 0 ]invalid,SnapShot check hq.code =" + snapShot.getStockCode()+ ",high=" + snapShot.getHigh());
           return true;
       }
       if (snapShot.getLow() < 0.00f) {
           logger.warn("params is  [snapShot.getLow() < 0 ]invalid,SnapShot check hq.code =" +snapShot.getStockCode() + ",low=" + snapShot.getLow());
           return true;
       }
       if (snapShot.getClose() < 0.00f) {
           logger.warn("params is  [snapShot.getClose() < 0 ]invalid,SnapShot check hq.code =" + snapShot.getStockCode()
                   + ",close=" + snapShot.getClose());
           return true;
       }
       if (snapShot.getVolume() < 0) {
           logger.warn("params [snapShot.getVolume() < 0 ] is invalid,SnapShot check hq.code =" + snapShot.getStockCode() + ",Volume=" + snapShot.getVolume());
           return true;
       }
       if (snapShot.getAmount() < 0.00f) {
           logger.warn("params [snapShot.getAmount() < 0 ] is invalid,SnapShot check hq.code =" + snapShot.getStockCode() + ",Amount=" + snapShot.getAmount());
           return true;
       }
       if (snapShot.getVolumeRatio() < 0.00f) {
           logger.warn("params [snapShot.getVolumeRatio() < 0 ] is invalid,SnapShot check hq.code =" +snapShot.getStockCode()+ ",VolumeRatio=" + snapShot.getVolumeRatio());
           return true;
       }
       if (snapShot.getAsset() == AssetType.INDEX.ordinal()) {
           if (snapShot.getRiseNum() < 0.00f) {
               logger.warn("params [snapShot.getRiseNum() < 0 ] is invalid,SnapShot check hq.code =" +snapShot.getStockCode()+ ",RiseNumber=" + snapShot.getRiseNum());
               return true;
           }
           if (snapShot.getFairNum() < 0.00f) {
               logger.warn("params [snapShot.getFairNum() < 0 ] is invalid,SnapShot check hq.code =" + snapShot.getStockCode() + ",EvenNumber=" + snapShot.getFairNum());
               return true;
           }
           if (snapShot.getFallNum() < 0.00f) {
               logger.warn("params [snapShot.getFallNum() < 0 ] is invalid,SnapShot check hq.code =" + snapShot.getStockCode()+ ",FallNumber=" + snapShot.getFallNum());
               return true;
           }
       }
       if (snapShot.getNowVolume() < 0) {
           logger.warn("params [snapShot.getNowVol() < 0 ] is invalid,SnapShot check hq.code =" + snapShot.getStockCode()+ ",NowVolume=" + snapShot.getNowVolume());
           return true;
       }
       if (snapShot.getCapitalization() < 0.00f) {
           logger.warn("params [snapShot.getCapitalization() < 0 ] is invalid,SnapShot check hq.code =" + snapShot.getStockCode() + ",Capitalization="+ snapShot.getCapitalization());
           return true;
       }
       if (snapShot.getCurrencyValue() < 0.00f) {
           logger.warn("params [snapShot.getCurrencyValue() < 0 ] is invalid,SnapShot check hq.code =" + snapShot.getStockCode() + ",CurrencyValue="+ snapShot.getCurrencyValue());
           return true;
       }
       return false;
   }
   
}
