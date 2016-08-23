package com.baidu.stock.process.fetch.reader.field;

/*
 * <p/>
 * HEADER:表示文件头  MD001：表示指数行情数据格式类型  MD002：表示股票（A、B股）行情数据格式类型
 * MD003：表示债券行情数据格式类型   MD004：表示基金行情数据格式类型；
 * <p/>
 */
public enum MDStreamType {
	HEADER,
	MD001,
	MD002,
	MD003,
	MD004,
	TRAILER
}
