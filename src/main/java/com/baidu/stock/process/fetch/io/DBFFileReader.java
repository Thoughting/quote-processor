package com.baidu.stock.process.fetch.io;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.process.fetch.reader.field.DBFField;
import com.baidu.stock.process.util.FileUtil;
import com.baidu.stock.process.util.ReadUtil;


public class DBFFileReader {
	public static Logger logger = Logger.getLogger(DBFFileReader.class);
	public String name = "";
	/**
	 * dbf 表头长度
	 */
	public short tableHeadLen = 0;
	/**
	 * dbf 记录长度
	 */
	public int recordLen = 0;
	/**
	 * 总记录数
	 */
	public int recordCount = 0;
	/**
	 * dbf文件路径
	 */
	public String dbfFilePath = "";
	/**
	 * dbf 字段列表
	 */
	public List<DBFField> fieldVec = null;
	public RandomAccessFile infoRaf = null;
	/**
	 * dbf 文件路径
	 */
	public RandomAccessFile dataRaf = null;
	
	private boolean inited = false;
	
	public DBFFileReader(String name,String dbfFilePath){
		this.name = name;
		this.dbfFilePath=dbfFilePath;
	}
	
	/**
	 * 初始化
	 * @return
	 */
	public boolean init()
	{
		
		inited = initHeadTable();
		
		return inited;
	}
	
	public void setInited(boolean b)
	{
		this.inited = b;
	}
	
	
	public boolean close()
	{
		try
		{
			if(infoRaf != null)
			{
				infoRaf.close();
			}
			if(dataRaf != null)
			{
				dataRaf.close();
			}
			infoRaf = null;
			return true;
		}
		catch(Exception ex)
		{
			return false;
		}
	}
	
	/**
	 * 初始化dbf表头
	 * @return
	 */
	protected boolean initHeadTable(){
		try {
			byte[] dbfBuffer = new byte[32];
			infoRaf = new RandomAccessFile(this.dbfFilePath, "r");
			infoRaf.read(dbfBuffer);
			recordCount = ReadUtil.readInt(dbfBuffer, 4);
			tableHeadLen = (short)ReadUtil.readShort(dbfBuffer, 8);
			recordLen = ReadUtil.readShort(dbfBuffer, 10);
			initField(infoRaf);
		} catch (Exception e){
			logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.QUOTE_FILE_ERROR_KEY, LogConstant.QUOTE_FILE_ERROR_VALUE ,dbfFilePath+"初始化文件异常:\n"+ FileUtil.printException(e)));
			return false;
		}
		finally{
			if(infoRaf != null){
				try{
					infoRaf.close();
				}catch(Exception e){}
			}
		}
		return true;
	}
	
	/**
	 * 初始化dbf 字段
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public List<DBFField> initField(RandomAccessFile inputStream) throws IOException{
		fieldVec = new ArrayList<DBFField>();
		DBFField field = getDBFField(inputStream);
		while (field != null)
		{
			fieldVec.add(field);
			field = getDBFField(inputStream);
		}
		return fieldVec;
	}
	
	/**
	 * 获取dbf字段
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static DBFField getDBFField(RandomAccessFile inputStream) throws IOException{
		DBFField field = new DBFField();
		byte[] tempFieldName = new byte[11];
		tempFieldName[0] = inputStream.readByte();
		if (tempFieldName[0] == (byte) 0x0d || tempFieldName[0] == (byte) 0x00)
		{
			return null;
		}
		inputStream.read(tempFieldName, 1, 10);
		for (int i = 0; i < tempFieldName.length; i++)
		{
			if (tempFieldName[i] == (byte) 0x00)
			{
				field.fieldName = new String(tempFieldName).substring(0, i);
				break;
			}
		}
		inputStream.skipBytes(5);
		field.fieldLength = inputStream.readByte();
		field.decimalCount = inputStream.readByte();
		inputStream.skipBytes(14);
		return field;
	}
	
	/**
	 * 读取dbf 数据
	 * @return
	 */
	public List<Map<String,String>> read() {
		 List<Map<String,String>> lstDatas = new ArrayList<Map<String,String>>();
		try{
			if(inited == false){
				init();
			}
			dataRaf = new RandomAccessFile(this.dbfFilePath, "r");
			dataRaf.seek(this.tableHeadLen);
			int dataStartIndex = this.tableHeadLen - (32+32*this.fieldVec.size()) - 1;
			if(dataStartIndex > 0){
				dataRaf.seek(dataStartIndex);
			}
			byte[] dataBuffer = new byte[this.recordCount * this.recordLen];
			dataRaf.read(dataBuffer,0,dataBuffer.length);
			ByteBuffer scanBuffer = ByteBuffer.allocateDirect(this.recordCount * this.recordLen);
			scanBuffer.put(dataBuffer);
			for(int i = 0; i < this.recordCount;i++){
				scanBuffer.position(1 + i * this.recordLen);
				Map<String,String> m = new HashMap<String,String>();
				for(int j = 0; j < this.fieldVec.size();j++){
					int fieldLen = this.fieldVec.get(j).fieldLength;
					byte[] fileValue = new byte[fieldLen];
					scanBuffer.get(fileValue);
					String strFieldValue = new String(fileValue,"GBK");
					m.put(this.fieldVec.get(j).fieldName, strFieldValue.trim());
				}
				lstDatas.add(m);
			}
		} 
		catch (Exception e) {
			logger.error(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE,LogConstant.QUOTE_FILE_ERROR_KEY, LogConstant.QUOTE_FILE_ERROR_VALUE ,dbfFilePath+"读取dbf文件异常:\n"+ FileUtil.printException(e)));
		}
		finally{
			if(dataRaf != null){
				try{
					dataRaf.close();
				}catch(Exception ex){}
			}
			dataRaf = null;
		}
		return lstDatas;
	}

	public String getName() {
		return name;
	}
	
}
