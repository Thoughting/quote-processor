package com.baidu.stock.quote.process.asyn;


import com.baidu.stock.process.asyn.HQAsynWriterTemplate;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.quote.asyn.AsynConfig;

public class TestAsyn {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int writerCount=AsynConfig.getInstance().getWriterCount();
		final HQAsynWriterTemplate template=new HQAsynWriterTemplate(writerCount, null);
		 new Thread(new Runnable() {
			@Override
			public void run() {
				int i=0;
				while(true){
				HQSnapShot snapshot=new HQSnapShot(); 
				snapshot.setStockCode(String.valueOf(i++));
				if(i/2==0){
				snapshot.setExchange("sh");
				}else{
					snapshot.setExchange("sz");
				}
				template.write(snapshot);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				}
			}
		}).start();
		
	}
}
