package com.baidu.stock.process.wrap.impl;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baidu.stock.common.AssetType;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.process.cache.AssistDataCache;
import com.baidu.stock.process.cache.LastPointCache;
import com.baidu.stock.process.cache.RiseFallCache;
import com.baidu.stock.process.config.HQGloableConfig;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.process.fetch.meta.LastPoint;
import com.baidu.stock.process.util.CommonHelp;
import com.baidu.stock.process.util.FileUtil;
import com.baidu.stock.process.util.HQConstant;
import com.baidu.stock.process.wrap.AbstractSnapshotHandler;
/**
 * 度量处理
 * @author dengjianli
 *
 */
public class MetricHandler extends AbstractSnapshotHandler{
	 private static final Logger logger = LoggerFactory.getLogger(MetricHandler.class);
	 private static int quoteCount = 5;
//	 private static ThreadPoolExecutor fixedTaskExecutor = new ThreadPoolExecutor(1, 1, 300,TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1),new ThreadPoolExecutor.DiscardPolicy());
	 private static ThreadPoolExecutor fixedsum399001Or399006AmountTaskExecutor = new ThreadPoolExecutor(2, 2, 300,TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1),new ThreadPoolExecutor.DiscardPolicy());
	
	 @Override
	public HQSnapShot handleRequest(HQSnapShot snapShot) {
		 HQSnapShot newSnapShot=process(snapShot);
			if(null!=getNextHandler()){
				newSnapShot=getNextHandler().handleRequest(newSnapShot);
			}
			return newSnapShot;
		}
	    
      private HQSnapShot process(HQSnapShot snapShot){
        try {
            // 预计算399001金额  计算399006金额
        	 Future<SumAmountResult>future=sum399001And399006Amount(snapShot);
            // 成交数量的度量
            checkVolumeChange(snapShot);
            // 价格变化的度量
            checkPriceChange(snapShot);
            // 更新并返回当前行情缓存
            updateCandleBar(snapShot);
            // 然后进行涨跌统计
            if (snapShot.getAsset()== AssetType.INDEX.value()) {
                publishResult(snapShot);
            } else {
                // 更新涨跌状态, 只更新涨跌统计
                if (snapShot.getAsset()== AssetType.A_STOCK.value()) {
                    updateStatus(snapShot);
                }
            }
            if(null!=future){
            if ((snapShot.getStockCode().equalsIgnoreCase("399001")|| snapShot.getStockCode().equalsIgnoreCase("399006")) && snapShot.getExchange().equalsIgnoreCase(HQConstant.szExchangeId)) {
            fillFutureResult(future,snapShot);
            }
            }
        } catch (Exception e) {
 			 logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY , LogConstant.DEFAULT_ERROR_VALUE ," exchange="+snapShot.getExchange()+" stockCode="+snapShot.getStockCode()+"计算行情成交数量、价格变化、跌涨幅度量数据异常:\n:" + FileUtil.printException(e)));
        }
		return snapShot;
    }
      
     
    
      /**
       * 从线程结果future获取结果,降低线程耗时性能
       * @param future
       * @param snapShot
       */
      private void fillFutureResult(Future<SumAmountResult>future,HQSnapShot snapShot){
    	  final String stockCode =snapShot.getStockCode();
    	 try {
    		 //fix 2015年12月31日17:52:27 优化统计深交和创业板累加计算次数,这里等待1秒，如果超时则获取上一次的值
			SumAmountResult result=future.get(1, TimeUnit.SECONDS);
			if(null!=result){
            snapShot.setAmount(result.getAmount()); // 成交金额 (元)
            snapShot.setVolume(result.getVolume()); // 总成交量 (股)
    		 }else{
    			 if (stockCode.equalsIgnoreCase("399001")){
    				 snapShot.setAmount(result399001.getAmount()); // 成交金额 (元)
    		         snapShot.setVolume(result399001.getVolume()); // 总成交量 (股)
                 }else if (stockCode.equalsIgnoreCase("399006")){
                	 snapShot.setAmount(result399006.getAmount()); 
                     snapShot.setVolume(result399006.getVolume());
	                 } 
    		 }
		} catch (Exception e) {
			//这里不建议暴漏异常信息,因为这里的异常是正常的.
			if (stockCode.equalsIgnoreCase("399001")){
				 snapShot.setAmount(result399001.getAmount()); // 成交金额 (元)
		         snapShot.setVolume(result399001.getVolume()); // 总成交量 (股)
            }else if (stockCode.equalsIgnoreCase("399006")){
           	    snapShot.setAmount(result399006.getAmount()); 
                snapShot.setVolume(result399006.getVolume());
                } 
		}finally{
			future.cancel(true);
		}
      }
      
      /**
       * 优化计算特殊逻辑，预计算
       * @param snapShot
       * @return
       * @throws Exception
       */
 // 成交数量的度量
    @SuppressWarnings("unchecked")
	private Future<SumAmountResult> sum399001And399006Amount(HQSnapShot snapShot) throws Exception {
        final String stockCode =snapShot.getStockCode();
        final String exchange = snapShot.getExchange();
        Future<SumAmountResult>future=null;
      //深证成指[399001]指数实时行情,创业板指[399006]指数实时行情
        if ((stockCode.equalsIgnoreCase("399001")|| stockCode.equalsIgnoreCase("399006")) && exchange.equalsIgnoreCase(HQConstant.szExchangeId)) {
        future=fixedsum399001Or399006AmountTaskExecutor.submit(new Callable<SumAmountResult>() {
			@Override
			public SumAmountResult call() throws Exception {
				SumAmountResult result=new SumAmountResult();
		            double amount = 0.00f;
		            long volume = 0L;
		            List<String>lstKey=LastPointCache.getCache().getKeys();
		            for(String key:lstKey){
		            	LastPoint point=LastPointCache.query(key);
		            	if (null!=point) {
		            	  if (stockCode.equalsIgnoreCase("399001")&& point.getExchange().equalsIgnoreCase(HQConstant.szExchangeId)){
		            		  if (point.getAsset() == AssetType.A_STOCK.value()
		                              || point.getAsset() == AssetType.B_STOCK.value()) {
			                        amount += point.getAmount();
			                        volume += point.getVolume();
		            		  }
		            		 }else if(stockCode.equalsIgnoreCase("399006")&& point.getExchange().equalsIgnoreCase(HQConstant.szExchangeId)){
		            			  if (point.getAsset() == AssetType.A_STOCK.value()) {
		                          	if((point.getStockCode()).startsWith("3")){
				                        amount += point.getAmount();
				                        volume += point.getVolume(); 
		                          	}
		            			  }
		            		 }
		            	}
		            }
	                  result.setAmount(amount);
	                  result.setVolume(volume);
	                  if (stockCode.equalsIgnoreCase("399001")){
	                	  setResult399001(amount,volume);
	                  }else if (stockCode.equalsIgnoreCase("399006")){
	                	  setResult399006(amount,volume);
		               }
				return result;
			}
		});
    }
        return future;
    }

    
    // 成交数量的度量
    private void checkVolumeChange(HQSnapShot snapShot) throws Exception {
        String exchange = snapShot.getExchange();
        // 5日平均成交量
	     String key =snapShot.getCompositID();
        double volumeOf5Days = AssistDataCache.query(key, HQConstant.DAY5_AVERAGE_VOLUME_KEY);
        // 累计开市时间
        int duration = 0;
        // 5日平均每分钟成交量
        double avgVolume = 0.00f;
	    //转换到毫秒级别
        int eventTime = Integer.parseInt(snapShot.getTime()) * 1000;
        if (exchange.equalsIgnoreCase(HQConstant.shExchangeId)
            || exchange.equalsIgnoreCase(HQConstant.szExchangeId)) {
            duration = CommonHelp.getCNDuration(eventTime);
            avgVolume = volumeOf5Days / 240;
        } else {
            duration = 0;
        }

        // 量比 = 现成交总手数 / 现累计开市时间(分) / 过去5日平均每分钟成交量
        if (avgVolume > 0.00001f && duration > 0) {
            // 量比
            snapShot.setVolumeRatio((float) (snapShot.getVolume()/ duration / avgVolume));
        }

        // 指数不计算委比和委差
        if (snapShot.getAsset() == AssetType.INDEX.ordinal()) {
            return;
        }

        // 委比: ( 委买手数 － 委卖手数 ) / ( 委买手数 + 委卖手数 ) × 100%
        //       委买手数：委托买入五档的总数量
        //       委卖手数：委托卖出五档的总数量
        long totalAskVolume = 0;
        long totalBidVolume = 0;
        long[]sellv = snapShot.getSellv();
        long[]buyv = snapShot.getBuyv();
        if (sellv != null) {
        for (int i = 0; i < sellv.length && i < quoteCount; i++) { // 卖5档
            if (sellv[i] == 0) {
                continue;
            }
            totalAskVolume += sellv[i];
        }
        }
        if (buyv != null) {
        for (int i = 0; i < buyv.length && i < quoteCount; i++) { // 买5档
            if (buyv[i] == 0) {
                continue;
            }
            totalBidVolume += buyv[i];
        }
        }
        // ( 委买手数 － 委卖手数 ) / ( 委买手数 + 委卖手数 ) × 100%
        if ((totalBidVolume + totalAskVolume) != 0) {
            double quoteDiff = (totalBidVolume - totalAskVolume);
            double quoteSums = (totalBidVolume + totalAskVolume);
            // 委比
            snapShot.setWeibiRatio((float) (quoteDiff / quoteSums * 100.00f));
        }
    }

    /**
     * 价格变化的度量
     */
	private void checkPriceChange(HQSnapShot snapShot) throws Exception {
        float preClose = snapShot.getPreClose();
        boolean execDiff = false;
        float diffPrice = 0.00f;
        float close = snapShot.getClose();
        //转换到毫秒级别
        int eventTime = Integer.parseInt(snapShot.getTime()) * 1000;
        // 分别处理集合竞价, 正常交易
        if (close > 0.00001f) {
            // 正常交易阶段
            execDiff = true;
            diffPrice = close - preClose;
        } else {
            // 集合竞价阶段93000000
            if (eventTime<(HQGloableConfig.applicationSettings.getMarketTime().getAmTime()* 1000)) {
                float[] sellp =snapShot.getSellp();
                if (sellp != null && sellp.length> 0 && sellp[0] > 0.00001f) {
                		  execDiff = true;
                          close = sellp[0];
                          diffPrice = close - preClose;
                } else {
                    float[]buyp=snapShot.getBuyp();
                    if (buyp != null && buyp.length> 0 && buyp[0]> 0.00001f) {
                            execDiff = true;
                            close = buyp[0];
                            diffPrice = close - preClose;
                    } else {
                        close = preClose;
                    }
                }
            } else {
                close = preClose;
            }
            snapShot.setClose(close); // 当前价
        }
        // 有正常可识别的数据是才进行计算
        if (execDiff && preClose > 0.00001f) {
            // 涨跌 = 现价 - 昨收盘价
            // 价格变化
            snapShot.setNetChange(diffPrice); // 涨跌
            // 涨幅 = (现价 - 昨收盘价) / 昨收盘价 * 100%
            snapShot.setNetChangeRatio(diffPrice / preClose * 100.00f); // 涨跌幅
        } else {
        	 snapShot.setNetChange(0.00f);
        	 snapShot.setNetChangeRatio(0.00f);
        }
        // 振幅 = (当日最高点的价格 － 当日最低点的价格) / 昨收盘价 × 100%
        if (preClose > 0.00001f) {
            float diffStage =snapShot.getHigh()- snapShot.getLow();
            if (diffStage > 0.00001f) {
            	snapShot.setAmplitudeRatio(diffStage / preClose * 100.00f); // 震幅
            } else {
                snapShot.setAmplitudeRatio(0.00f);
            }
        }
        // 涨停价：昨收价X110%,ST股为昨收价X105%,新股为发行价X144%
        float limitUp = getRiseLimit(snapShot);
        // 涨停 (转为double类型)
        snapShot.setLimitUp(Float.parseFloat(String.valueOf(limitUp)));
        // 跌停价：昨收价X90%,ST股为昨收价X95%,新股发行价X64%
        float limitDown = getFallLimit(snapShot,preClose);
        snapShot.setLimitDown(limitDown);
    }
    

    /**
     * 发布统计结果
     */
    private void publishResult(HQSnapShot snapShot) throws Exception {
        String stockCode =snapShot.getStockCode();
        String exchange = snapShot.getExchange();
        // 上交所：只在遭遇上证综指时，选择向外输出涨跌统计
        if (exchange.equalsIgnoreCase(HQConstant.shExchangeId)
            && stockCode.equalsIgnoreCase(HQConstant.SH_MASTER_INDEX)) {
            extractResult(snapShot);
        }
        // 深交所: 只在遭遇深市成指时，选择向外输出涨跌统计
        if (exchange.equalsIgnoreCase(HQConstant.szExchangeId)
            && stockCode.equalsIgnoreCase(HQConstant.SZ_MASTER_INDEX)) {
            extractResult(snapShot);
        }
    }

    /**
     * 提取统计结果
     */
	private void extractResult(HQSnapShot snapShot) throws Exception {
		 //不用每次都计算，即使每次计算也不能保证最新的值是当前，因为是多线程处理，这里优化：仅仅一个线程执行最新的计算，每次拿结果值或再次计算,算业务降级处理
//    	  snapShot.setRiseNum(riseCount);
//        snapShot.setFallNum(fallCount);
//        snapShot.setFairNum(fairCount);
//    	fixedTaskExecutor.execute(new StatisticsTotal(this,snapShot));
       //如下是之前的方式，不合理
         int riseCount=0;
	     int fallCount=0;
	     int fairCount=0;
	     List<?> lstKey=null;
	     if(HQConstant.shExchangeId.equals(snapShot.getExchange())){
	    	 lstKey=RiseFallCache.getSHCache().getKeys();
	     }else if(HQConstant.szExchangeId.equals(snapShot.getExchange())){
	    	 lstKey=RiseFallCache.getSZCache().getKeys();
	     }
		 if(null!=lstKey && lstKey.size()>0){
	   	  for(Object key:lstKey){
	   		 Integer value=RiseFallCache.query((String) key,snapShot.getExchange());
	   		 if(null!=value){
	             if (value.intValue() > 0) {
	                riseCount++;
	             } else if (value.intValue() < 0) {
	                fallCount++;
	             } else {
	             	fairCount++;
	             }
	   		 }
	   	  }
		 }
	    	snapShot.setRiseNum(riseCount);
	        snapShot.setFallNum(fallCount);
	        snapShot.setFairNum(fairCount);
		 
    }
    
    /**
     * 更新涨跌状态： 1 表示上涨，0 表示平盘， -1 表示下跌
     */
    private void updateStatus(HQSnapShot snapShot) throws Exception {
    	 String exchange = snapShot.getExchange();
    	 String stockCode =snapShot.getStockCode();
	     String key =snapShot.getCompositID();
        // 剔除B股: 上海B股以9开头
        if (exchange.equalsIgnoreCase(HQConstant.shExchangeId)
            && stockCode.startsWith("9")) {
            return;
        }
        // 剔除B股: 深圳B股以2开头
        if (exchange.equalsIgnoreCase(HQConstant.szExchangeId)
            && stockCode.startsWith("2")) {
            return;
        }
        float close =snapShot.getClose();
        float preClose =snapShot.getPreClose();
        int tagValue = 0;
        if (close > 0.0001f) {
            if ((close - preClose) > 0.0001f) {
                tagValue = 1;
                //统计计数
            } else if ((preClose - close) > 0.0001f) {
                tagValue = -1;
            }
        }
        RiseFallCache.addOrUpdate(key, tagValue,snapShot.getExchange());
    }

    /**
     * 更新并返回当前行情缓存
     */
    private void updateCandleBar(HQSnapShot snapShot) throws Exception {
	     String key =snapShot.getCompositID();
        // 获取上一笔行情的价, 量, 金额, 如果缓存内不存在，会返回一个新的 CandleBar
	     LastPoint lastPoint = LastPointCache.query(key);
        if(null!=lastPoint){
        long lastVolume = lastPoint.getVolume();
        double lastAmount = lastPoint.getAmount();
        snapShot.setNowVolume(snapShot.getVolume() - lastVolume);
        snapShot.setNowAmount(snapShot.getAmount() - lastAmount);
        // Current-Point 的 Volume 和 Amount
        // 直接记录行情数据, 系开市到当前累计值
        lastPoint.setVolume(snapShot.getVolume());
        lastPoint.setAmount(snapShot.getAmount());
        lastPoint.setAsset(snapShot.getAsset());
        lastPoint.setStockCode(snapShot.getStockCode());
        lastPoint.setExchange(snapShot.getExchange());
        }else{
        lastPoint =new LastPoint();
        lastPoint.setVolume(snapShot.getVolume());
        lastPoint.setAmount(snapShot.getAmount());
        lastPoint.setAsset(snapShot.getAsset());
        lastPoint.setStockCode(snapShot.getStockCode());
        lastPoint.setExchange(snapShot.getExchange());
        LastPointCache.add(key, lastPoint);
        }
    }

    private float getRiseLimit(HQSnapShot snapShot) {
    	float upLimit = 0;
        float preClose =snapShot.getPreClose();
        if (preClose < 0.00001f) {
            return upLimit;
        }
        if (snapShot.getStockName().startsWith(HQConstant.NEW_SYMBOL_SIGN)) {
            upLimit = preClose * HQConstant.RISE_LIMIT_RATIO_FOR_NEW;
        }else if (snapShot.getStockName().contains(HQConstant.ST_SYMBOL_SIGN)) {
            upLimit = preClose * HQConstant.RISE_LIMIT_RATIO_FOR_ST;
        }else{
        	upLimit = preClose * HQConstant.RISE_LIMIT_RATIO;
        }
        return upLimit;
    }

    /**
     * 跌停判断
     */
    private float getFallLimit(HQSnapShot snapShot, float preClose) {
    	float fallLimit = 0;
        if (preClose < 0.00001f) {
            return fallLimit;
        }
        if (snapShot.getStockName().startsWith(HQConstant.NEW_SYMBOL_SIGN)) {
        	fallLimit = preClose * HQConstant.FALL_LIMIT_RATIO_FOR_NEW;
        }else  if (snapShot.getStockName().contains(HQConstant.ST_SYMBOL_SIGN)) {
        	fallLimit = preClose * HQConstant.FALL_LIMIT_RATIO_FOR_ST;
        }else{
        	fallLimit = preClose * HQConstant.FALL_LIMIT_RATIO;
        }
        return fallLimit;
    }


    
    /*class StatisticsTotal implements Runnable{
    private MetricHandler metricHandler;
    private HQSnapShot snapShot;
    public  StatisticsTotal(MetricHandler metricHandler,HQSnapShot snapShot){
    	this.metricHandler=metricHandler;
    	this.snapShot=snapShot;
    }
	 @Override
	  public void run() {
	     int riseCount=0;
	     int fallCount=0;
	     int fairCount=0;
	     List<?> lstKey=null;
	     if(HQConstant.shExchangeId.equals(snapShot.getExchange())){
	    	 lstKey=RiseFallCache.getSHCache().getKeys();
	     }else if(HQConstant.szExchangeId.equals(snapShot.getExchange())){
	    	 lstKey=RiseFallCache.getSZCache().getKeys();
	     }
		 if(null!=lstKey && lstKey.size()>0){
	   	  for(Object key:lstKey){
	   		 Integer value=RiseFallCache.query((String) key,snapShot.getExchange());
	   		 if(null!=value){
	             if (value.intValue() > 0) {
	                riseCount++;
	             } else if (value.intValue() < 0) {
	                fallCount++;
	             } else {
	             	fairCount++;
	             }
	   		 }
	   	  }
		 }
	   	  this.metricHandler.setFairCount(fairCount);
	      this.metricHandler.setFallCount(fallCount);
	   	  this.metricHandler.setRiseCount(riseCount);
	   }
    }

    class StatisticsResult{
    	private int riseCount;
    	private int fallCount;
    	private int fairCount;
    	public StatisticsResult(int riseCount,int fallCount,int fairCount){
    		this.riseCount=riseCount;
    		this.fairCount=fairCount;
    		this.fallCount=fallCount;
    	}
    	
		public int getRiseCount() {
			return riseCount;
		}
		public void setRiseCount(int riseCount) {
			this.riseCount = riseCount;
		}
		public int getFallCount() {
			return fallCount;
		}
		public void setFallCount(int fallCount) {
			this.fallCount = fallCount;
		}
		public int getFairCount() {
			return fairCount;
		}
		public void setFairCount(int fairCount) {
			this.fairCount = fairCount;
		}
    }*/
    

   
    
}
