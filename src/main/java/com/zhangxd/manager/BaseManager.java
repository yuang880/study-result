package com.zhangxd.manager;

import cn.hutool.core.text.UnicodeUtil;
import com.zhangxd.enums.ENCachePrefix;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author taojun
 * @descrition
 * @data 2022/1/17
 */
@Slf4j
@Component
public class BaseManager {
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 获取指定类型数据 先查缓存、再查数据库（redis宕机后也能查询数据库正常返回）
     * @param enCachePrefix  缓存前缀
     * @param cacheKey  缓存key
     * @param function  从数据库获取数据
     * @param <R>  返回类型
     * @return
     */
    public <R> R getCache(ENCachePrefix enCachePrefix, String cacheKey, Function<String, R> function) {
        return getCache(enCachePrefix, cacheKey, function, false);
    }

    /**
     * 获取指定类型数据 先查缓存、再查数据库（redis宕机后也能查询数据库正常返回）
     * @param enCachePrefix  缓存前缀
     * @param cacheKey  缓存key
     * @param function  从数据库获取数据
     * @param unicodeFlag  key是否需要unicode编码
     * @param <R>   返回类型
     * @return
     */
    public <R> R getCache(ENCachePrefix enCachePrefix, String cacheKey, Function<String, R> function, boolean unicodeFlag) {
        RBucket<R> bucket = null;
        R cache = null;
        String cacheKeyTemp = unicodeFlag ? UnicodeUtil.encode(cacheKey) : cacheKey;
        try {
            bucket = redissonClient.getBucket(enCachePrefix.getValue() + cacheKeyTemp);
            cache = bucket.get();
        } catch (Exception e) {
            log.error("获取缓存[{}]数据异常", enCachePrefix.getValue() + cacheKeyTemp, e);
        }
        if (cache != null) {
            return cache;
        }
        R r = function.apply(cacheKey);
        if(Objects.nonNull(bucket) && Objects.nonNull(r)) {
            try {
                bucket.set(r);
            } catch (Exception e) {
                log.error("设置缓存[{}]数据异常", enCachePrefix.getValue() + cacheKeyTemp, e);
            }
        }
        return r;
    }
}
