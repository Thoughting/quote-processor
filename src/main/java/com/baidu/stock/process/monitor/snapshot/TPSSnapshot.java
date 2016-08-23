package com.baidu.stock.process.monitor.snapshot;

import java.io.Serializable;

/**
 * 写入TPS统计
 * @author dengjianli
 *
 */
public class TPSSnapshot implements Serializable{
	private static final long serialVersionUID = 1L;
	private String des="MQ写入(TPS)指标";
	private long requestCount ;
	private double meanRate;
	public String getDes() {
		return des;
	}
	public long getRequestCount() {
		return requestCount;
	}
	public void setRequestCount(long requestCount) {
		this.requestCount = requestCount;
	}
	public double getMeanRate() {
		return meanRate;
	}
	public void setMeanRate(double meanRate) {
		this.meanRate = meanRate;
	} 
	
}
