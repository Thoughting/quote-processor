package com.baidu.stock.process.monitor.snapshot;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 写入器快照
 * @author dengjianli
 *
 */
public class InnerBufferWriterSnapshot implements Serializable{
	private static final long serialVersionUID = 1L;

	private String des="内部缓存指标";
	private String exchange;
    private int writerNum;
	private Map<String,Integer>queueMapper=new HashMap<String,Integer>();
	
	public String getDes() {
		return exchange+des;
	}
	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	public int getWriterNum() {
		return writerNum;
	}
	public void setWriterNum(int writerNum) {
		this.writerNum = writerNum;
	}
	public Map<String, Integer> getQueueMapper() {
		return queueMapper;
	}
	public void setQueueMapper(Map<String, Integer> queueMapper) {
		this.queueMapper = queueMapper;
	}
	
}
