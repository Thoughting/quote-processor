package com.baidu.stock.process.config.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 定时配置
 * @author dengjianli
 *
 */
@Component
@ConfigurationProperties(prefix="application.schedule")
public class AppScheduleConfig {
	private String market_initMaterial_cronExpression;
	private String market_notopen_cronExpression;
	private String market_auction_cronExpression;
	private String market_amTransaction_cronExpression;
	private String market_lunchBreak_cronExpression;
	private String market_pmTransaction_cronExpression;
	private String market_closed_cronExpression;
	private String worker_start_cronExpression;
	private String worker_start_time;
	private String worker_stop_cronExpression;
	private String worker_stop_time;
	private int worker_fetch_IntervalMillis;
	private String service_clean_cronExpression;
	
	public AppScheduleConfig(){}

	public String getMarket_notopen_cronExpression() {
		return market_notopen_cronExpression;
	}

	public void setMarket_notopen_cronExpression(
			String market_notopen_cronExpression) {
		this.market_notopen_cronExpression = market_notopen_cronExpression;
	}

	public String getMarket_auction_cronExpression() {
		return market_auction_cronExpression;
	}

	public void setMarket_auction_cronExpression(
			String market_auction_cronExpression) {
		this.market_auction_cronExpression = market_auction_cronExpression;
	}

	public String getMarket_amTransaction_cronExpression() {
		return market_amTransaction_cronExpression;
	}

	public void setMarket_amTransaction_cronExpression(
			String market_amTransaction_cronExpression) {
		this.market_amTransaction_cronExpression = market_amTransaction_cronExpression;
	}

	public String getMarket_lunchBreak_cronExpression() {
		return market_lunchBreak_cronExpression;
	}

	public void setMarket_lunchBreak_cronExpression(
			String market_lunchBreak_cronExpression) {
		this.market_lunchBreak_cronExpression = market_lunchBreak_cronExpression;
	}

	public String getMarket_pmTransaction_cronExpression() {
		return market_pmTransaction_cronExpression;
	}

	public void setMarket_pmTransaction_cronExpression(
			String market_pmTransaction_cronExpression) {
		this.market_pmTransaction_cronExpression = market_pmTransaction_cronExpression;
	}

	public String getMarket_closed_cronExpression() {
		return market_closed_cronExpression;
	}

	public void setMarket_closed_cronExpression(String market_closed_cronExpression) {
		this.market_closed_cronExpression = market_closed_cronExpression;
	}

	public String getWorker_start_cronExpression() {
		return worker_start_cronExpression;
	}

	public void setWorker_start_cronExpression(String worker_start_cronExpression) {
		this.worker_start_cronExpression = worker_start_cronExpression;
	}

	public String getWorker_start_time() {
		return worker_start_time;
	}

	public void setWorker_start_time(String worker_start_time) {
		this.worker_start_time = worker_start_time;
	}

	public String getWorker_stop_cronExpression() {
		return worker_stop_cronExpression;
	}

	public void setWorker_stop_cronExpression(String worker_stop_cronExpression) {
		this.worker_stop_cronExpression = worker_stop_cronExpression;
	}

	public String getWorker_stop_time() {
		return worker_stop_time;
	}

	public void setWorker_stop_time(String worker_stop_time) {
		this.worker_stop_time = worker_stop_time;
	}

	public int getWorker_fetch_IntervalMillis() {
		return worker_fetch_IntervalMillis;
	}

	public void setWorker_fetch_IntervalMillis(int worker_fetch_IntervalMillis) {
		this.worker_fetch_IntervalMillis = worker_fetch_IntervalMillis;
	}

	public String getMarket_initMaterial_cronExpression() {
		return market_initMaterial_cronExpression;
	}

	public void setMarket_initMaterial_cronExpression(
			String market_initMaterial_cronExpression) {
		this.market_initMaterial_cronExpression = market_initMaterial_cronExpression;
	}

	public String getService_clean_cronExpression() {
		return service_clean_cronExpression;
	}

	public void setService_clean_cronExpression(String service_clean_cronExpression) {
		this.service_clean_cronExpression = service_clean_cronExpression;
	}
	
}
