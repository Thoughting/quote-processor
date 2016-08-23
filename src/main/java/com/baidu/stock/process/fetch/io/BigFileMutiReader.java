package com.baidu.stock.process.fetch.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
public class BigFileMutiReader {
	private static Logger logger = LoggerFactory.getLogger(BigFileMutiReader.class);  
	private int threadSize;
	private String charset;
	private int bufferSize;
	private ExecutorService  executorService;
	private long fileLength;
	private Set<StartEndPair> startEndPairs;
	private String fastFilePath;
	
	public BigFileMutiReader(String fastFilePath,String charset,int bufferSize,int threadSize){
		this.fastFilePath = fastFilePath;
		this.charset = charset;
		this.bufferSize = bufferSize;
		this.threadSize = threadSize;
		this.executorService = Executors.newFixedThreadPool(threadSize);
		startEndPairs = new HashSet<BigFileMutiReader.StartEndPair>();
	}
	
	/**
	 * fast解析和dbf解析返回的结果不一样是因为fast解析是多线程优化的,
	 * @return
	 */
	public FastHQMessage read(){
		 FastHQMessage message=new FastHQMessage();
		 File file = new File(this.fastFilePath);
		 this.fileLength=file.length();
		 long everySize = fileLength/this.threadSize;
		 RandomAccessFile rAccessFile = null;
		 List<Future<String>>lstFutrue=new ArrayList<Future<String>>();
		try {
			rAccessFile = new RandomAccessFile(file,"r");
			calculateStartEnd(rAccessFile,0, everySize);
			//故意分片数量在加1,表示当前在等待
		for(StartEndPair pair:startEndPairs){
			Future<String>future=this.executorService.submit(new SliceReaderTask(pair,message,rAccessFile));
			lstFutrue.add(future);
		}
		CountDownLatch latch=new CountDownLatch(lstFutrue.size());
		while(latch.getCount()!=0){
			for(Future<String> future:lstFutrue){
				try {
				if(!future.isDone() && !future.isCancelled()){
					String res=future.get(5, TimeUnit.SECONDS);
					if(!"ok".equals(res)){
						return null;
					}
				}
				} catch (InterruptedException e) {
					logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.QUOTE_FILE_ERROR_KEY, LogConstant.QUOTE_FILE_ERROR_VALUE ,fastFilePath+"文件读取线程中断异常:\n"+ FileUtil.printException(e)));
				} catch (ExecutionException e) {
					logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.QUOTE_FILE_ERROR_KEY, LogConstant.QUOTE_FILE_ERROR_VALUE ,fastFilePath+"文件读取线程执行异常:\n"+ FileUtil.printException(e)));
				} catch (TimeoutException e) {
					logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.QUOTE_FILE_ERROR_KEY, LogConstant.QUOTE_FILE_ERROR_VALUE ,fastFilePath+"文件读取超时异常:\n"+ FileUtil.printException(e)));
				}finally{
					latch.countDown();
					future.cancel(true);
				}
				}
			}
			} catch (FileNotFoundException e) {
				logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.QUOTE_FILE_ERROR_KEY, LogConstant.QUOTE_FILE_ERROR_VALUE ,fastFilePath+"文件不存在:\n"+ FileUtil.printException(e)));
				return null;
			} catch (IOException e) {
				logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.QUOTE_FILE_ERROR_KEY, LogConstant.QUOTE_FILE_ERROR_VALUE ,fastFilePath+"文件读取操作异常:\n"+ FileUtil.printException(e)));
				return null;
			}finally{
				close(rAccessFile);
			}
		return message;
	}
	
	private void calculateStartEnd(RandomAccessFile rAccessFile,long start,long size) throws IOException{
		if(start>fileLength-1){
			return;
		}
		StartEndPair pair = new StartEndPair();
		pair.start=start;
		long endPosition = start+size-1;
		if(endPosition>=fileLength-1){
			pair.end=fileLength-1;
			startEndPairs.add(pair);
			return;
		}
		rAccessFile.seek(endPosition);
		byte tmp =(byte) rAccessFile.read();
		while(tmp!='\n' && tmp!='\r'){
			endPosition++;
			if(endPosition>=fileLength-1){
				endPosition=fileLength-1;
				break;
			}
			rAccessFile.seek(endPosition);
			tmp =(byte) rAccessFile.read();
		}
		pair.end=endPosition;
		startEndPairs.add(pair);
		calculateStartEnd(rAccessFile,endPosition+1, size);
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
	
	@Deprecated
	public void shutdown(){
		try {
			this.executorService.shutdown();
		} catch (Exception e) {
			logger.error("线程池关闭异常",e);
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
	private static class StartEndPair{
		public long start;
		public long end;
		
		@Override
		public String toString() {
			return "star="+start+";end="+end;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (end ^ (end >>> 32));
			result = prime * result + (int) (start ^ (start >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StartEndPair other = (StartEndPair) obj;
			if (end != other.end)
				return false;
			if (start != other.start)
				return false;
			return true;
		}
		
	}
	private class SliceReaderTask implements Callable<String>{
		private long start;
		private long sliceSize;
		private byte[] readBuff;
		private FastHQMessage hqMessage;
		private RandomAccessFile rAccessFile;
		/**
		 * @param start 	read position (include)
		 * @param end 	the position read to(include)
		 */
		public SliceReaderTask(StartEndPair pair,FastHQMessage hqMessage,RandomAccessFile rAccessFile) {
			this.start = pair.start;
			this.sliceSize = pair.end-pair.start+1;
			this.readBuff = new byte[bufferSize];
			this.hqMessage=hqMessage;
			this.rAccessFile=rAccessFile;
		}

		@Override
		public String call() throws Exception {
			try {
				MappedByteBuffer mapBuffer = rAccessFile.getChannel().map(MapMode.READ_ONLY,start, this.sliceSize);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				for(int offset=0;offset<sliceSize;offset+=bufferSize){
					int readLength;
					if(offset+bufferSize<=sliceSize){
						readLength = bufferSize;
					}else{
						readLength = (int) (sliceSize-offset);
					}
					mapBuffer.get(readBuff, 0, readLength);
					for(int i=0;i<readLength;i++){
						byte tmp = readBuff[i];
						if(tmp=='\n' || tmp=='\r'){
							handle(bos.toByteArray(),hqMessage);
							bos.reset();
						}else{
							bos.write(tmp);
						}
					}
				}
				if(bos.size()>0){
					handle(bos.toByteArray(),hqMessage);
				}
			}catch (Exception e) {
				logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.QUOTE_FILE_ERROR_KEY, LogConstant.QUOTE_FILE_ERROR_VALUE ,"读取文件异常:\n "+ FileUtil.printException(e)));
				return "fail";
			}
			return "ok";
		}
		
	}
}
