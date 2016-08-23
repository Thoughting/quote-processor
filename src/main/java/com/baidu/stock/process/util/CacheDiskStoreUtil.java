package com.baidu.stock.process.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;

/**
 * 
 * @author dengjianli
 *
 */
public class CacheDiskStoreUtil {
	private static Logger logger = LoggerFactory.getLogger(CacheDiskStoreUtil.class);
	private static SimpleDateFormat dateformat=new SimpleDateFormat("yyyy-MM-dd");
	
	public static void writeCache2Disk(Properties pres){
		FileWriter fw=null;
		 try {
			   URL url= CacheDiskStoreUtil.class.getClassLoader().getResource("cache/cache.data");
			   File file=null;
			   if(url==null){
				URL root= CacheDiskStoreUtil.class.getClassLoader().getResource("ehcache.xml");
				file=new File(root.getPath());
				String cachePath=file.getParentFile().getPath()+"/cache/cache.data";
				file=new File(cachePath);
				if(!file.getParentFile().exists()){
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			   }else{
				   file=new File(url.getPath());  
			   }
			   if(file.exists()){
			   fw=new FileWriter(file);
			   pres.store(fw, dateformat.format(new Date())+"[sumInnerDisc|sumOuterDisc|bigInflows|bigOutflows]");
			   logger.info("间隔120秒存储临时缓存持久到文件cache/cache.data完成.");
				}
			  } catch (IOException e) {
				logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"存储临时缓存写入文件异常\n:"+ FileUtil.printException(e)));
			  }finally{
				  if(fw!=null){
					  try {
						fw.flush();
						fw.close();
					} catch (IOException e) {
						logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"写入缓存导致关闭存储临时缓存写入文件异常\n:"+ FileUtil.printException(e)));
					} 
				  }
			  }
	}
	
	public static Properties readCacheFromDisk() throws Exception{
		 Properties prs=new Properties();
			  URL url= CacheDiskStoreUtil.class.getClassLoader().getResource("cache/cache.data");
			  if(url!=null){
			  File file=new File(url.getPath());
			  if(file.exists()){
		       FileReader fr=new FileReader(url.getPath()); 
		       prs.load(fr); 
		       fr.close();
			  }
			  }
			return prs;
	}
	
	
	public static void cleanDiskCache(){
			  Properties pres=new Properties();
			  FileWriter fw=null;
			  try {
				  URL url= CacheDiskStoreUtil.class.getClassLoader().getResource("cache/cache.data");
				  if(url!=null){
				  File file=new File(url.getPath());
				  if(file.exists()){
					   fw=new FileWriter(url.getPath()); 
					   pres.store(fw, dateformat.format(new Date())+"[sumInnerDisc|sumOuterDisc|bigInflows|bigOutflows]"); 
					   logger.info("清理存储临时缓存文件cache/cache.data完成.");
				  }else{
					   logger.warn(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"没有发现存储临时缓存文件cache/cache.data"));
				  }
				  }
			  } catch (IOException e) {
				  logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"清理存储临时缓存文件异常\n:"+ FileUtil.printException(e)));
			  } finally{
				  if(fw!=null){
					  try {
						fw.flush();
						fw.close();
					} catch (IOException e) {
						  logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE,"清理操作导致关闭存储临时缓存写入文件异常\n"+ FileUtil.printException(e)));
					} 
			  }
			  }
	}
}
