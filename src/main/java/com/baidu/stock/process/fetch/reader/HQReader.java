package com.baidu.stock.process.fetch.reader;

import java.util.List;
import java.util.Map;
import com.baidu.stock.process.fetch.meta.HQSnapShot;

/**
 * 数据源处理接口
 * @author dengjianli
 *
 */
public interface HQReader{
	public abstract HQSnapShot readSSHQValue(Map<String, String> m);
	public abstract List<HQSnapShot> process();
	public abstract List<HQSnapShot> preFetchHQ();
	public abstract boolean isFileDataIsRefresh();
	public abstract void checkRefresh();
	public abstract String getHQurl();
}
