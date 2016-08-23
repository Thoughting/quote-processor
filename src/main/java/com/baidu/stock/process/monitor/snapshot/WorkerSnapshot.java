package com.baidu.stock.process.monitor.snapshot;

import java.io.Serializable;

/**
 * worker线程参数快照
 * @author dengjianli
 *
 */
public class WorkerSnapshot implements Serializable{
	private static final long serialVersionUID = 1L;

	private String workerName;
	private String runStats;
	private String stockCodeSendStatus;//每天初始化标志位状态
	private int concumer; //消费者数
	private int maxConcumer; //最大消费者数
	

	public String getRunStats() {
		return runStats;
	}

	public void setRunStats(String runStats) {
		this.runStats = runStats;
	}

	public String getStockCodeSendStatus() {
		return stockCodeSendStatus;
	}

	public void setStockCodeSendStatus(String stockCodeSendStatus) {
		this.stockCodeSendStatus = stockCodeSendStatus;
	}

	public int getConcumer() {
		return concumer;
	}
	public void setConcumer(int concumer) {
		this.concumer = concumer;
	}
	public int getMaxConcumer() {
		return maxConcumer;
	}
	public void setMaxConcumer(int maxConcumer) {
		this.maxConcumer = maxConcumer;
	}
	public String getWorkerName() {
		return workerName;
	}
	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}
	
	
}
