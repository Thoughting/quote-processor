package com.baidu.stock.process.config.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
/**
 * 
 * @author dengjianli
 *
 */
    @Component
	@ConfigurationProperties(prefix="hqRunTimeConfig.activeMQConfig")
	public class ActiveMQConfig{
    	//broker的协议及地址
		private String brokerUrl;
		//用户名
		private String userName;
		//密码
	    private String password;
	    //点queue:队列 topic：主题
	    private boolean topic=true;
	    //消息传递是否持久化模式
	    private boolean persistent=true;
	    private String   snapshotDestination ;
	    private String   notifyDestination;
	    private String   materialDestination;
	    private long timetolive=1800000;
	    private int maxConnection=120;
	    //消息的transacted模式,true为事务模式，false非事务模式
	    private boolean transacted=false;
	    //如果是topic方式 需要设置该client id 
	    private	String clientId;
	    private	String clienName;
	    
	    public ActiveMQConfig(){}
	    
		public String getBrokerUrl() {
			return brokerUrl;
		}

		public void setBrokerUrl(String brokerUrl) {
			this.brokerUrl = brokerUrl;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
		
		public boolean isTopic() {
			return topic;
		}

		public void setTopic(boolean topic) {
			this.topic = topic;
		}

		public boolean isPersistent() {
			return persistent;
		}
		public void setPersistent(boolean persistent) {
			this.persistent = persistent;
		}
		
		public long getTimetolive() {
			return timetolive;
		}
		public void setTimetolive(long timetolive) {
			this.timetolive = timetolive;
		}

		public boolean isTransacted() {
			return transacted;
		}

		public void setTransacted(boolean transacted) {
			this.transacted = transacted;
		}

		public String getClientId() {
			return clientId;
		}

		public void setClientId(String clientId) {
			this.clientId = clientId;
		}

		public String getClienName() {
			return clienName;
		}

		public void setClienName(String clienName) {
			this.clienName = clienName;
		}

		public int getMaxConnection() {
			return maxConnection;
		}

		public void setMaxConnection(int maxConnection) {
			this.maxConnection = maxConnection;
		}

		public String getSnapshotDestination() {
			return snapshotDestination;
		}

		public void setSnapshotDestination(String snapshotDestination) {
			this.snapshotDestination = snapshotDestination;
		}

		public String getNotifyDestination() {
			return notifyDestination;
		}

		public void setNotifyDestination(String notifyDestination) {
			this.notifyDestination = notifyDestination;
		}

		public String getMaterialDestination() {
			return materialDestination;
		}

		public void setMaterialDestination(String materialDestination) {
			this.materialDestination = materialDestination;
		}
	    
	}