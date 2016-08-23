package com.baidu.stock.process.fetch.meta;


import java.io.Serializable;

import com.baidu.stock.quote.protocbuf.StockBasicProto.StockBasic;


/**
 * 数据块的基本定义
 * <p/>
 * Created by xugang on 2014-09-24
 */
public class Meta implements Serializable {
    // 日期 eg: 20140924
    protected int date = 0;
    // 时间 eg: 9300101222
    protected int time = 0;

    // 交易所, eg: sh, sz
    protected String exchange  = "";
    // 证券代码, eg: 600036
    protected String symbol    = "";
    // 证券标识, eg: sh.600036
    protected String uuid      = "";
    // 证券类型
    protected int    assetType = 0;

    // 个股基本信息
    protected StockBasic stockBasic;

    // 昨收盘
    protected float preClose = 0.00f;
    // 最高价
    protected float high     = 0.00f;
    // 开盘价
    protected float open     = 0.00f;
    // 最低价
    protected float low      = 0.00f;
    // 今收盘
    protected float close    = 0.00f;

    // 成交总量（股）
    protected long   volume = 0;
    // 成交总额（元）
    protected double amount = 0.00f;

    // 当前成交量（股）
    protected long   nowVolume = 0;
    // 当前成交额（元）
    protected double nowAmount = 0.00f;
    // 52周最高
    protected float highFor52Week = 0.00f;
    // 52周最低
    protected float lowFor52Week  = 0.00f;
    // getters and setters -------------------------------------------------------------------------------------

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getAssetType() {
        return assetType;
    }

    public void setAssetType(int assetType) {
        this.assetType = assetType;
    }

    public float getPreClose() {
        return preClose;
    }

    public void setPreClose(float preClose) {
        this.preClose = preClose;
    }

    public float getHigh() {
        return high;
    }

    public void setHigh(float high) {
        this.high = high;
    }

    public float getOpen() {
        return open;
    }

    public void setOpen(float open) {
        this.open = open;
    }

    public float getLow() {
        return low;
    }

    public void setLow(float low) {
        this.low = low;
    }

    public float getClose() {
        return close;
    }

    public void setClose(float close) {
        this.close = close;
    }

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

    public long getNowVolume() {
        return nowVolume;
    }

    public void setNowVolume(long nowVolume) {
        this.nowVolume = nowVolume;
    }

    public double getNowAmount() {
        return nowAmount;
    }

    public void setNowAmount(double nowAmount) {
        this.nowAmount = nowAmount;
    }

    public StockBasic getStockBasic() {
        return stockBasic;
    }

    public void setStockBasic(StockBasic stockBasic) {
        this.stockBasic = stockBasic;
    }

	public float getHighFor52Week() {
		return highFor52Week;
	}

	public void setHighFor52Week(float highFor52Week) {
		this.highFor52Week = highFor52Week;
	}

	public float getLowFor52Week() {
		return lowFor52Week;
	}

	public void setLowFor52Week(float lowFor52Week) {
		this.lowFor52Week = lowFor52Week;
	}
    
}
