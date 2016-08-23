package com.baidu.stock.process.config.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 
 * @author dengjianli
 *
 */
    @Component
	@ConfigurationProperties(prefix="hqRunTimeConfig.marketTime")
	public class MarketTime{
    	//发送交易日时间 0:01
		private String notopenedTime;
		private String exchageInitTime;
		//集合计价时间9:00~9:15
		private String auctionTime;
		//上午交易时间9:30
		private String transactionAMTime;
		//下午交易时间 1:00
		private String transactionPMTime;
		//午市时间 11:30
		private String lunchbreakTime;
		//闭盘时间 15：00
		private String closedTime;
		//停止发送全量行情时间 15:04 这个时间必须和发送停盘信号(定时任务{字段:market_closed_cronExpression})的时间一致
		private String stopSendFullHQTime;
		//深圳收盘竞价开始时间
		private String closedAuctionTime;
		
		//long类型,上面的字段long类型对应转换
		private long exchageInitTimeForLong;
		private long amTime;
		private long pmTime;
		private long auctionStartTimeForLong;
		private long lunchbreakTimeForLong;
		private long closedTimeForLong;
		private long stopSendFullHQTimeForLong;
		private long closedAuctionTimeForLong;
		
		public MarketTime(){}
		public String getNotopenedTime() {
			return notopenedTime;
		}
		public void setNotopenedTime(String notopenedTime) {
			this.notopenedTime = notopenedTime;
		}
		public String getExchageInitTime() {
			return exchageInitTime;
		}
		public void setExchageInitTime(String exchageInitTime) {
			this.exchageInitTime = exchageInitTime;
		}
		public String getAuctionTime() {
			return auctionTime;
		}
		public void setAuctionTime(String auctionTime) {
			this.auctionTime = auctionTime;
		}
		public String getTransactionAMTime() {
			return transactionAMTime;
		}
		public void setTransactionAMTime(String transactionAMTime) {
			this.transactionAMTime = transactionAMTime;
		}
		public String getTransactionPMTime() {
			return transactionPMTime;
		}
		public void setTransactionPMTime(String transactionPMTime) {
			this.transactionPMTime = transactionPMTime;
		}
		public String getLunchbreakTime() {
			return lunchbreakTime;
		}
		public void setLunchbreakTime(String lunchbreakTime) {
			this.lunchbreakTime = lunchbreakTime;
		}
		public String getClosedTime() {
			return closedTime;
		}
		public void setClosedTime(String closedTime) {
			this.closedTime = closedTime;
		}
		public long getExchageInitTimeForLong() {
			return exchageInitTimeForLong;
		}
		public void setExchageInitTimeForLong(long exchageInitTimeForLong) {
			this.exchageInitTimeForLong = exchageInitTimeForLong;
		}
		public long getAmTime() {
			return amTime;
		}
		public void setAmTime(long amTime) {
			this.amTime = amTime;
		}
		public long getPmTime() {
			return pmTime;
		}
		public void setPmTime(long pmTime) {
			this.pmTime = pmTime;
		}
		public long getAuctionStartTimeForLong() {
			return auctionStartTimeForLong;
		}
		public void setAuctionStartTimeForLong(long auctionStartTimeForLong) {
			this.auctionStartTimeForLong = auctionStartTimeForLong;
		}
		public long getLunchbreakTimeForLong() {
			return lunchbreakTimeForLong;
		}
		public void setLunchbreakTimeForLong(long lunchbreakTimeForLong) {
			this.lunchbreakTimeForLong = lunchbreakTimeForLong;
		}
		public long getClosedTimeForLong() {
			return closedTimeForLong;
		}
		public void setClosedTimeForLong(long closedTimeForLong) {
			this.closedTimeForLong = closedTimeForLong;
		}
		public String getStopSendFullHQTime() {
			return stopSendFullHQTime;
		}
		public void setStopSendFullHQTime(String stopSendFullHQTime) {
			this.stopSendFullHQTime = stopSendFullHQTime;
		}
		public long getStopSendFullHQTimeForLong() {
			return stopSendFullHQTimeForLong;
		}
		public void setStopSendFullHQTimeForLong(long stopSendFullHQTimeForLong) {
			this.stopSendFullHQTimeForLong = stopSendFullHQTimeForLong;
		}
        public String getClosedAuctionTime() {
            return closedAuctionTime;
        }
        public void setClosedAuctionTime(String closedAuctionTime) {
            this.closedAuctionTime = closedAuctionTime;
        }
        public long getClosedAuctionTimeForLong() {
            return closedAuctionTimeForLong;
        }
        public void setClosedAuctionTimeForLong(long closedAuctionTimeForLong) {
            this.closedAuctionTimeForLong = closedAuctionTimeForLong;
        }
	}