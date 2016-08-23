package com.baidu.stock.process.fetch.reader.field;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @author dengjianli
 *
 */
public class FastHQMessage {
	private String exchangeState;
	private String dbfDate;
	private  CopyOnWriteArrayList<Map<String,String>> lstHQ = new CopyOnWriteArrayList<Map<String,String>>();

	public String getExchangeState() {
		return exchangeState;
	}
	public void setExchangeState(String exchangeState) {
		this.exchangeState = exchangeState;
	}
	public String getDbfDate() {
		return dbfDate;
	}
	public void setDbfDate(String dbfDate) {
		this.dbfDate = dbfDate;
	}
	public CopyOnWriteArrayList<Map<String, String>> getLstHQ() {
		return lstHQ;
	}
	
}
