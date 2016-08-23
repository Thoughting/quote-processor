package com.baidu.stock.process.config;

import java.text.ParseException;
import java.util.concurrent.Executors;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.baidu.stock.process.config.setting.AppScheduleConfig;
import com.baidu.stock.process.cron.ScheduleSignalTaskJob;
import com.baidu.stock.process.util.HQConstant;
import com.baidu.stock.process.util.HQStatus;


/**
 * 定时任务配置
 * @author dengjianli
 *
 */
@Configuration
@EnableConfigurationProperties(AppScheduleConfig.class)
public class SchedulerCronConfig {
	private Logger logger = LoggerFactory.getLogger(SchedulerCronConfig.class); 
	private String targetMethodName=HQConstant.DEFAULT_SCHEDULE_METHODNAME;
	@Resource
	private AppScheduleConfig appScheduleConfig;
	
	

/**
 * 配置初始化基础数据
 * @return
 * @throws ParseException
 */
	@Bean
	public CronTriggerBean initMaterialCronTrigger() throws ParseException {
		CronTriggerBean cronTrigger = new CronTriggerBean();
		cronTrigger.setJobDetail(initMaterialJobDetail().getObject());
		cronTrigger.setCronExpression(appScheduleConfig.getMarket_initMaterial_cronExpression());
		logger.info("初始化-配置基础数据定时正则表达式:"+appScheduleConfig.getMarket_initMaterial_cronExpression());
		return cronTrigger;
	}
	@Bean
	public MethodInvokingJobDetailFactoryBean initMaterialJobDetail() {
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(new ScheduleSignalTaskJob(HQStatus.INIT));
		jobDetail.setTargetMethod(targetMethodName);
		return jobDetail;
	}
	
	/**
	 * 开盘信号
	 * @return
	 * @throws ParseException
	 */
	@Bean
	public CronTriggerBean openMarketCronTrigger() throws ParseException {
		CronTriggerBean cronTrigger = new CronTriggerBean();
		cronTrigger.setJobDetail(openMarketJobDetail().getObject());
		cronTrigger.setCronExpression(appScheduleConfig.getMarket_notopen_cronExpression());
		logger.info("初始化-配置开盘信号定时正则表达式:"+appScheduleConfig.getMarket_notopen_cronExpression());
		return cronTrigger;
	}
	@Bean
	public MethodInvokingJobDetailFactoryBean  openMarketJobDetail() {
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(new ScheduleSignalTaskJob(HQStatus.NOTOPEN));
		jobDetail.setTargetMethod(targetMethodName);
		return jobDetail;
	}
	

	/**
	 * 配置集合竞价信号定时
	 * @return
	 * @throws ParseException
	 */
	@Bean
	public CronTriggerBean auctionCronTrigger() throws ParseException {
		CronTriggerBean cronTrigger = new CronTriggerBean();
		cronTrigger.setJobDetail(auctionJobDetail().getObject());
		cronTrigger.setCronExpression(appScheduleConfig.getMarket_auction_cronExpression());
		logger.info("初始化-配置集合竞价信号定时正则表达式:"+appScheduleConfig.getMarket_auction_cronExpression());
		return cronTrigger;
	}
	@Bean
	public MethodInvokingJobDetailFactoryBean auctionJobDetail() {
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(new ScheduleSignalTaskJob(HQStatus.AUCTION));
		jobDetail.setTargetMethod(targetMethodName);
		return jobDetail;
	}
	
	
	
	/**
	 * 配置上午交易信号定时器
	 * @return
	 * @throws ParseException
	 */
	@Bean
	public CronTriggerBean amTransactionCronTrigger() throws ParseException {
		CronTriggerBean cronTrigger = new CronTriggerBean();
		cronTrigger.setJobDetail(amTransactionJobDetail().getObject());
		cronTrigger.setCronExpression(appScheduleConfig.getMarket_amTransaction_cronExpression());
		logger.info("初始化-配置上午交易信号定时正则表达式:"+appScheduleConfig.getMarket_amTransaction_cronExpression());
		return cronTrigger;
	}
	@Bean
	public MethodInvokingJobDetailFactoryBean amTransactionJobDetail() {
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(new ScheduleSignalTaskJob(HQStatus.TRANSACTIONAM));
		jobDetail.setTargetMethod(targetMethodName);
		return jobDetail;
	}
	
	
	/**
	 * 配置午休信号定时器
	 * @return
	 * @throws ParseException
	 */
	@Bean
	public CronTriggerBean lunchBreakCronTrigger() throws ParseException {
		CronTriggerBean cronTrigger = new CronTriggerBean();
		cronTrigger.setJobDetail(lunchBreakJobDetail().getObject());
		cronTrigger.setCronExpression(appScheduleConfig.getMarket_lunchBreak_cronExpression());
		logger.info("初始化-配置午休信号定时正则表达式:"+appScheduleConfig.getMarket_lunchBreak_cronExpression());
		return cronTrigger;
	}
	@Bean
	public MethodInvokingJobDetailFactoryBean lunchBreakJobDetail() {
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(new ScheduleSignalTaskJob(HQStatus.LUNCHBREAK));
		jobDetail.setTargetMethod(targetMethodName);
		return jobDetail;
	}
	
	
	/**
	 * 配置下午交易信号定时器
	 * @return
	 * @throws ParseException
	 */
	@Bean
	public CronTriggerBean pmTransactionCronTrigger() throws ParseException {
		CronTriggerBean cronTrigger = new CronTriggerBean();
		cronTrigger.setJobDetail(pmTransactionJobDetail().getObject());
		cronTrigger.setCronExpression(appScheduleConfig.getMarket_pmTransaction_cronExpression());
		logger.info("初始化-配置下午交易信号定时正则表达式:"+appScheduleConfig.getMarket_pmTransaction_cronExpression());
		return cronTrigger;
	}
	@Bean
	public MethodInvokingJobDetailFactoryBean pmTransactionJobDetail() {
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(new ScheduleSignalTaskJob(HQStatus.TRANSACTIONPM));
		jobDetail.setTargetMethod(targetMethodName);
		return jobDetail;
	}
	
