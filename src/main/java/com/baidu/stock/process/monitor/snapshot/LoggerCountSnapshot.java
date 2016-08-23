package com.baidu.stock.process.monitor.snapshot;

import java.io.Serializable;

/**
 * 日志统计
 * @author dengjianli
 *
 */
public class LoggerCountSnapshot implements Serializable{
	private static final long serialVersionUID = 1L;
	private String des="日志监控指标(警告和错误次数统计)";
	private long warn;
	private long error;
	public long getWarn() {
		return warn;
	}
	public void setWarn(long warn) {
		this.warn = warn;
	}
	public long getError() {
		return error;
	}
	public void setError(long error) {
		this.error = error;
	}
	public String getDes() {
		return des;
	}
	
}
