package com.baidu.stock.process.context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.PropertyResolver;
import com.baidu.stock.jms.Producer;
import com.baidu.stock.log.LogConstant;
import com.baidu.stock.log.LogUtils;
import com.baidu.stock.material.domain.IndexComponent;
import com.baidu.stock.material.domain.StockExchangeRate;
import com.baidu.stock.material.domain.StockFinance;
import com.baidu.stock.material.domain.StockStopInfo;
import com.baidu.stock.material.facade.MaterialManager;
import com.baidu.stock.process.asyn.HQAsynWriterTemplate;
import com.baidu.stock.process.cache.AssistDataCache;
import com.baidu.stock.process.cache.BigFundSDataCache;
import com.baidu.stock.process.cache.FinacialInfoCache;
import com.baidu.stock.process.cache.InOutDataCache;
import com.baidu.stock.process.cache.IndexBagCache;
import com.baidu.stock.process.cache.StockStatusCache;
import com.baidu.stock.process.cache.mannage.HQCacheManager;
import com.baidu.stock.process.channel.HQExecutorOutChannel;
import com.baidu.stock.process.channel.HQDirectChannel;
import com.baidu.stock.process.config.ApplicationSettings;
import com.baidu.stock.process.config.HQGloableConfig;
import com.baidu.stock.process.config.setting.BaseDataSource;
import com.baidu.stock.process.config.setting.MarketTime;
import com.baidu.stock.process.fetch.adapter.impl.SHHQDataSourcesHandler;
import com.baidu.stock.process.fetch.adapter.impl.SZHQDataSourcesHandler;
import com.baidu.stock.process.fetch.core.HQThreadListenerContainer;
import com.baidu.stock.process.fetch.listener.HQDataSourceListener;
import com.baidu.stock.process.fetch.reader.HQReader;
import com.baidu.stock.process.fetch.reader.impl.SHFastProcessor;
import com.baidu.stock.process.fetch.reader.impl.SZProcessor;
import com.baidu.stock.process.handler.HQOutboundHandler;
import com.baidu.stock.process.handler.HQProcessHandler;
import com.baidu.stock.process.handler.StockCodeListHandler;
import com.baidu.stock.process.monitor.metrics.MetricsManager;
import com.baidu.stock.process.monitor.zk.ZkMonitorManager;
import com.baidu.stock.process.util.CacheDiskStoreUtil;
import com.baidu.stock.process.util.CommonHelp;
import com.baidu.stock.process.util.HQConstant;
import com.baidu.stock.process.wrap.HQDataProcessor;
import com.baidu.stock.process.wrap.SnapshotHandler;
import com.baidu.stock.process.wrap.impl.FinacialInfoHandler;
import com.baidu.stock.process.wrap.impl.FundsFlowHandler;
import com.baidu.stock.process.wrap.impl.MetricHandler;
import com.baidu.stock.quote.asyn.AsynConfig;

/**
 * 行情数据源处理上下文配置
 * 
 * @author dengjianli
 *
 */
