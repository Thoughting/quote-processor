package com.baidu.stock.process.fetch;

import java.util.List;

/**
 * 
 * @author dengjianli
 *
 */
public interface IFetchAction<T> {
	public abstract List<T> doFetch();
}
