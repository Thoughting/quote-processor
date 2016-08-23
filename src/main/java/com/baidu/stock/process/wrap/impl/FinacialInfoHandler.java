package com.baidu.stock.process.wrap.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baidu.stock.common.AssetType;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.material.domain.StockFinance;
import com.baidu.stock.process.cache.AssistDataCache;
import com.baidu.stock.process.cache.FinacialInfoCache;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.process.util.FileUtil;
import com.baidu.stock.process.util.HQConstant;
import com.baidu.stock.process.wrap.AbstractSnapshotHandler;

/**
 * 财务相关信息包装处理
 * @author dengjianli
 *
 */

public class FinacialInfoHandler extends AbstractSnapshotHandler{
	private static final Logger logger = LoggerFactory.getLogger(FinacialInfoHandler.class);
	
	@Override
	public HQSnapShot handleRequest(HQSnapShot snapShot){
		HQSnapShot newSnapShot=process(snapShot);
		if(null!=getNextHandler()){
			newSnapShot=getNextHandler().handleRequest(newSnapShot);
		}
		return newSnapShot;
	}
	
    private HQSnapShot process(HQSnapShot snapShot){
    	// 跳过指数/基金/回购
    	int asset=snapShot.getAsset();
        if (asset== AssetType.INDEX.value() || asset== AssetType.FUND.value() || asset== AssetType.REPURCHASE.value()){
            return snapShot;
        }
 	  
        try {
            // 提取财务信息
        	StockFinance financeInfo= getFinacialInfo(snapShot);
        	if(null!=financeInfo){
            // 股本数据的度量
            checkCapitalChange(snapShot,financeInfo);
            // 财务数据的度量
            checkFinanceChange(snapShot,financeInfo);
        	}else{
        		logger.warn(snapShot.getCompositID()+" 没有找到对应的财务数据.");
        	}
        } catch (Exception e) {
  			 logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY , LogConstant.DEFAULT_ERROR_VALUE ," exchange="+snapShot.getExchange()+" stockCode="+snapShot.getStockCode()+"行情计算依赖财务信息处理异常:" + FileUtil.printException(e)));
        }
        return snapShot;
    }

    /**
     * 提取财务信息
     */
    private StockFinance getFinacialInfo(HQSnapShot snapShot) throws Exception {
    	String compositId=snapShot.getCompositID();
        StockFinance financeInfo=  FinacialInfoCache.query(compositId);
        if (financeInfo != null) {
            //每股收益
            snapShot.setStockProfit(financeInfo.getStockProfit());
            snapShot.setNetAssets(financeInfo.getNetAssets());
        }else{
  			logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY , LogConstant.DEFAULT_ERROR_VALUE ,"从内存中没有找到 exchange="+snapShot.getExchange()+" stockCode="+snapShot.getStockCode()+"获取财务信息为空."));
        }
        return financeInfo;
    }

    /**
     * 计算财务数据
     */
    private void checkCapitalChange(HQSnapShot snapShot,StockFinance financeInfo) throws Exception {
        // 使用价格
    	float currPrice = snapShot.getClose();
        if (currPrice < 0.00001f) {
            currPrice = snapShot.getPreClose();
        }
        // 流通股本
        long tradeVolume = financeInfo.getAamount();
        snapShot.setTradeVolume(tradeVolume);
        // 总市值: 现价 * 总股数
        // 总市值
        snapShot.setCapitalization(currPrice * financeInfo.getTotalAmount());
        //总股本
        snapShot.setTotalAmount(financeInfo.getTotalAmount());
        // 流通市值: 现价 * 流通股数
        // 流通市值
        snapShot.setCurrencyValue(currPrice * tradeVolume);
        // 换手率：日成交量（股数）/ 流通股数 × 100%
        if (tradeVolume > 0) {
            double todayVolume = snapShot.getVolume();
            snapShot.setTurnoverRatio((float) (todayVolume * 1.00f / tradeVolume * 100.00f)); // 换手率
        }
    }

    /**
     * 财务数据的度量
     * @return 
     */
    private void checkFinanceChange(HQSnapShot snapShot,StockFinance financeInfo) throws Exception {
        // 使用价格
        float closePrice =snapShot.getClose();
        if (closePrice < 0.00001f) {
            closePrice =snapShot.getPreClose();
        }
        if(snapShot.getExchange().equalsIgnoreCase(HQConstant.shExchangeId)
            && snapShot.getStockCode().startsWith("9",0)){
        	Double usdDouble= AssistDataCache.query(HQConstant.FOREIGNEXCHANGE_KEY, "USD");
        	float usd=0.00f;
        	if(null!=usdDouble){
        		usd=usdDouble.floatValue();
        	}
            if(usd != 0){
                closePrice = usd * closePrice;
            }
        }else if(snapShot.getExchange().equalsIgnoreCase(HQConstant.szExchangeId)
                && snapShot.getStockCode().startsWith("2",0)){
        	Double hkdollarDouble= AssistDataCache.query(HQConstant.FOREIGNEXCHANGE_KEY, "HKDOLLAR");
        	float hkdollar=0.00f;
        	if(null!=hkdollarDouble){
        		hkdollar=hkdollarDouble.floatValue();
        	}
            if(hkdollar != 0){
                closePrice = hkdollar * closePrice;
            }
        }
        float capitalVolume = financeInfo.getTotalAmount();
        // MRQ 市盈率 = 实时价格*总股本/(最新财报中的净利润 * 年化系数)
        float MRQProfits = Double.valueOf(financeInfo.getMrq()).floatValue();
        
        if(MRQProfits != 0 && capitalVolume !=0){
             // MRQ市盈率
             snapShot.setMrqPeratio((closePrice * capitalVolume)/ MRQProfits);
        }
        // TTM 市盈率 = 实时价格/(最近4个季度利润总和÷总股本)
        float TTMProfits = Double.valueOf(financeInfo.getTtm()).floatValue();
        if(TTMProfits != 0 && capitalVolume !=0){
             // TTM 市盈率
             snapShot.setTtmPeratio(closePrice / (TTMProfits / capitalVolume));
        }
        
        // LYR 市盈率   = 实时价格/(最近年度的净利润÷总股本)
        float LYRProfits =Double.valueOf(financeInfo.getLyr()).floatValue();
        if(LYRProfits != 0 && capitalVolume !=0){
                // LYR 市盈率
                snapShot.setLyrPeratio(closePrice / (LYRProfits / capitalVolume)); 
        }
        
        // 市净率 = 股价 ÷ 每股净资产
        if (financeInfo.getNetAssets() != 0) {
            // 市净率
        	float netAssets=Double.valueOf(financeInfo.getNetAssets()).floatValue();
            snapShot.setBvRatio(closePrice/netAssets);
        }
        // 市销率
        double totalSalesAmount =financeInfo.getSellAmountPS();//总营业收入
        if (totalSalesAmount != 0) {
            // 市销率 = 总市值 ÷ 主营业务收入
              //总市值
            float totalSharesValue = closePrice * financeInfo.getTotalAmount();
            // 市销率
            snapShot.setPsRatio((float) (totalSharesValue / totalSalesAmount));
        }
    }
 
}
