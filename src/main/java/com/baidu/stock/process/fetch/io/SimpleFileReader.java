//package com.baidu.stock.process.fetch.io;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import org.apache.log4j.Logger;
//
//import com.baidu.stock.process.util.FileUtil;
//
//
//
//public class SimpleFileReader {
//	public static Logger logger = Logger.getLogger(SimpleFileReader.class);
//	/**
//	 * fast文件路径
//	 */
//	public String fastFilePath = "";
//	
//	public SimpleFileReader(String fastFilePath){
//		this.fastFilePath = fastFilePath;
//	}
//	
//	/**
//	 * 读取 fast 文件内容
//	 * @return
//	 */
//	public List<Map<String,String>> read(){
//		
//		 File dataRaf = new File(this.fastFilePath);
//		 if(!dataRaf.exists() || !dataRaf.isFile()){
//			 logger.error("path :" + this.fastFilePath +" is not exists,please check path or change to read dbf");
//			 return null;
//		 }
//		 List<Map<String,String>> list = new ArrayList<Map<String,String>>();
//		try{
//	        BufferedReader bufReader = null;  
//	        try {  
//	            bufReader = new BufferedReader(new InputStreamReader(new FileInputStream(dataRaf),"gbk"));  
//	            String temp = null;  
//	            while ((temp = bufReader.readLine()) != null) {  
//	            	Map<String,String> map = new HashMap<String,String>();
//	            	String[] arg = temp.split("\\|");
//	            	for (int i = 0; i < arg.length; i++) {
//	            		map.put("f"+i, arg[i].trim());
//					}
//	            	list.add(map);
//	            }  
//	        } catch (Exception e) {  
//	            e.getStackTrace();  
//	        } finally {  
//	            if (bufReader != null) {  
//	                try {  
//	                    bufReader.close();  
//	                } catch (IOException e) {  
//	                    e.getStackTrace();  
//	                }  
//	            }  
//	        }  
//		} 
//		catch (Exception e) {
//            logger.error("read fastfile error:" + FileUtil.printException(e));	
//            e.printStackTrace();
//		}
//		return list;
//	}
//}
