package com.baidu.stock.process.config.setting;

/**
 * 
 * @author dengjianli
 *
 */
public class BaseDataSource {
	private String exchange;
	//1/2/3表示优先级高到低
	private String file_1;
	private String file_2;
	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	public String getFile_1() {
		return file_1;
	}
	public void setFile_1(String file_1) {
		this.file_1 = file_1;
	}
	public String getFile_2() {
		return file_2;
	}
	public void setFile_2(String file_2) {
		this.file_2 = file_2;
	}
	
}
