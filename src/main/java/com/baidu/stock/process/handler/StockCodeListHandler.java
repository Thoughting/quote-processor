package com.baidu.stock.process.handler;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.process.util.CommonHelp;
import com.baidu.stock.process.util.FileUtil;
import com.baidu.stock.quote.protocbuf.CodeListProto;
import com.baidu.stock.quote.protocbuf.StockBasicProto;

/**
 * 股票代码发下发处理
 * @author dengjianli
 *
 */
public class StockCodeListHandler implements MessageHandler {
	private static Logger logger = LoggerFactory.getLogger(StockCodeListHandler.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		try{
			 List<HQSnapShot>lstHQ=(List<HQSnapShot>) message.getPayload();
			 CodeListProto.CodeList.Builder codeList= CodeListProto.CodeList.newBuilder();
			   for(HQSnapShot hqSnapshot:lstHQ){
					StockBasicProto.StockBasic.Builder stockBasic=StockBasicProto.StockBasic.newBuilder();
					if(StringUtils.isEmpty(hqSnapshot.getStockCode()) || !hqSnapshot.getStockCode().matches("^\\d{6}$")){
						logger.warn(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_WARN_KEY, LogConstant.DEFAULT_WARN_VALUE,"过滤非法股票代码: " + stockBasic.getStockCode()));
						continue;
					}
					stockBasic.setExchange(hqSnapshot.getExchange());
					stockBasic.setStockCode(hqSnapshot.getStockCode());
					stockBasic.setAsset(hqSnapshot.getAsset());
					stockBasic.setStockName(hqSnapshot.getStockName());
					stockBasic.setStockStatus(hqSnapshot.getStockStatus());
					codeList.addStockBasic(stockBasic.build());
					logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"准备发送市场:"+stockBasic.getExchange()+" 股票代码:"+stockBasic.getStockCode()));
				    }
			   codeList.setDate(lstHQ.get(0).getDate());
			   codeList.setExchange(lstHQ.get(0).getExchange());
			   //发送mq
			   byte[] codelistByte=codeList.build().toByteArray();
			   CommonHelp.sendSingalToMQ("codelist", codelistByte);
			   logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"发送市场股票代码完成."));
			}catch(Exception ex){
			   logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"发送市场股票代码完成异常:\n"+ FileUtil.printException(ex)));
			}
	}
	
}



