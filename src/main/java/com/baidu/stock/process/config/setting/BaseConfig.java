package com.baidu.stock.process.config.setting;

//import java.util.ArrayList;
//import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
/**
 * 
 * @author dengjianli
 *
 */
@Component
@ConfigurationProperties(prefix="hqRunTimeConfig.baseConfig")
public class BaseConfig{
		private long sendFullTimeMillis;
		private long dataExceptionTimeSecond;
		private String materialURL;
//		private List<String>traceStockSet=new ArrayList<String>();
		private CopyOnWriteArrayList<String> traceStockSet=new CopyOnWriteArrayList<String>();
		public BaseConfig(){}
		public long getSendFullTimeMillis() {
			return sendFullTimeMillis;
		}
		public void setSendFullTimeMillis(long sendFullTimeMillis) {
			this.sendFullTimeMillis = sendFullTimeMillis;
		}
		
		public long getDataExceptionTimeSecond() {
			return dataExceptionTimeSecond;
		}
		public void setDataExceptionTimeSecond(long dataExceptionTimeSecond) {
			this.dataExceptionTimeSecond = dataExceptionTimeSecond;
		}
/*		public List<String> getTraceStockSet() {
			return traceStockSet;
		}
		public void setTraceStockSet(List<String> traceStockSet) {
			this.traceStockSet = traceStockSet;
		}*/
		
		public String getMaterialURL() {
			return materialURL;
		}
		public CopyOnWriteArrayList<String> getTraceStockSet() {
			return traceStockSet;
		}
		public void setTraceStockSet(CopyOnWriteArrayList<String> traceStockSet) {
			this.traceStockSet = traceStockSet;
		}
		public void setMaterialURL(String materialURL) {
			this.materialURL = materialURL;
		}
	}