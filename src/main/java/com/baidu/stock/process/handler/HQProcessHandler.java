package com.baidu.stock.process.handler;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import com.baidu.stock.process.channel.HQExecutorOutChannel;
import com.baidu.stock.process.context.HQProcessContext;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.process.monitor.zk.ZkMonitorManager;
import com.baidu.stock.process.wrap.HQDataProcessor;
import com.baidu.stock.process.wrap.HQDataProcessor.TaskEvent;

/**
 * 行情处理加工
 * @author dengjianli
 *
 */
public class HQProcessHandler implements MessageHandler {
	/**
	 * 行情数据加工处理器
	 */
	private HQDataProcessor hqprocessor;
	private List<HQExecutorOutChannel>lstExecutorChannel;
	private int writerCount;
	public HQProcessHandler(HQDataProcessor hqprocessor,List<HQExecutorOutChannel>lstExecutorChannel,int writerCount){
		this.hqprocessor=hqprocessor;
		this.lstExecutorChannel=lstExecutorChannel;
		this.writerCount=writerCount;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void handleMessage(Message<?> message) {
		List<HQSnapShot>lstHQ=((List<HQSnapShot>) message.getPayload());
		TaskEvent result=hqprocessor.doProcess(lstHQ);
		if(ZkMonitorManager.isMaster()){
		 ConcurrentHashMap<String, Set<HQSnapShot>>snapshotMap=result.getResultMap();
		 if(snapshotMap.size()>0){
			 for(String key:snapshotMap.keySet()){
				 Set<HQSnapShot>builderSet=snapshotMap.get(key);
				    int  cid=Integer.parseInt(message.getHeaders().get("cid").toString());
				    Message<Set<HQSnapShot>> msg = MessageBuilder.withPayload(builderSet).build();
				    //这里采用负载,因为发送有序处理,目前5个生产者配置,暂时写死
				    int step=writerCount/HQProcessContext.producerNum;
				    if(cid<step*1){
				    	lstExecutorChannel.get(0).send(msg);
				    }else if(cid<step*2){
				    	lstExecutorChannel.get(1).send(msg);
				    }else if(cid<step*3){
				    	lstExecutorChannel.get(2).send(msg);
				    }else if(cid<step*4){
				    	lstExecutorChannel.get(3).send(msg);
				    }else if(cid<step*5){
				    	lstExecutorChannel.get(4).send(msg);	
				    }
			 }
		 }
	}
	}
}