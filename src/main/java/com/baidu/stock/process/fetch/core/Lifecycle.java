package com.baidu.stock.process.fetch.core;


public interface Lifecycle{

	public void start();

	public void stop();

	public boolean isRunning();

}
