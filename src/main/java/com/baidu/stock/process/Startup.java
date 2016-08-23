package com.baidu.stock.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.process.config.HQConfiguration;
import com.baidu.stock.process.config.SchedulerCronConfig;

/**
 * 行情数据处理服务启动入口
 * @author dengjianli
 *
 */

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Startup { 
	private static Logger logger = LoggerFactory.getLogger(Startup.class);  
	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(Startup.class,HQConfiguration.class,SchedulerCronConfig.class);
	    application.setRegisterShutdownHook(true);
	    application.run(args);
		logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_INFO_KEY, LogConstant.DEFAULT_INFO_VALUE,"行情数据处理服务启动完成."));
	}
}
