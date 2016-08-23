package com.baidu.stock.process.fetch.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.scheduling.SchedulingAwareRunnable;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.util.Assert;

import com.baidu.stock.process.fetch.exception.ThreadContainerException;

public  class HQThreadListenerContainer extends AbstractPollingThreadListenerContainer {

	/* 线程处理器 */
	private ThreadPoolExecutor taskExecutor;
	/* 最小线程数 */
	private int concurrentConsumers = 1;
	/* 最大线程数 */
	private int maxConcurrentConsumers = 1;
	/* 单个线程执行次数 */
	private int maxMessagesPerTask =10000;
	/* 懒惰线程数 */
	private int idleTaskExecutionLimit = 1;

	/*默认间隔时间1秒*/
	private static final long DEFAULT_RECOVERY_INTERVAL = 100;
	private long interval = DEFAULT_RECOVERY_INTERVAL;

	/* 容器中所有可运行的线程体 */
	private final Set<AsyncMessageListenerInvoker> scheduledInvokers = new HashSet<AsyncMessageListenerInvoker>();

	private int activeInvokerCount = 0;

	private Runnable stopCallback;

	public Set<AsyncMessageListenerInvoker> getScheduledInvokers1() {
		return scheduledInvokers;
	}

	public void setTaskExecutor(ThreadPoolExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * 最小线程数
	 * @param concurrentConsumers
	 */
	public void setConcurrentConsumers(int concurrentConsumers) {
		Assert.isTrue(concurrentConsumers > 0, "'concurrentConsumers' value must be at least 1 (one)");
		synchronized (this.lifecycleMonitor) {
			this.concurrentConsumers = concurrentConsumers;
			if (this.maxConcurrentConsumers < concurrentConsumers) {
				this.maxConcurrentConsumers = concurrentConsumers;
			}
		}
	}


	public final int getConcurrentConsumers() {
//		synchronized (this.lifecycleMonitor) {
		 final ReentrantLock mainLock = this.mainLock;
	        mainLock.lock();
	        try{
			return this.concurrentConsumers;
	        }finally{
	        mainLock.unlock();
	        }
//		}
	}


	/**
	 * 设置最大线程数
	 * @param maxConcurrentConsumers
	 */
	public void setMaxConcurrentConsumers(int maxConcurrentConsumers) {
		Assert.isTrue(maxConcurrentConsumers > 0, "'maxConcurrentConsumers' value must be at least 1 (one)");
		synchronized (this.lifecycleMonitor) {
			this.maxConcurrentConsumers =
					(maxConcurrentConsumers > this.concurrentConsumers ? maxConcurrentConsumers : this.concurrentConsumers);
		}
	}


	public final int getMaxConcurrentConsumers() {
//		synchronized (this.lifecycleMonitor) {
		 final ReentrantLock mainLock = this.mainLock;
	        mainLock.lock();
	        try{
			return this.maxConcurrentConsumers;
	        }finally{
		        mainLock.unlock();
		        }
//		}
	}


	/**
	 * 设置单线程执行测试,建议设置较大,避免线程来回切换带来性能影响
	 * @param maxMessagesPerTask
	 */
	public void setMaxMessagesPerTask(int maxMessagesPerTask) {
		Assert.isTrue(maxMessagesPerTask != 0, "'maxMessagesPerTask' must not be 0");
		synchronized (this.lifecycleMonitor) {
			this.maxMessagesPerTask = maxMessagesPerTask;
		}
	}


	public int getMaxMessagesPerTask() {
		synchronized (this.lifecycleMonitor) {
			return this.maxMessagesPerTask;
		}
	}

	/**
	 * 设置懒惰线程数限制
	 * @param idleTaskExecutionLimit
	 */
	public void setIdleTaskExecutionLimit(int idleTaskExecutionLimit) {
		Assert.isTrue(idleTaskExecutionLimit > 0, "'idleTaskExecutionLimit' must be 1 or higher");
		synchronized (this.lifecycleMonitor) {
			this.idleTaskExecutionLimit = idleTaskExecutionLimit;
		}
	}


	public int getIdleTaskExecutionLimit() {
		synchronized (this.lifecycleMonitor) {
			return this.idleTaskExecutionLimit;
		}
	}

	public long getInterval() {
		final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try{
        	return interval;
        }finally{
	        mainLock.unlock();
	        }
	}

	public ThreadPoolExecutor getTaskExecutor() {
		return taskExecutor;
	}

	/**
	 * 设置间隔时间
	 * @param interval
	 */
	public void setIntervalMillis(long interval) {
		Assert.isTrue(interval >=1000, "'Interval' must be 1000 or higher");
		synchronized (this.lifecycleMonitor) {
			this.interval = interval;
		}
	}


	public int getActiveInvokerCount() {
		return activeInvokerCount;
	}

	/**
	 * 初始化线程体
	 */
	public void initialize() {
		// 如果没有指定第三方的线程池,则使用内部自带的.
		synchronized (this.lifecycleMonitor) {
			if (this.taskExecutor == null) {
				throw new RuntimeException("必须指定线程池配置.");
			}
			else if (this.taskExecutor instanceof SchedulingTaskExecutor &&
					((SchedulingTaskExecutor) this.taskExecutor).prefersShortLivedTasks() &&
					this.maxMessagesPerTask == Integer.MIN_VALUE) {
				this.maxMessagesPerTask = 10;
			}
		}
		super.initialize();
	}

	/**
	 * 初始化worker
	 */
	protected void doInitialize() throws ThreadContainerException {
		synchronized (this.lifecycleMonitor) {
			for (int i = 0; i < this.concurrentConsumers; i++) {
				scheduleNewInvoker();
			}
		}
	}

	protected void doShutdown() throws ThreadContainerException {
		logger.debug("Waiting for shutdown of message listener invokers");
		try {
			synchronized (this.lifecycleMonitor) {
				while (this.activeInvokerCount > 0) {
					if (logger.isDebugEnabled()) {
						logger.debug("Still waiting for shutdown of " + this.activeInvokerCount +
								" message listener invokers");
					}
					this.lifecycleMonitor.wait();
				}
			}
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * 启动
	 */
	public void start() {
		synchronized (this.lifecycleMonitor) {
			this.stopCallback = null;
		}
		super.start();
	}

	/**
	 * 停止
	 */
	public void stop(Runnable callback) throws ThreadContainerException {
		synchronized (this.lifecycleMonitor) {
			this.stopCallback = callback;
		}
		stop();
	}


	public final int getScheduledConsumerCount() {
		synchronized (this.lifecycleMonitor) {
			return this.scheduledInvokers.size();
		}
	}

/**
 * 获得活动线程计数
 * @return
 */
	public final int getActiveConsumerCount() {
		synchronized (this.lifecycleMonitor) {
			return this.activeInvokerCount;
		}
	}

	/**
 * 初始化一个线程回调,添加到内存中
 */
	private void scheduleNewInvoker() {
		AsyncMessageListenerInvoker invoker = new AsyncMessageListenerInvoker();
		
		if (rescheduleTaskIfNecessary(invoker)) {
			// This should always be true, since we're only calling this when active.
			this.scheduledInvokers.add(invoker);
		}
	}

	/**
	 * 执行线程
	 * @see #setTaskExecutor
	 */
	protected void doRescheduleTask(Object task) {
		
		this.taskExecutor.execute((Runnable) task);
	}

	/**
	 * 试图新增一个线程体,因为判断有消息需要消费
	 */
	protected void messageReceived(Object invoker) {
		((AsyncMessageListenerInvoker) invoker).setIdle(false);
		scheduleNewInvokerIfAppropriate();
		super.messageReceived(invoker);
	}

	/**
	 * 没数据接收,做标记idle=true
	 */
	protected void noMessageReceived(Object invoker) {
		((AsyncMessageListenerInvoker) invoker).setIdle(true);
		super.noMessageReceived(invoker);
	}

	/**
	 * 是否必要再次创建一个新的线程体
	 */
	protected void scheduleNewInvokerIfAppropriate() {
		if (isRunning()) {
			resumePausedTasks();
			synchronized (this.lifecycleMonitor) {
				if (this.scheduledInvokers.size() < this.maxConcurrentConsumers && getIdleInvokerCount() == 0) {
					scheduleNewInvoker();
					if (logger.isDebugEnabled()) {
						logger.debug("Raised scheduled invoker count: " + this.scheduledInvokers.size());
					}
				}
			}
		}
	}

	/**
	 * 判断当前线程体是否回收或继续接收消息
	 */
	private boolean shouldRescheduleInvoker(int idleTaskExecutionCount) {
		boolean superfluous =
				(idleTaskExecutionCount >= this.idleTaskExecutionLimit && getIdleInvokerCount() > 1);
		return (this.scheduledInvokers.size() <=
				(superfluous ? this.concurrentConsumers : this.maxConcurrentConsumers));
	}

	/**
	 * 获得懒惰线程计数
	 */
	private int getIdleInvokerCount() {
		int count = 0;
		for (AsyncMessageListenerInvoker invoker : this.scheduledInvokers) {
			if (invoker.isIdle()) {
				count++;
			}
		}
		return count;
	}

	
	/**
	 * 获得活动线程体
	 * @return
	 */
	public List<AsyncMessageListenerInvoker> getActiveThreadInvoker(){
		List <AsyncMessageListenerInvoker>list=new ArrayList<AsyncMessageListenerInvoker>();
		for (AsyncMessageListenerInvoker invoker : this.scheduledInvokers) {
			if (!invoker.isIdle()) {
				list.add(invoker);
			}
		}
		return list;
	}
	
	/**
	 * 获得非活动线程体
	 * @return
	 */
	public List<AsyncMessageListenerInvoker> getIdelThreadInvoker(){
		List <AsyncMessageListenerInvoker>list=new ArrayList<AsyncMessageListenerInvoker>();
		for (AsyncMessageListenerInvoker invoker : this.scheduledInvokers) {
			if (invoker.isIdle()) {
				list.add(invoker);
			}
		}
		return list;
	}
	

    /**
     * 等待时间设置
	 */
	protected void sleepInbetweenRecoveryAttempts() {
		if (this.interval > 0) {
			try {
				Thread.sleep(this.interval);
			}
			catch (InterruptedException interEx) {
				// Re-interrupt current thread, to allow other threads to react.
				Thread.currentThread().interrupt();
			}
		}
	}

	
	//-------------------------------------------------------------------------
	//内部线程类
	//-------------------------------------------------------------------------

	/**
	 * 循环执行该线程类
	 */
	public class AsyncMessageListenerInvoker  implements SchedulingAwareRunnable{	
		
		private int idleTaskExecutionCount = 0;

		private volatile boolean idle = true;

		private boolean lastMessageSucceeded;
		
		public void run() {
			synchronized (lifecycleMonitor) {
				activeInvokerCount++;
//				线程间隔等待时间
				sleepInbetweenRecoveryAttempts();
				lifecycleMonitor.notifyAll();
			}
			boolean messageReceived = false;
			try {
				if (maxMessagesPerTask < 0) {
					messageReceived = executeOngoingLoop();
				}
				else {
					int messageCount = 0;
					while (isRunning() && messageCount < maxMessagesPerTask) {
						messageReceived = (invokeListener() || messageReceived);
						messageCount++;
					}
				}
			}
			catch (Throwable ex) {
				ex.printStackTrace();
				if (!this.lastMessageSucceeded) {
					// We failed more than once in a row - sleep for recovery interval
					// even before first recovery attempt.
					sleepInbetweenRecoveryAttempts();
				}
				this.lastMessageSucceeded = false;
				handleListenerException(ex);
			}
			synchronized (lifecycleMonitor) {
				decreaseActiveInvokerCount();
				lifecycleMonitor.notifyAll();
			}
			if (!messageReceived) {
				this.idleTaskExecutionCount++;
			}
			else {
				this.idleTaskExecutionCount = 0;
			}
			synchronized (lifecycleMonitor) {
				if (!shouldRescheduleInvoker(this.idleTaskExecutionCount) || !rescheduleTaskIfNecessary(this)) {
					// We're shutting down completely.
					scheduledInvokers.remove(this);
					if (logger.isDebugEnabled()) {
						logger.debug("Lowered scheduled invoker count: " + scheduledInvokers.size());
					}
					lifecycleMonitor.notifyAll();
				}
				else if (isRunning()) {
					int nonPausedConsumers = getScheduledConsumerCount() - getPausedTaskCount();
					if (nonPausedConsumers < 1) {
						logger.error("所有的任务线程可能被挂起，需要手动的重启容器或回调start()方法");
					}
					else if (nonPausedConsumers < getConcurrentConsumers()) {
						logger.warn("最小并发线程已经到最低数之下了，请检测线程配置");
					}
				}
			}
		}

		/*
		 * 循环执行抓取数据，性能影响较大，不建议使用。
		 */
		private boolean executeOngoingLoop() throws ThreadContainerException {
			boolean messageReceived = false;
			boolean active = true;
			while (active) {
				synchronized (lifecycleMonitor) {
					boolean interrupted = false;
					boolean wasWaiting = false;
					while ((active = isActive()) && !isRunning()) {
						if (interrupted) {
							throw new IllegalStateException("线程中断，需要重启容器");
						}
						if (!wasWaiting) {
							decreaseActiveInvokerCount();
						}
						wasWaiting = true;
						try {
							lifecycleMonitor.wait();
						}
						catch (InterruptedException ex) {
							// Re-interrupt current thread, to allow other threads to react.
							Thread.currentThread().interrupt();
							interrupted = true;
						}
					}
					if (wasWaiting) {
						activeInvokerCount++;
					}
				}
				if (active) {
					messageReceived = (invokeListener() || messageReceived);
				}
			}
			
			return messageReceived;
		}

		private boolean invokeListener() throws ThreadContainerException {
			boolean messageReceived = receiveAndExecute(this);
			this.lastMessageSucceeded = true;
			return messageReceived;
		}

		private void decreaseActiveInvokerCount() {
			activeInvokerCount--;
			if (stopCallback != null && activeInvokerCount == 0) {
				stopCallback.run();
				stopCallback = null;
			}
		}
		
		public boolean isLongLived() {
			return (maxMessagesPerTask < 0);
		}

		public void setIdle(boolean idle) {
			this.idle = idle;
		}

		public boolean isIdle() {
			return this.idle;
		}
	}

	
	
}
