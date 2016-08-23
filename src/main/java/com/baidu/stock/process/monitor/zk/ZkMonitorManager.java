package com.baidu.stock.process.monitor.zk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baidu.stock.ha.LeaderElectionAgent;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.process.util.FileUtil;

/**
 * 
 * @author dengjianli
 *
 */
public class ZkMonitorManager {
	private static Logger logger = LoggerFactory.getLogger(ZkMonitorManager.class);  
  //标记当前服务是否处于master状态Y
  public static LeaderElectionAgent leaderElectionAgent;
  public static void initZkMonitor() {
	  leaderElectionAgent=LeaderElectionAgent.getInstance();
	  leaderElectionAgent.addListener(new ZKNotifyListener());
	  leaderElectionAgent.start();
	  String info=isMaster()?"当前服务状态是:主 ":"服务状态是:备 ";
	  logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY , LogConstant.DEFAULT_INFO_VALUE ,info));
  }
  
  public static boolean isMaster() {
		try {
			return leaderElectionAgent.isMaster();
		} catch (Exception e) {
			 logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.MASTER_SLAVE_SWITCH_ERROR_KEY , LogConstant.MASTER_SLAVE_SWITCH_ERROR_VALUE ,"基于zookeeper主从切换异常:\n"+ FileUtil.printException(e)));
		}
		return false;
	}
}
