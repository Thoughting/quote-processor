package com.baidu.stock.process.fetch.listener;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.process.asyn.HQAsynWriterTemplate;
import com.baidu.stock.process.fetch.ThreadAwareMessageListener;
import com.baidu.stock.process.fetch.core.AbstractPollingThreadListenerContainer;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.process.util.FileUtil;

/**
 * 行情源监听器
 * @author dengjianli
 *
 */

public class HQDataSourceListener  implements ThreadAwareMessageListener<HQSnapShot> {
	private static Logger logger = LoggerFactory.getLogger(HQDataSourceListener.class);
	private HQAsynWriterTemplate hqAsynWriterTemplate;
	
	public HQDataSourceListener(HQAsynWriterTemplate asynWriterTemplate){
		this.hqAsynWriterTemplate=asynWriterTemplate;
	}
	
	@Override
	public void onMessage(AbstractPollingThreadListenerContainer container,List<HQSnapShot> message) throws Exception {
	try{
	   List<HQSnapShot> lstHQ=message;
	   if(null!=lstHQ && lstHQ.size()>0){
		   hqAsynWriterTemplate.write(lstHQ);
	   }
	}catch (Exception e) {
		logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_INFO_VALUE,"发送行情对象到内部队列异常"+FileUtil.printException(e)));
	}
	}
}
