package com.baidu.stock.process.fetch.meta;

import java.io.Serializable;


/**
 * 报价关系
 * <p/>
 * Created by xugang on 2014/10/02
 */
public class Quote implements Serializable {
	private static final long serialVersionUID = 1L;
	private float price  = 0.00f;
    private long  volume = 0L;

    public Quote() {
        price = 0.00f;
        volume = 0L;
    }

    public Quote(float _price, long _volume) {
        price = _price;
        volume = _volume;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }
}
