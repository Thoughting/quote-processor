package com.baidu.stock.process.util;


import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 文件处理类
 * @author v_longxi
 *
 */
public class FileUtil{
	
	public static String printException(Exception exception){
		StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);		                 
        exception.printStackTrace(printWriter);
        return stringWriter.toString(); 
	}
}
