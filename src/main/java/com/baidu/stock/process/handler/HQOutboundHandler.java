package com.baidu.stock.process.handler;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.process.monitor.metrics.MetricsManager;
import com.baidu.stock.process.util.CommonHelp;
import com.baidu.stock.quote.protocbuf.SnapShotListProto;
import com.baidu.stock.quote.protocbuf.SnapShotProto;

/**
 * 行情下发处理
 * @author dengjianli
 *
 */
public class HQOutboundHandler implements MessageHandler{
//	private static Logger logger = LoggerFactory.getLogger(HQOutboundHandler.class);

	@SuppressWarnings("unchecked")
	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		 Set<HQSnapShot>snapshotSet=(Set<HQSnapShot>) message.getPayload();
		 //统计写入MQ的TPS统计
		 MetricsManager.mqRequest.mark(snapshotSet.size());
		 SnapShotListProto.SnapShotList.Builder snapShotListBuilder=SnapShotListProto.SnapShotList.newBuilder();
		 for(HQSnapShot snapShot:snapshotSet){
			 SnapShotProto.SnapShot hqpbSnapShot=CommonHelp.convertSnapShot(snapShot);
			 snapShotListBuilder.addSnapShot(hqpbSnapShot);
		 }
		 CommonHelp.sendSnapshotToMQ("", com.baidu.stock.jms.model.Message.SNAPSHOT_TYPE,snapShotListBuilder.build().toByteArray());
//		 logger.info("批量大小:"+snapshotSet.size()+" 耗时:  "+Thread.currentThread().getId()+"  "+(System.currentTimeMillis()- snapshotSet.iterator().next().getStartTimeStamp()));
		 //记录当前瞬间的耗时,该耗时：文件抓取出来到写入MQ的时间，整体的服务处理的时间：MetricsManager.timeConsuming + 文件抓取时间
		 MetricsManager.timeConsuming=(System.currentTimeMillis()- snapshotSet.iterator().next().getStartTimeStamp());
	}
}
