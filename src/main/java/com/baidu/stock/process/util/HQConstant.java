package com.baidu.stock.process.util;

/**
 * 全局常数
 * <p/>
 */
public class HQConstant {
	public static final String DEFAULT_SCHEDULE_METHODNAME="execute";
    // 缓存配置文件
    public static final String CACHE_FILENAME   ="ehcache.xml";
    //缓存key
    public static final String CACHE_KEY="HQ_cache";
    // 上交所代码
    public static final String shExchangeId = "sh";
    // 深交所代码
    public static final String szExchangeId = "sz";
    
    public static final String HOLIDAYKEY = "quotes_basicinfo_holiday_a";
    
    public static final String SZMINVALUE = "99999.999";
//	// 全量补充数据发送间隔
//	public static final long sendFullSleepTime = 35000;
    // 系统时间通知
    public static final String SYSTEM_TIME_CODE = "systime";
    // 外币汇率
    public static final String FOREIGNEXCHANGE_KEY = "foreignexchange_rmb";
    // 5日平均成交量
    public static final String DAY5_AVERAGE_VOLUME_KEY = "5dayavg";
    public static final String SUM_INNER_VOLUME   = "sumInnerDisc";
    // 外盘累计
    public static final String SUM_OUTER_VOLUME   = "sumOuterDisc";
    //大户流入资金
    public static final String BIG_FUNDS_INFLOWS  = "bigInflows";
    //大户流出资金
    public static final String BIG_FUNDS_OUTFLOWS = "bigOutflows";
    //总计流入资金
    public static final String SUM_FUNDS_INFLOWS  = "sumInflows";
    //总计流出资金
    public static final String SUM_FUNDS_OUTFLOWS = "sumOutflows";
    // 总计交易笔数
    public static final String DEAL_BATCH_NUMBER = "dealBatchNumber";
    // Ask1 Price
    public static final String ASK_ONE_PRICE_KEY  = "Ask1Price";
    // Bid1 Price
    public static final String BID_ONE_PRICE_KEY  = "Bid1Price";
    // Ask1 Volume
    public static final String ASK_ONE_VOLUME_KEY = "Ask1Volume";
    // Bid1 Volume
    public static final String BID_ONE_VOLUME_KEY = "Bid1Volume";
    
    
    // 上交所 - 标志指数代码
    public static final String SH_MASTER_INDEX = "000001";
    // 深交所 - 标志指数代码
    public static final String SZ_MASTER_INDEX = "399001";
    // 深交所 - 深圳创业板
    public static final String SZC_INDEX_KEY = "399006";
    // 深交所- 沪深300指数代码
    public static final String SZ_HS300_INDEX_KEY = "399300";
    // 上交所- 沪深300指数代码
    public static final String SH_HS300_INDEX_KEY = "000300";
    // 涨跌停比例 ------------------------------------------------------------------
    public static final float RISE_LIMIT_RATIO         = 1.10f;
    public static final float RISE_LIMIT_RATIO_FOR_ST  = 1.05f;
    public static final float RISE_LIMIT_RATIO_FOR_NEW = 1.44f;

    public static final float FALL_LIMIT_RATIO         = 0.9f;
    public static final float FALL_LIMIT_RATIO_FOR_ST  = 0.95f;
    public static final float FALL_LIMIT_RATIO_FOR_NEW = 0.64f;
    
    // ST股票标志
    public static final String ST_SYMBOL_SIGN = "ST";
    // 新股股票标志
    public static final String NEW_SYMBOL_SIGN = "N";
    
    // 港交所 - 市场资金统计
    public static final String HK_MAIN_AMOUNT = "MMAIN";
    
}
