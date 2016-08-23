package com.baidu.stock.quote.process.worker;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.baidu.stock.process.config.HQGloableConfig;
import com.baidu.stock.process.fetch.adapter.DataSourcesAdapterHandler;
import com.baidu.stock.process.fetch.adapter.impl.SHHQDataSourcesHandler;
import com.baidu.stock.process.fetch.core.HQThreadListenerContainer;
import com.baidu.stock.process.fetch.meta.HQSnapShot;
import com.baidu.stock.quote.protocbuf.SnapShotProto;
import com.baidu.stock.quote.protocbuf.SnapShotProto.SnapShot.Builder;

public abstract class WorkerTest {
	static ThreadPoolExecutor taskExecutor = new ThreadPoolExecutor(1, 1, 300,TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(2),new ThreadPoolExecutor.DiscardPolicy());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final HQThreadListenerContainer  hqThreadListenerContainer= new HQThreadListenerContainer();
		hqThreadListenerContainer.setConcurrentConsumers(1);
		hqThreadListenerContainer.setMaxConcurrentConsumers(1);
		hqThreadListenerContainer.setTaskExecutor(taskExecutor);
		hqThreadListenerContainer.setDataSourcesAdapterHandler(new Handlerimpl());
		hqThreadListenerContainer.initialize();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				hqThreadListenerContainer.stop();
				System.out.println("stop....");
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			hqThreadListenerContainer.start();
			System.out.println("start....");
		}
			}
		}).start();

	}

	
	static class Handlerimpl implements DataSourcesAdapterHandler<HQSnapShot> {
		@Override
		public List<HQSnapShot> fetchHQ() {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
	}
}
