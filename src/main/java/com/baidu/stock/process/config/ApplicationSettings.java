package com.baidu.stock.process.config;

import javax.annotation.Resource;
import org.springframework.stereotype.Component;

import com.baidu.stock.process.config.setting.ActiveMQConfig;
import com.baidu.stock.process.config.setting.AppScheduleConfig;
import com.baidu.stock.process.config.setting.BaseConfig;
import com.baidu.stock.process.config.setting.DataSource;
import com.baidu.stock.process.config.setting.MarketTime;

@Component
public class ApplicationSettings {
	@Resource
	private AppScheduleConfig appScheduleConfig;
	@Resource
	private DataSource dataSource;
	@Resource
	private MarketTime marketTime;
	@Resource
	private ActiveMQConfig activeMQConfig;
	@Resource
	private BaseConfig baseConfig;
	public ApplicationSettings(){}
	public MarketTime getMarketTime() {
		return marketTime;
	}
	public void setMarketTime(MarketTime marketTime) {
		this.marketTime = marketTime;
	}
	public BaseConfig getBaseConfig() {
		return baseConfig;
	}
	public void setBaseConfig(BaseConfig baseConfig) {
		this.baseConfig = baseConfig;
	}
	public ActiveMQConfig getActiveMQConfig() {
		return activeMQConfig;
	}
	public void setActiveMQConfig(ActiveMQConfig activeMQConfig) {
		this.activeMQConfig = activeMQConfig;
	}
	public AppScheduleConfig getAppScheduleConfig() {
		return appScheduleConfig;
	}
	public void setAppScheduleConfig(AppScheduleConfig appScheduleConfig) {
		this.appScheduleConfig = appScheduleConfig;
	}
	public DataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
