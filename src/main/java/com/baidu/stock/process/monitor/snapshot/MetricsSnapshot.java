package com.baidu.stock.process.monitor.snapshot;

import java.io.Serializable;


/**
 * 性能快照
 * @author dengjianli
 *
 */
public class MetricsSnapshot implements Serializable{
	private static final long serialVersionUID = 1L;
	private String des="抓取文件耗时指标";
	private double maxMillis=0;
	private double minMillis=0;
	private double meanMillis=0;
	//耗时的分布情况
	private double p75=0;
	private double p95=0;
	private double p98=0;
	private double p99=0;
	private double p999=0;
	//每秒回调次数统计
	private double m1Rate=0;
	private double m5Rate=0;
	private double m15Rate=0;
	private double meanRate=0;
	
	public String getDes() {
		return des;
	}
	public double getMaxMillis() {
		return maxMillis;
	}
	public void setMaxMillis(double maxMillis) {
		this.maxMillis = maxMillis;
	}
	public double getMinMillis() {
		return minMillis;
	}
	public void setMinMillis(double minMillis) {
		this.minMillis = minMillis;
	}
	public double getMeanMillis() {
		return meanMillis;
	}
	public void setMeanMillis(double meanMillis) {
		this.meanMillis = meanMillis;
	}
	public double getP75() {
		return p75;
	}
	public void setP75(double p75) {
		this.p75 = p75;
	}
	public double getP95() {
		return p95;
	}
	public void setP95(double p95) {
		this.p95 = p95;
	}
	public double getP98() {
		return p98;
	}
	public void setP98(double p98) {
		this.p98 = p98;
	}
	public double getP99() {
		return p99;
	}
	public void setP99(double p99) {
		this.p99 = p99;
	}
	
	public double getP999() {
		return p999;
	}
	public void setP999(double p999) {
		this.p999 = p999;
	}
	public double getM1Rate() {
		return m1Rate;
	}
	public void setM1Rate(double m1Rate) {
		this.m1Rate = m1Rate;
	}
	public double getM5Rate() {
		return m5Rate;
	}
	public void setM5Rate(double m5Rate) {
		this.m5Rate = m5Rate;
	}
	public double getM15Rate() {
		return m15Rate;
	}
	public void setM15Rate(double m15Rate) {
		this.m15Rate = m15Rate;
	}
	public double getMeanRate() {
		return meanRate;
	}
	public void setMeanRate(double meanRate) {
		this.meanRate = meanRate;
	}

	
}
