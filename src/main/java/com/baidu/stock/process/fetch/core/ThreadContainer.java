package com.baidu.stock.process.fetch.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import com.baidu.stock.process.fetch.exception.ThreadContainerException;

/**
 * 线程容器基类
 * @author dengjianli
 *
 */
public abstract  class ThreadContainer implements Lifecycle,  DisposableBean {
	protected static Logger logger = LoggerFactory.getLogger(ThreadContainer.class);  
	/*默认名称前缀*/
	public static final String DEFAULT_THREAD_NAME_PREFIX ="Worker-";
   
	/*worker名称*/
	private String workName=DEFAULT_THREAD_NAME_PREFIX;
	/*描述*/
	private String workDescribution;
	
	private boolean active = false;

	private boolean running = false;
	public volatile boolean isExchangeOpenFlag=false;
	private final List<Object> pausedTasks = new LinkedList<Object>();

	protected final Object lifecycleMonitor = new Object();
	
	private boolean autoStartup = false;
	 protected final ReentrantLock mainLock = new ReentrantLock();
	/**
	 * 关闭
	 */
	public void destroy() {
		try {
			shutdown();
		} catch (ThreadContainerException e) {
		}
	}


	public void initialize() throws ThreadContainerException {
		try {
			synchronized (this.lifecycleMonitor) {
				this.active = true;
				this.lifecycleMonitor.notifyAll();
			}
			doInitialize();
			if (this.autoStartup) {
				doStart();
			}
		}
		catch (ThreadContainerException ex) {
			logger.error(ex.getCause().getMessage());
		}
	}

	/**
	 * 关闭容器(worker)
	 * @throws ThreadContainerException
	 */
	public void shutdown() throws ThreadContainerException {
		logger.debug("Shutting down syn listener container");
		synchronized (this.lifecycleMonitor) {
			this.running = false;
			this.active = false;
			this.lifecycleMonitor.notifyAll();
		}
		// Shut down the invokers.
		try {
			doShutdown();
			if(logger.isDebugEnabled()){
				logger.debug(this.getWorkName()+"关闭成功.");
			}
		}
		catch (ThreadContainerException ex) {
			logger.error(ex.getCause().getMessage());
		}
	}

	/**
	 * 判断是否处于活动状态
	 */
	public final boolean isActive() {
		synchronized (this.lifecycleMonitor) {
			return this.active;
		}
	}

	/**
	 * 启动容器(worker)
	 * @see #doStart
	 */
	public void start() {
		try {
			doStart();
		}
		catch (ThreadContainerException ex) {
			logger.error("Worker: "+this.getWorkName()+"start failue!"+ex.getMessage());
			
		}
	}

	/**
	 * 启动,初始化所有线程体
	 */
	protected void doStart() throws ThreadContainerException {
		// Reschedule paused tasks, if any.
		synchronized (this.lifecycleMonitor) {
			this.running = true;
			this.lifecycleMonitor.notifyAll();
			resumePausedTasks();
			if(logger.isDebugEnabled()){
				logger.debug(this.getWorkName()+"启动成功.");
			}
		}
	}

	/**
	 * 停止
	 * @see #doStop
	 */
	public void stop() {
		try {
			doStop();
		}
		catch (ThreadContainerException ex) {
			logger.error(ex.getMessage());
		}
	}
	/**
	 * 停止运行
	 */
	protected void doStop() throws ThreadContainerException {
		if(running){
		synchronized (this.lifecycleMonitor) {
			this.running = false;
			this.lifecycleMonitor.notifyAll();
			if(logger.isDebugEnabled()){
				logger.debug(this.getWorkName()+"停止成功.");
			}
		}
		}
	}

	/**
	 * 是否运行状态
	 */
	public final boolean isRunning() {
//		synchronized (this.lifecycleMonitor) {
		final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
	try{
		return (this.running);
      	}finally{
        mainLock.unlock();
        }
//		}
	}


	public String getWorkName() {
		return workName;
	}


	/**
	 * 容器名称(worker名称)
	 * @param workName
	 */
	public void setWorkName(String workName) {
		this.workName = workName;
	}

	public String getWorkDescribution() {
		return workDescribution;
	}


	public boolean isAutoStartup() {
		return autoStartup;
	}


	/**
	 * 设置自动启动
	 * @param autoStartup
	 */
	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}


	/**
	 * 容器描述(worker描述)
	 * @param workDescribution
	 */
	public void setWorkDescribution(String workDescribution) {
		this.workDescribution = workDescribution;
	}

	/**
	 * 如果必要,重新启动线程体
	 */
	protected final boolean rescheduleTaskIfNecessary(Object task) {
		if (this.running) {
			try {
				doRescheduleTask(task);
			}
			catch (RuntimeException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Listener container task [" + task + "] has been rejected and paused: " + ex);
				}
				this.pausedTasks.add(task);
			}
			return true;
		}
		else if (this.active) {
			this.pausedTasks.add(task);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * 试图重新启动所有初始化挂起的线程体
	 */
	protected void resumePausedTasks() {
		synchronized (this.lifecycleMonitor) {
			if (!this.pausedTasks.isEmpty()) {
				for (Iterator<?> it = this.pausedTasks.iterator(); it.hasNext();) {
					Object task = it.next();
					try {
						doRescheduleTask(task);
						it.remove();
						if (logger.isDebugEnabled()) {
							logger.debug("Resumed paused task: " + task);
						}
					}
					catch (RuntimeException ex) {
						if (logger.isDebugEnabled()) {
							logger.debug("Listener container task [" + task + "] has been rejected and paused: " + ex);
						}
					}
				}
			}
		}
	}

	public int getPausedTaskCount() {
		synchronized (this.lifecycleMonitor) {
			return this.pausedTasks.size();
		}
	}

	/**
	 * 重新执行该线程体
	 */
	protected void doRescheduleTask(Object task) {
		throw new UnsupportedOperationException(
				ClassUtils.getShortName(getClass()) + " does not support rescheduling of tasks");
	}


	protected abstract void doInitialize() throws ThreadContainerException;


	protected abstract void doShutdown() throws ThreadContainerException;


	public void setRunning(boolean running) {
		this.running = running;
	}



}
