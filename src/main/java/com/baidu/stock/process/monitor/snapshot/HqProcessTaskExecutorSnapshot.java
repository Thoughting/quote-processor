package com.baidu.stock.process.monitor.snapshot;

/**
 * 
 * @author dengjianli
 *
 */
public class HqProcessTaskExecutorSnapshot {

	private long active;
	private long corePoolSize;
	public long getActive() {
		return active;
	}
	public void setActive(long active) {
		this.active = active;
	}
	public long getCorePoolSize() {
		return corePoolSize;
	}
	public void setCorePoolSize(long corePoolSize) {
		this.corePoolSize = corePoolSize;
	}
	
	
}
