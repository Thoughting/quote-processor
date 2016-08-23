package com.baidu.stock.process.fetch.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.io.ByteArrayOutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.process.fetch.reader.field.FastHQMessage;
import com.baidu.stock.process.fetch.reader.field.MDStreamType;
import com.baidu.stock.process.util.FileUtil;

/**
 * 并行读取文件
 * @author dengjianli
 *
 */
public class BigFileReader {
	private static Logger logger = LoggerFactory.getLogger(BigFileReader.class);  
	private String name;
	private String charset;
	private int bufferSize;
	private String fastFilePath;
	
	public BigFileReader(String name,String fastFilePath,String charset,int bufferSize){
		this.name=name;
		this.fastFilePath = fastFilePath;
		this.charset = charset;
		this.bufferSize = bufferSize;
	}
	
	/**
	 * 通过buffer读取文件
	 * @return
	 */
	public FastHQMessage readByBuffer(){
		 FastHQMessage message=new FastHQMessage();
		 File dataRaf = new File(this.fastFilePath);
		 if(!dataRaf.exists() || !dataRaf.isFile()){
			 logger.error(this.fastFilePath +"文件不存在,请检查文件.");
			 return null;
		 }
	        BufferedReader bufReader = null;  
	        try {  
	            bufReader = new BufferedReader(new InputStreamReader(new FileInputStream(dataRaf),charset));  
	            String temp = null;  
	            while ((temp = bufReader.readLine()) != null) {  
	            	Map<String,String> map = new HashMap<String,String>();
	            	if(temp!=null && !"".equals(temp)){
	            	String[] arg = temp.split("\\|");
	            	for (int i = 0; i < arg.length; i++) {
	            		if(MDStreamType.HEADER.toString().equals(arg[i].trim())){
	            				String dbfDate=arg[6].trim();
	            				message.setDbfDate(dbfDate);
	            				String exchangeState =arg[8].trim();
	            				message.setExchangeState(exchangeState);
	            		}else{
	            			map.put("f"+i, arg[i].trim());
	            		}
	    			}
	            	message.getLstHQ().add(map);
	            }  
	            }
	        } catch (Exception e) {  
				logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE ,"读取文件异常:\n "+ FileUtil.printException(e)));
	        } finally {  
	            if (bufReader != null) {  
	                try {  
	                    bufReader.close();  
	                } catch (IOException e) {  
	                	 logger.error(this.fastFilePath +"文件关闭异常:\n "+ FileUtil.printException(e));
	                }  
	            }  
	        }  
		return message;
	}
	
	/**
	 * 
	 * 通过内存映射读物文件, MappedByteBuffer是java nio引入的文件内存映射方案，读写性能极高。N
	 * @return
	 */
	public FastHQMessage readByMappedByteBufferViaFileInputStream(){
		 FastHQMessage message=new FastHQMessage();
		 File dataRaf = new File(this.fastFilePath);
		 if(!dataRaf.exists() || !dataRaf.isFile()){
			 logger.error(this.fastFilePath +"文件不存在,请检查文件.");
			 return null;
		 }
		 long fileLength=dataRaf.length();
		 byte[] readBuff = new byte[bufferSize];
		 ByteArrayOutputStream bos = null;
		 MappedByteBuffer mapBuffer=null;
		 FileInputStream in=null;
		 try{
		 in = new FileInputStream(dataRaf);  
         mapBuffer = in.getChannel().map(  FileChannel.MapMode.READ_ONLY, 0, fileLength); 
             bos = new ByteArrayOutputStream();
			for(int offset=0;offset<fileLength;offset+=bufferSize){
				int readLength;
				if(offset+bufferSize<=fileLength){
					readLength = bufferSize;
				}else{
					readLength = (int) (fileLength-offset);
				}
				mapBuffer.get(readBuff, 0, readLength);
				for(int i=0;i<readLength;i++){
					byte tmp = readBuff[i];
					if(tmp=='\n' || tmp=='\r'){
						handle(bos.toByteArray(),message);
						bos.reset();
					}else{
						bos.write(tmp);
					}
				}
			}
			if(bos.size()>0){
				handle(bos.toByteArray(),message);
			}
	        } catch (Exception e) {  
				logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_VALUE ,"读取文件异常:\n "+ FileUtil.printException(e)));
	        } finally {
				if(bos!=null){
					try {
						bos.flush();
						bos.close();
					} catch (IOException e) {
						logger.error("ByteArrayOutputStream关闭异常\n"+ FileUtil.printException(e));
					}
				}
				if(mapBuffer!=null){
					unmap(mapBuffer);	
				}
				if(in!=null){
					try {
						in.close();
					} catch (IOException e) {
						 logger.error(this.fastFilePath +"文件关闭异常:\n "+ FileUtil.printException(e));
					}
				}
		}  
		return message;
	}
	
	/**
	 * 通过内存映射读物文件, MappedByteBuffer是java nio引入的文件内存映射方案，读写性能极高。
	 * @return
	 */
	public FastHQMessage readByMappedByteBuffer(){
		 FastHQMessage message=new FastHQMessage();
		 File file = new File(this.fastFilePath);
		 long fileLength=file.length();
		 RandomAccessFile rAccessFile = null;
		 ByteArrayOutputStream bos = null;
		 byte[] readBuff = new byte[bufferSize];
		 MappedByteBuffer mapBuffer =null;
				try {
					rAccessFile = new RandomAccessFile(file,"r");
					mapBuffer = rAccessFile.getChannel().map(MapMode.READ_ONLY,0,fileLength);
					bos = new ByteArrayOutputStream();
					for(int offset=0;offset<fileLength;offset+=bufferSize){
						int readLength;
						if(offset+bufferSize<=fileLength){
							readLength = bufferSize;
						}else{
							readLength = (int) (fileLength-offset);
						}
						mapBuffer.get(readBuff, 0, readLength);
						for(int i=0;i<readLength;i++){
							byte tmp = readBuff[i];
							if(tmp=='\n' || tmp=='\r'){
								handle(bos.toByteArray(),message);
								bos.reset();
							}else{
								bos.write(tmp);
							}
						}
					}
					if(bos.size()>0){
						handle(bos.toByteArray(),message);
					}
					
				}catch (Exception e) {
					logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.DEFAULT_ERROR_KEY, LogConstant.DEFAULT_ERROR_KEY ,"读取文件异常:\n "+ FileUtil.printException(e)));
				}finally{
					if(bos!=null){
						try {
							bos.flush();
							bos.close();
						} catch (IOException e) {
							logger.error("ByteArrayOutputStream关闭异常\n"+ FileUtil.printException(e));
						}
					}
					if(mapBuffer!=null){
						unmap(mapBuffer);	
					}
					close(rAccessFile);
			}
		return message;
	}
	
	
	 /**
     * 在MappedByteBuffer释放后再对它进行读操作的话就会引发jvm crash，在并发情况下很容易发生
     * 正在释放时另一个线程正开始读取，于是crash就发生了。所以为了系统稳定性释放前一般需要检
     * 查是否还有线程在读或写
     * @param mappedByteBuffer
     */
    private void unmap(final MappedByteBuffer mappedByteBuffer) {
        try {
            if (mappedByteBuffer == null) {
                return;
            }
            mappedByteBuffer.force();
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    try {
                        Method getCleanerMethod = mappedByteBuffer.getClass().getMethod("cleaner", new Class[0]);
                        getCleanerMethod.setAccessible(true);
                        sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod .invoke(mappedByteBuffer, new Object[0]);
                        cleaner.clean();
                    } catch (Exception e) {
                    	logger.error("内存映射cleaner异常\n"+ FileUtil.printException(e));
                    }
                    return null;
                }
            });
 
        } catch (Exception e) {
        	logger.error("内存映射unmap异常\n"+ FileUtil.printException(e));
        }
    }
    
    
	public void close(RandomAccessFile rAccessFile){
		try {
			if(null!=rAccessFile){
			rAccessFile.close();
			}
		} catch (IOException e) {
			logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.QUOTE_FILE_ERROR_KEY, LogConstant.QUOTE_FILE_ERROR_VALUE ,"文件"+fastFilePath+"close异常:\n "+ FileUtil.printException(e)));
		}
	}
	
	private void handle(byte[] bytes,FastHQMessage message) throws UnsupportedEncodingException{
		String line = null;
		if(this.charset==null){
			line = new String(bytes);
		}else{
			line = new String(bytes,charset);
		}
		if(line!=null && !"".equals(line)){
			Map<String,String> map = new HashMap<String,String>();
        	String[] arg = line.split("\\|");
        	for (int i = 0; i < arg.length; i++) {
        		if(MDStreamType.HEADER.toString().equals(arg[i].trim())){
        				String dbfDate=arg[6].trim();
        				message.setDbfDate(dbfDate);
        				String exchangeState =arg[8].trim();
        				message.setExchangeState(exchangeState);
        			return;
        		}else{
        			map.put("f"+i, arg[i].trim());
        		}
			}
        	message.getLstHQ().add(map);
		}
	}

	public String getName() {
		return name;
	}
	
}
