
package com.baidu.stock.process.fetch.core;

public interface DisposableBean {

	/**
	 * 关闭
	 */
	public void destroy() throws Exception;

}
