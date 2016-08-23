package com.baidu.stock.process.fetch.adapter.impl;

import java.util.List;
import org.springframework.core.env.PropertyResolver;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.process.channel.HQExecutorOutChannel;
import com.baidu.stock.process.fetch.IFetchAction;
import com.baidu.stock.process.fetch.adapter.AbstractDataSourcesAdapterHandler;
import com.baidu.stock.process.fetch.core.HQThreadListenerContainer;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.process.fetch.reader.HQReader;

/**
 * 行情数据源抓取适配处理器
 * @author dengjianli
 *
 */
public class SZHQDataSourcesHandler extends AbstractDataSourcesAdapterHandler<HQSnapShot> {
	public SZHQDataSourcesHandler(PropertyResolver propertyResolver,HQReader[] szProcessor,HQExecutorOutChannel executorOutChannel,HQThreadListenerContainer container){
		super(szProcessor);//目前sz没有fast格式文件，只有dbf格式
		this.executorOutChannel=executorOutChannel;
		this.fetchIntervalMillis=Integer.parseInt(propertyResolver.getProperty("worker_fetch_IntervalMillis", "3000"));
		this.container=container;
	}
	
	@Override
	public  List<HQSnapShot> fetchHQ() {
		return this.fetchDataFromFile(new IFetchAction<HQSnapShot>() {
			@Override
			public List<HQSnapShot> doFetch() {
				return fetchInvoke();
			}
		});
		}
	
	private List<HQSnapShot> fetchInvoke() {
		List<HQSnapShot> lstHQ = null;
		// stop定时任务时重新初始化isExchangeOpenFlag的值,用于判断是否是第一次刚开盘
		if (!container.isExchangeOpenFlag) {
			// 预抓取，如果没有更新则轮询比对，发现今天新行情更新便执行获取行情接口
			lstHQ = getHQReader().preFetchHQ();
			if (null != lstHQ && lstHQ.size() > 0) {
				container.isExchangeOpenFlag = true;
					Message<List<HQSnapShot>> message = MessageBuilder.withPayload(lstHQ).build();
					executorOutChannel.send(message);
					logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"sz 发送当天首次开盘股票代码表完成."));
					return lstHQ;
			} 
		} else {
			// 获取行情数据
			lstHQ = getHQReader().process();
			if (null != lstHQ && lstHQ.size() > 0) {
				logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"线程ID:" + Thread.currentThread().getId()+ " 轮询sz市场抓取行情文件["+getHQReader().getHQurl()+"]处理,其中处理数量:" + lstHQ.size()));
			}
		}
		return lstHQ;
	}
	
	
}
