package com.baidu.stock.process.monitor.zk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baidu.stock.ha.LeaderChangeListener;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;

/**
 * 
 * @author dengjianli
 *
 */
public class ZKNotifyListener implements LeaderChangeListener {
	private static Logger logger = LoggerFactory.getLogger(ZKNotifyListener.class);  
	
	@Override
	public void update(boolean isLeader) {
		  String info=isLeader?"发现当前服务状态是:主 ":"服务状态是:备 ";
		  logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,info));
	}

}
