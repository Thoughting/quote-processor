package com.baidu.stock.process.asyn;

import java.util.List;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import com.baidu.stock.process.channel.HQDirectChannel;
import com.baidu.stock.quote.asyn.RecordBundle;
import com.baidu.stock.quote.asyn.schedule.WriteSchedule;

/**
 * 同步输出
 * @author dengjianli
 *
 * @param <T>
 */
public class HQSynOutputSchedule extends WriteSchedule{
	private HQDirectChannel hqDirectChannel=null;
	
	public void onRecordBundle(RecordBundle bundle){
		if (null!=bundle){
			if (null!=bundle.getRecords()&& bundle.getRecords().size() > 0){
				List<Object>lstHQ=bundle.getRecords();
				Message<List<Object>>message=MessageBuilder.withPayload(lstHQ).setHeader("cid",bundle.getConsumerName()).build();
				hqDirectChannel.send(message);
			}
		}
	}

	@Override
	public void setHQDirectChannel(DirectChannel directChannel) {
		this.hqDirectChannel=(HQDirectChannel) directChannel;
	}

	@Override
	public void setHQExecutorChannel(ExecutorChannel executorChannel) {
	}


}
