package com.baidu.stock.quote.process.asyn;


import java.util.List;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import com.baidu.stock.process.channel.HQDirectChannel;
import com.baidu.stock.quote.asyn.RecordBundle;
import com.baidu.stock.quote.asyn.schedule.WriteSchedule;

/**
 * 同步输出
 * @author dengjianli
 *
 * @param <T>
 */
public class TestAsynOut extends WriteSchedule{
	
	public void onRecordBundle(RecordBundle bundle){
		if (null!=bundle){
			if (null!=bundle.getRecords()&& bundle.getRecords().size() > 0){
				List<Object>lstHQ=bundle.getRecords();
				System.out.println("receive:"+lstHQ.size());
			}
		}
	}

	@Override
	public void setHQDirectChannel(DirectChannel directChannel) {
	}

	@Override
	public void setHQExecutorChannel(ExecutorChannel executorChannel) {
	}


}
