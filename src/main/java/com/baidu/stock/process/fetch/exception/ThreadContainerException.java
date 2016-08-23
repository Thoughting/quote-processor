package com.baidu.stock.process.fetch.exception;

public  class ThreadContainerException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor that takes a message.
	 * @param msg the detail message
	 */
	public ThreadContainerException(String msg) {
		super(msg);
	}

	/**
	 * 异常处理
	 */
	public ThreadContainerException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * 异常处理
	 */
	public ThreadContainerException(Throwable cause) {
		super(cause != null ? cause.getMessage() : null, cause);
	}


}
