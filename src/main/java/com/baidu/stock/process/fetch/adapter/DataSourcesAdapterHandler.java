package com.baidu.stock.process.fetch.adapter;

import java.util.List;

/**
 * 
 * @author dengjianli
 *
 * @param <T>
 */
public interface DataSourcesAdapterHandler<T> {

	public List<T> fetchHQ();
}