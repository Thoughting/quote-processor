package com.baidu.stock.process.fetch.meta;

import java.io.Serializable;

/**
 * 
 * @author dengjianli
 *
 */
public class LastPoint implements Serializable {
	private static final long serialVersionUID = 1L;

    // 成交量
    private long   volume = 0L;
    // 成交额
    private double amount = 0.00f;
    /**
	 * 0:股票 1:期货 2:期权 3:外汇 4指数 5:场内基金 6:债券、7:认购权证 8:认沽权证 9: 牛证 10:熊证  
     *11:其他  12:表示场外 13:表示货币 14:表示B股 15:回购 16:场内的货币基金 17:港股基金
	 */
    private int asset;
    
    private String exchange;

    private String stockCode;

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public int getAsset() {
		return asset;
	}

	public void setAsset(int asset) {
		this.asset = asset;
	}

	public String getStockCode() {
		return stockCode;
	}

	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
    
}
