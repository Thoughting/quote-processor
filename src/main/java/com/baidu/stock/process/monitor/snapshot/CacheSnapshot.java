package com.baidu.stock.process.monitor.snapshot;

/**
 * 
 * @author dengjianli
 *
 */
public class CacheSnapshot {

	private String name;
	private int cacheSize;
	private double hitRatio;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCacheSize() {
		return cacheSize;
	}
	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}
	public double getHitRatio() {
		return hitRatio;
	}
	public void setHitRatio(double hitRatio) {
		this.hitRatio = hitRatio;
	}
	
}