public class HQProcessContext implements InitializingBean, ApplicationContextAware {
    private static Logger logger = LoggerFactory.getLogger(HQProcessContext.class);
    private GenericApplicationContext applicationContext;
    private PropertyResolver propertyResolver;
    @Resource
    private HQExecutorOutChannel stockCodeExecutorOutChannel;
    @Resource
    private HQDirectChannel hqDirectChannel;
    @Resource
    private ApplicationSettings applicationSettings;
    // 行情加工订阅处理
    private ThreadPoolExecutor hqProcessTaskExecutor;
    public static int producerNum = 5;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (GenericApplicationContext) applicationContext;
        this.propertyResolver = applicationContext.getEnvironment();
        Producer snapshotProducer = (Producer) applicationContext.getBean("snapshotProducer");
        Producer notifyProducer = (Producer) applicationContext.getBean("notifyProducer");
        CommonHelp.setSnapshotProducer(snapshotProducer);
        CommonHelp.setNotifyProducer(notifyProducer);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 注册zk状态
        ZkMonitorManager.initZkMonitor();
        // 参数加载和解析
        injectConfig(propertyResolver);
        // 初始化缓存管理
        HQCacheManager.initCacheManagerInstance();
        // 注册管道
        assembleChannel();
        // 初始化加载基础数据
        initLoadMaterial();
        // 启动日志监控
        MetricsManager.startLoggerMonitor();
        // 加载盘间因重启导致jvm某些特殊缓存,包括:内外盘和大基金流向
        loaddiskPersistentCache();
        // 启动行情源监听器worker
        startHQSourcesListener();
    }

    /**
     * 加载配置参数
     * 
     * @param propertyResolver
     * @return
     */
    private void injectConfig(PropertyResolver propertyResolver) {
        MarketTime marketTime = applicationSettings.getMarketTime();
        // 重新构造部分属性值,对外没有配置,需要特殊处理
        String exchageInitTime = marketTime.getExchageInitTime();
        applicationSettings.getMarketTime().setExchageInitTimeForLong(
                Long.valueOf(exchageInitTime.replaceAll(":", "") + "00"));
        String transactionAMTime = marketTime.getTransactionAMTime();
        String transactionPMTime = marketTime.getTransactionPMTime();
        applicationSettings.getMarketTime().setAmTime(Long.valueOf(transactionAMTime.replaceAll(":", "") + "00"));
        applicationSettings.getMarketTime().setPmTime(Long.valueOf(transactionPMTime.replaceAll(":", "") + "00"));
        String auctionTime = marketTime.getAuctionTime();
        applicationSettings.getMarketTime().setAuctionStartTimeForLong(
                Long.valueOf(auctionTime.replaceAll(":", "") + "00"));
        String lunchbreakTime = marketTime.getLunchbreakTime();
        applicationSettings.getMarketTime().setLunchbreakTimeForLong(
                Long.valueOf(lunchbreakTime.replaceAll(":", "") + "00"));
        String closedTime = marketTime.getClosedTime();
        applicationSettings.getMarketTime().setClosedTimeForLong(Long.valueOf(closedTime.replaceAll(":", "") + "00"));
        String stopSendFullHQTime = marketTime.getStopSendFullHQTime();
        applicationSettings.getMarketTime().setStopSendFullHQTimeForLong(
                Long.valueOf(stopSendFullHQTime.replaceAll(":", "") + "00"));
        String closedAuctionTime = marketTime.getClosedAuctionTime();
        applicationSettings.getMarketTime().setClosedAuctionTimeForLong(
                Long.valueOf(closedAuctionTime.replaceAll(":", "") + "00"));
        CommonHelp.materialManagerUrl = applicationSettings.getBaseConfig().getMaterialURL();
        HQGloableConfig.propertyResolver = propertyResolver;
        HQGloableConfig.applicationSettings = applicationSettings;
        logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE, LogConstant.DEFAULT_INFO_KEY,
                LogConstant.DEFAULT_INFO_VALUE, "加载配置文件applicaation.yaml解析参数完成."));
    }

    /**
     * 初始化依赖数据
     * 
     * @param materialManagerUrl
     */
    private void initLoadMaterial() {
        MaterialManager materialManager = new MaterialManager(CommonHelp.materialManagerUrl);
        // 初始化节假日信息
        Set<String> hilidayset = materialManager.getHolidays(null);
        if (hilidayset != null && hilidayset.size() > 0) {
            CommonHelp.holidayset.addAll(hilidayset);
            logger.info("初始化-节假日信息");
        } else {
            logger.warn("初始化-节假日信息列表为空");
        }
        // 初始化财务信息 key:市场_股票编码 [sh_600000]
        Map<String, StockFinance> financesMap = materialManager.getStockFinances(null);
        if (null != financesMap && financesMap.size() > 0) {
            for (String key : financesMap.keySet()) {
                StockFinance stockFinance = financesMap.get(key);
                FinacialInfoCache.add(key, stockFinance);
            }
            logger.info("初始化-财务信息");
        } else {
            logger.warn("初始化-财务信息为空");
        }

        // 汇率
        StockExchangeRate stockExchangeRate = materialManager.getForeignExchange(null);
        if (stockExchangeRate != null) {
            AssistDataCache.add(HQConstant.FOREIGNEXCHANGE_KEY, "USD", stockExchangeRate.getUsd());
            AssistDataCache.add(HQConstant.FOREIGNEXCHANGE_KEY, "HKDOLLAR", stockExchangeRate.getHkd());
            logger.info("初始化-汇率信息");
        } else {
            logger.warn("初始化-汇率为空");
        }
        // 5日均价
        Map<String, Long> fiveVolumeMap = materialManager.getStockMaFiveVolume(null);
        if (null != fiveVolumeMap && fiveVolumeMap.size() > 0) {
            for (String key : fiveVolumeMap.keySet()) {
                Long value = fiveVolumeMap.get(key);
                AssistDataCache.add(key, HQConstant.DAY5_AVERAGE_VOLUME_KEY, value.doubleValue());
            }
            logger.info("初始化-5日均价");
        } else {
            logger.warn("初始化-5日均价列表为空");
        }

        List<StockStopInfo> lstStopStock = materialManager.getStockStopInfos(null);
        if (lstStopStock != null && lstStopStock.size() > 0) {
            for (StockStopInfo ssi : lstStopStock) {
                String key = CommonHelp.compositID(ssi.getExchange(), ssi.getStockCode());
                StockStatusCache.add(key, ssi);
            }
            logger.info("初始化-停牌股票列表");
        } else {
            logger.warn("初始化-停牌股票列表为空");
        }

        Map<String, IndexComponent> indexBag = materialManager.getIndexComponents(null);
        if (indexBag != null && indexBag.size() > 0) {
            for (String key : indexBag.keySet()) {
                IndexComponent indexComponent = indexBag.get(key);
                IndexBagCache.add(key, indexComponent);
            }
            logger.info("初始化-指数的成份股列表");
        } else {
            logger.warn("初始化-指数的成份股列表为空");
        }
    }

    /**
     * 拼装channel
     */
    private void assembleChannel() {
        // 股票代码订阅处理
        stockCodeExecutorOutChannel.subscribe(new StockCodeListHandler());
        // 行情输出订阅处理
        // 构造2个异步写入MQ的channel
        List<HQExecutorOutChannel> lstExecutorChannel = new ArrayList<HQExecutorOutChannel>();
        int writerCount = AsynConfig.getInstance().getWriterCount();
        if (writerCount < producerNum || ((writerCount % producerNum) != 0)) {
            throw new RuntimeException("asynConfig.properties的asyn.writerCount参数必须大于" + producerNum + "且整除.");
        }
        // 默认写死5个生产者
        for (int i = 0; i < producerNum; i++) {
            // 线程数1,同步处理
            ThreadPoolExecutor executor =
                    new ThreadPoolExecutor(1, 1, 300, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10240),
                            new ThreadPoolExecutor.DiscardOldestPolicy());
            HQExecutorOutChannel executorChannel = new HQExecutorOutChannel(executor);
            executorChannel.setComponentName("异步消息写入Channel(MQ)-" + i);
            executorChannel.subscribe(new HQOutboundHandler());
            lstExecutorChannel.add(executorChannel);
        }
        // 行情加工订阅处理
        hqProcessTaskExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        // 行情数据处理，采用责任链模式处理
        SnapshotHandler snapshotHandler = new FinacialInfoHandler();
        SnapshotHandler metricHandler = new MetricHandler();
        metricHandler.setNextHandler(new FundsFlowHandler());
        snapshotHandler.setNextHandler(metricHandler);
        hqDirectChannel.subscribe(new HQProcessHandler(new HQDataProcessor(hqProcessTaskExecutor, snapshotHandler),
                lstExecutorChannel, writerCount));
    }

    /**
     * 启动行情监听容器,包括：sz和sh
     */
    private void startHQSourcesListener() {
        // 启动sh和sz
        List<BaseDataSource> lstDataSource = applicationSettings.getDataSource().getLst();
        if (null == lstDataSource || lstDataSource.size() == 0) {
            lstDataSource = new ArrayList<BaseDataSource>();
            lstDataSource.add(applicationSettings.getDataSource());
        }
        for (BaseDataSource dataSource : lstDataSource) {
            HQThreadListenerContainer hqThreadListenerContainer = new HQThreadListenerContainer();
            // 建议写死，暂时不暴漏出去，默认单线程抓取数据
            ThreadPoolExecutor taskExecutor =
                    new ThreadPoolExecutor(1, 1, 300, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(2),
                            new ThreadPoolExecutor.DiscardPolicy());
            hqThreadListenerContainer.setTaskExecutor(taskExecutor);
            if (HQConstant.shExchangeId.equals(dataSource.getExchange())) {// 上海
                HQReader fastprocessor_1 = new SHFastProcessor(dataSource.getFile_1());
                HQReader fastprocessor_2 = new SHFastProcessor(dataSource.getFile_2());
                HQReader[] shHQReaderArray = new HQReader[] { fastprocessor_1, fastprocessor_2 };// 按优先级排
                logger.info("初始化构造sh行情源文件读取器对象,优先级依次高->低" + " 高:" + fastprocessor_1.getHQurl() + " 次高:"
                        + fastprocessor_2.getHQurl());
                hqThreadListenerContainer.setDataSourcesAdapterHandler(new SHHQDataSourcesHandler(propertyResolver,
                        shHQReaderArray, stockCodeExecutorOutChannel, hqThreadListenerContainer));
            } else if (HQConstant.szExchangeId.equals(dataSource.getExchange())) {// 深圳
                HQReader dbfprocessor_1 = new SZProcessor(dataSource.getFile_1());
                HQReader dbfprocessor_2 = new SZProcessor(dataSource.getFile_2());
                HQReader[] szHQReaderArray = new HQReader[] { dbfprocessor_1, dbfprocessor_2 };
                logger.info("初始化构造sz行情源文件读取器对象,优先级依次高->低" + " 高:" + dbfprocessor_1.getHQurl() + " 次高:"
                        + dbfprocessor_2.getHQurl());
                hqThreadListenerContainer.setDataSourcesAdapterHandler(new SZHQDataSourcesHandler(propertyResolver,
                        szHQReaderArray, stockCodeExecutorOutChannel, hqThreadListenerContainer));
            } else {
                throw new RuntimeException("配置错误，仅仅支持sh和sz两种数据源读取类型");
            }
            // 异步并发写入
            HQAsynWriterTemplate hqAsynWriterTemplate =
                    new HQAsynWriterTemplate(AsynConfig.getInstance().getWriterCount(), hqDirectChannel);
            HQGloableConfig.asynWriterCacheInMemory.put(dataSource.getExchange(), hqAsynWriterTemplate);
            hqThreadListenerContainer.setHqlistener(new HQDataSourceListener(hqAsynWriterTemplate));
            // 必须是单线程处理,sh和sz各自一个主单线程负责文件读取
            hqThreadListenerContainer.setConcurrentConsumers(1);
            hqThreadListenerContainer.setMaxConcurrentConsumers(1);
            hqThreadListenerContainer.setMaxMessagesPerTask(10000);
            hqThreadListenerContainer.setWorkName("worker-" + dataSource.getExchange());
            hqThreadListenerContainer.initialize();
            HQGloableConfig.workerCacheInMemory.put(dataSource.getExchange(), hqThreadListenerContainer);
            // 判断启动时间是否在行情交易时间内，如果在则立刻启动，反之不启动，仅仅初始化
            Calendar curCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
            long curMillis = curCalendar.getTimeInMillis();
            if (CommonHelp.isWorkerFetchTimeRange(curMillis) && CommonHelp.isTradeDay(curCalendar)) {
                hqThreadListenerContainer.start();
                logger.info(LogUtils.getLogInfo(LogConstant.LEVLE1_PROCESSOR_MODULE, LogConstant.DEFAULT_ERROR_KEY,
                        LogConstant.DEFAULT_INFO_VALUE, "启动监听获取" + dataSource.getExchange() + "数据源worker."));
            } else {
                logger.info("由于当前时间不在情交易时间区间[9:00~15:00]内或者不是交易日,因此监听获取" + dataSource.getExchange()
                        + "数据源worker仅仅初始化完成,系统会根据定时时间来启动worker");
            }
        }
    }

    /**
     * 用于在交易区间重启服务,加载内外盘和大资金内外流从磁盘加载到内存,用于累计不丢失.
     */
    private void loaddiskPersistentCache() {
        Calendar curCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        long curMillis = curCalendar.getTimeInMillis();
        if (CommonHelp.isLoadDiskCacheTimeRange(curMillis) && CommonHelp.isTradeDay(curCalendar)) {
            logger.info("从磁盘加载盘间因重启导致缓存丢失的数据(大额基金和内外盘缓存)时间范围:9:30~ 15:04才加载文件");
            try {
                Properties prs = CacheDiskStoreUtil.readCacheFromDisk();
                Set<Object> keySets = prs.keySet();
                for (Object key : keySets) {
                    String values = prs.getProperty((String) key);
                    String[] cacheArray = values.split("\\|");
                    if (cacheArray != null && cacheArray.length == 4) {
                        // sumInnerDisc|sumOuterDisc|bigInflows|bigOutflows
                        // 内盘外盘过滤掉指数
                        // 上交所 - 标志指数代码
                        // 深交所 - 标志指数代码
                        // 深交所 - 深圳创业板
                        // 深交所- 沪深300指数代码
                        // 上交所- 沪深300指数代码
                        if (key.equals("sh_000001") || key.equals("sh_000300") || key.equals("sz_399001")
                                || key.equals("sz_399006") || key.equals("sz_399300")) {
                            BigFundSDataCache.add((String) key, HQConstant.BIG_FUNDS_INFLOWS,
                                    Double.valueOf(cacheArray[2]).doubleValue());
                            BigFundSDataCache.add((String) key, HQConstant.BIG_FUNDS_OUTFLOWS,
                                    Double.valueOf(cacheArray[3]).doubleValue());
                        } else {
                            InOutDataCache.add((String) key, HQConstant.SUM_INNER_VOLUME, Long.valueOf(cacheArray[0])
                                    .longValue());
                            InOutDataCache.add((String) key, HQConstant.SUM_OUTER_VOLUME, Long.valueOf(cacheArray[1])
                                    .longValue());
                            BigFundSDataCache.add((String) key, HQConstant.BIG_FUNDS_INFLOWS,
                                    Double.valueOf(cacheArray[2]).doubleValue());
                            BigFundSDataCache.add((String) key, HQConstant.BIG_FUNDS_OUTFLOWS,
                                    Double.valueOf(cacheArray[3]).doubleValue());
                            logger.info("加载磁盘缓存key:" + key + " {sumInnerDisc|sumOuterDisc|bigInflows|bigOutflows}:"
                                    + values);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("从磁盘文件加载盘间因重启导致特殊缓存(大额基金和内外盘缓存)异常" + e);
            }
        }
    }

    public ThreadPoolExecutor getHqProcessTaskExecutor() {
        return hqProcessTaskExecutor;
    }

    public GenericApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

}