	/**
	 * 配置闭盘信号定时器
	 * @return
	 * @throws ParseException
	 */
	@Bean
	public CronTriggerBean closeMarketCronTrigger() throws ParseException {
		CronTriggerBean cronTrigger = new CronTriggerBean();
		cronTrigger.setJobDetail(closeMarketJobDetail().getObject());
		cronTrigger.setCronExpression(appScheduleConfig.getMarket_closed_cronExpression());
		logger.info("初始化-配置闭盘信号定时正则表达式:"+appScheduleConfig.getMarket_closed_cronExpression());
		return cronTrigger;
	}
	@Bean
	public MethodInvokingJobDetailFactoryBean closeMarketJobDetail() {
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(new ScheduleSignalTaskJob(HQStatus.CLOSE));
		jobDetail.setTargetMethod(targetMethodName);
		return jobDetail;
	}
	
	
	/**
	 * 配置worke抓取行情文件定时启动
	 * @return
	 * @throws ParseException
	 */
	@Bean
	public CronTriggerBean workerStartCronTrigger() throws ParseException {
		CronTriggerBean cronTrigger = new CronTriggerBean();
		cronTrigger.setJobDetail(workerStartJobDetail().getObject());
		cronTrigger.setCronExpression(appScheduleConfig.getWorker_start_cronExpression());
		logger.info("初始化-配置worke抓取行情文件定时启动正则表达式:"+appScheduleConfig.getWorker_start_cronExpression());
		return cronTrigger;
	}
	@Bean
	public MethodInvokingJobDetailFactoryBean workerStartJobDetail() {
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(new ScheduleSignalTaskJob(HQStatus.STARTWORKER));
		jobDetail.setTargetMethod(targetMethodName);
		return jobDetail;
	}
	
	
	/**
	 * 配置worke抓取行情文件定时停止
	 * @return
	 * @throws ParseException
	 */
	@Bean
	public CronTriggerBean workerStopCronTrigger() throws ParseException {
		CronTriggerBean cronTrigger = new CronTriggerBean();
		cronTrigger.setJobDetail(workerStopJobDetail().getObject());
		cronTrigger.setCronExpression(appScheduleConfig.getWorker_stop_cronExpression());
		logger.info("初始化-配置worke抓取行情文件定时停止正则表达式:"+appScheduleConfig.getWorker_stop_cronExpression());
		return cronTrigger;
	}
	@Bean
	public MethodInvokingJobDetailFactoryBean workerStopJobDetail() {
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(new ScheduleSignalTaskJob(HQStatus.STOPWORKER));
		jobDetail.setTargetMethod(targetMethodName);
		return jobDetail;
	}
	
	
	
	/**
	 * 配置清理缓存数据
	 * @return
	 * @throws ParseException
	 */
	@Bean
	public CronTriggerBean cleanCronTrigger() throws ParseException {
		CronTriggerBean cronTrigger = new CronTriggerBean();
		cronTrigger.setJobDetail(cleanJobDetail().getObject());
		cronTrigger.setCronExpression(appScheduleConfig.getService_clean_cronExpression());
		logger.info("初始化-配置清理系统内存数据正则表达式:"+appScheduleConfig.getService_clean_cronExpression());
		return cronTrigger;
	}
	
	@Bean
	public MethodInvokingJobDetailFactoryBean cleanJobDetail() {
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(new ScheduleSignalTaskJob(HQStatus.CLEAN));
		jobDetail.setTargetMethod(targetMethodName);
		return jobDetail;
	}
	
	/**
	 * 定时器执行工厂
	 * @return
	 * @throws ParseException
	 */
	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() throws ParseException {
		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
		schedulerFactoryBean.setTriggers(initMaterialCronTrigger(),openMarketCronTrigger(),
				auctionCronTrigger(),
				amTransactionCronTrigger(),
				lunchBreakCronTrigger(),
				pmTransactionCronTrigger(),
				closeMarketCronTrigger(),
				workerStartCronTrigger(),
				workerStopCronTrigger(),
				cleanCronTrigger());
		schedulerFactoryBean.setTaskExecutor(Executors.newFixedThreadPool(2));
		return schedulerFactoryBean;
	}
	
}
