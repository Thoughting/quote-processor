package com.baidu.stock.process.wrap;

import com.baidu.stock.process.fetch.meta.HQSnapShot;

/**
 * 行情快照接口
 * @author dengjianli
 *
 */
public interface SnapshotHandler {
    public abstract HQSnapShot handleRequest(HQSnapShot snapShot);
    public void setNextHandler(SnapshotHandler nextHandler);
}
