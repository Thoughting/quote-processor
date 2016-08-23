package com.baidu.stock.process.wrap;


/**
 * 行情数据处理，采用责任链模式处理
 * @author dengjianli
 *
 */
public abstract class AbstractSnapshotHandler implements SnapshotHandler{
    
    protected SnapshotHandler nextHandler;
    protected volatile static  int riseCount = 0;
    protected volatile static int fallCount = 0;
    protected volatile static int fairCount = 0;
    
    protected volatile SumAmountResult  result399001=new SumAmountResult();
    protected volatile SumAmountResult  result399006=new SumAmountResult();
	public SnapshotHandler getNextHandler() {
		return nextHandler;
	}
	public void setNextHandler(SnapshotHandler nextHandler) {
		this.nextHandler = nextHandler;
	}
	public void setRiseCount(int riseCount) {
		AbstractSnapshotHandler.riseCount = riseCount;
	}
	public void setFallCount(int fallCount) {
		AbstractSnapshotHandler.fallCount = fallCount;
	}
	public void setFairCount(int fairCount) {
		AbstractSnapshotHandler.fairCount = fairCount;
	}
	
	 public void setResult399001(double amount,long volume) {
		 this.result399001.setAmount(amount);
		 this.result399001.setVolume(volume);
	}
	public void setResult399006(double amount,long volume) {
		 this.result399006.setAmount(amount);
		 this.result399006.setVolume(volume);
	}


	public class SumAmountResult{
	    	private double amount=0;
	        private long volume=0;
	        public SumAmountResult(){}
	        public SumAmountResult(double amount, long volume){
	        	this.amount=amount;
	        	this.volume=volume;
	        }
			public double getAmount() {
				return amount;
			}
			public void setAmount(double amount) {
				this.amount = amount;
			}
			public long getVolume() {
				return volume;
			}
			public void setVolume(long volume) {
				this.volume = volume;
			}
	    }
    
}