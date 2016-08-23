package com.baidu.stock.process.asyn;

import java.util.ArrayList;
import java.util.List;
import com.baidu.stock.process.channel.HQDirectChannel;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.quote.asyn.AsynWriterTemplate;
import com.baidu.stock.quote.asyn.HQConsistentShard;
import com.baidu.stock.quote.asyn.IAsynWriter;

/**
 * 行情异步写入
 * @author dengjianli
 *
 */
public class HQAsynWriterTemplate extends AsynWriterTemplate<HQSnapShot>{
	private HQConsistentShard<Integer> shard;
	private IAsynWriter[] asynWriterwriters;
	
	public HQAsynWriterTemplate(int writerCount,HQDirectChannel hqDirectChannel){
		super(writerCount,hqDirectChannel,null);
		List<Integer>lstShard=new ArrayList<Integer>();
		for(int i=0;i<writerCount;i++){
			lstShard.add(i);
		}
		//一致性hash
		shard=new HQConsistentShard<Integer>(lstShard,500);
		this.asynWriterwriters=getWriters();
	}

	/**
	 * 利用一致性hash负载路由,根据股票代码和市场hash
	 * 这里不用泛型处理,因为结合业务处理考虑因素多,这里采用object对象统一处理
	 */
	@Override
	public IAsynWriter getWriter(Object obj) {
		HQSnapShot hq=(HQSnapShot) obj;
		String stockCode=hq.getStockCode();
		String exchange=hq.getExchange();
		String key=String.valueOf(stockCode).concat(exchange);
		IAsynWriter[] writers =  getWriters();
		if (writers != null && writers.length > 0){
			int index=shard.getShardInfo(key);
			return writers[index];
		}
		else{
			return writers[0];
		}
	}

	public IAsynWriter[] getAsynWriters() {
		return asynWriterwriters;
	}
	
}

