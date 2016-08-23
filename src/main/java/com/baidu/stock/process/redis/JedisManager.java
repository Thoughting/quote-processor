package com.baidu.stock.process.redis;

import jodd.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertyResolver;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import com.baidu.gushitong.redis.client.RedisPoolProxy;
import com.baidu.gushitong.redis.client.RedisPoolProxyConfig;

/**
 * jedis操作帮助类
 * 
 * @author dengjianli
 * 
 */
public class JedisManager {
	private static Logger logger = LoggerFactory.getLogger(JedisManager.class);
	private static RedisPoolProxy redisPoolProxy;

//	# [redis]公共配置
//	redis.maxtotal=500
//	redis.minidle=2
//	redis.maxwaitmillis=3000
//	redis.pooltimeout=5000
//	redis.bns=group.redis3-fb-rdtestnewquote-1.osp.cn
//	# redis.bns=
//	redis.bnstype=1
//	redis.bnsRetryTimes=3
//	redis.bnsTimeout=5000
//	redis.proxyHost=10.92.103.52
//	redis.proxyPort=8390
//	# redis重试操作的次数
//	redis.retrytimes=3
	
	public static void initRedisPoolProxy(PropertyResolver propertyResolver) {
		RedisPoolProxyConfig redisPoolConfig = new RedisPoolProxyConfig();
		int maxtotal =500;
		int minidle =2;
		int maxwaitmillis =3000;
		int pooltimeout =5000;
		// 下面的基本配置属于common-pool的基本配置，默认不暴漏出去
		redisPoolConfig.setMaxTotal(maxtotal);
		redisPoolConfig.setMinIdle(minidle);// maxIdle默认是8
		redisPoolConfig.setMaxWaitMillis(maxwaitmillis);
		redisPoolConfig.setPoolTimeOut(pooltimeout);
		String redisBns ="group.redis3-fb-rdtestnewquote-1.osp.cn";
		if (StringUtil.isNotBlank(redisBns)) {
			redisPoolConfig.setBnsRetryTimes(3);
			redisPoolConfig.setBnsTimeout(5000);
			redisPoolConfig.setProxyBnsType(1);
		} else {
			logger.warn("发现redis没有配置proxyBns,则默认读取配置host和port作为连接.");
			// 开发环境
			redisPoolConfig.setProxyHost("10.92.103.52");
			redisPoolConfig.setProxyPort(8390);
		}
		redisPoolProxy = new RedisPoolProxy(redisPoolConfig);
		logger.info("初始化redis配置完成.");
	}

	public static RedisPoolProxy getRedisPoolProxy() {
		return redisPoolProxy;
	}
	
//SafeEncoder.encode(resultKey)
	/**
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public static byte[] hgetCache(byte[] key, byte[] field) {
		JedisPool jedisPool = null;
		Jedis jedis = null;
		try {
			jedisPool = redisPoolProxy.getJedisPool();
			jedis = jedisPool.getResource();
			byte[] value = jedis.hget(key, field);
			redisPoolProxy.returnResource(jedisPool, jedis);
			return value;
		} catch (Exception e) {
			redisPoolProxy.returnBrokenResource(jedisPool, jedis);
			logger.error("获取内外盘和大资金流向缓存的数据异常:",e);
		}
		return null;
	}

	
	public static byte[] hsetCache(byte[] key, byte[] field,byte[] value) {
		JedisPool jedisPool = null;
		Jedis jedis = null;
		try {
			jedisPool = redisPoolProxy.getJedisPool();
			jedis = jedisPool.getResource();
			jedis.hset(key, field, value);
			redisPoolProxy.returnResource(jedisPool, jedis);
			return value;
		} catch (Exception e) {
			redisPoolProxy.returnBrokenResource(jedisPool, jedis);
			logger.error("临时存储内外盘和大资金流向缓存数据异常:",e);
		}
		return null;
	}

	public static void delKey(String ...key) {
		JedisPool jedisPool = null;
		Jedis jedis = null;
		try {
			jedisPool = redisPoolProxy.getJedisPool();
			jedis = jedisPool.getResource();
			jedis.del(key);
			redisPoolProxy.returnResource(jedisPool, jedis);
		} catch (Exception e) {
			redisPoolProxy.returnBrokenResource(jedisPool, jedis);
			logger.error("清理临时存储内外盘和大资金流向缓存所有的数据异常:",e);
		}
	}
}
