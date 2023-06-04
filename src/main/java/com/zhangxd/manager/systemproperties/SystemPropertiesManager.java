package com.zhangxd.manager.systemproperties;


import com.zhangxd.enums.ENCachePrefix;
import com.zhangxd.manager.BaseManager;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhanxp
 * @version 1.0 2018/8/16
 */
@Component
public class SystemPropertiesManager extends BaseManager {
    @Autowired
    private SystemPropertiesMapper systemPropertiesMapper;
    @Autowired
    private RedissonClient redissonClient;

    public int updateValueByKey(SystemPropertiesDTO systemPropertiesDTO) {
        int update = systemPropertiesMapper.updateValueByKey(systemPropertiesDTO);
        if (update == 1) {
            SystemPropertiesCacheDTO systemPropertiesCacheDTO = new SystemPropertiesCacheDTO();
            String cacheKey = ENCachePrefix.T_SYSTEM_PROPERTIES.getValue() + systemPropertiesDTO.getKey();
            systemPropertiesCacheDTO.setCacheKey(cacheKey);
            systemPropertiesCacheDTO.setValue(systemPropertiesDTO.getValue());
            RBucket<SystemPropertiesCacheDTO> bucket = redissonClient.getBucket(cacheKey);
            bucket.set(systemPropertiesCacheDTO);
        }
        return update;
    }

    public void initCacheAlone() {
    	RBatch transaction = null;
    	initCache(transaction);
    }

    public SystemPropertiesCacheDTO getCache(ENCachePrefix enCachePrefix, String cacheKey) {
        return getCache(enCachePrefix, cacheKey, x -> systemPropertiesMapper.getCacheByKey(x));
    }

    public List<SystemPropertiesDTO> getByPrefix(String prefix) {
        return systemPropertiesMapper.getByPrefix(prefix);
    }

    public String getValueByKey(String key) {
        SystemPropertiesPO systemPropertiesPO = systemPropertiesMapper.selectByFullKey(key);
        if (systemPropertiesPO != null) {
            return systemPropertiesPO.getValue();
        } else {
            throw new BizopsBusinessException("未找到key为" + key + "的系统配置");
        }
    }

    public void initCache(RBatch transaction) {
    	//配置成单独刷新时，transaction为空，需要重新获取
    	boolean needCommit = false;
    	if(transaction == null) {
    		BatchOptions batchOptions = BatchOptions.defaults().skipResult();
    		transaction = redissonClient.createBatch(batchOptions);
    		needCommit = true;
    	}
    	RBatch finalTransaction = transaction;
        List<SystemPropertiesCacheDTO> list = systemPropertiesMapper.getAllCache();
        list.forEach(x -> {
            RBucketAsync<SystemPropertiesCacheDTO> bucket = finalTransaction.getBucket(ENCachePrefix.T_SYSTEM_PROPERTIES.getValue() + x.getCacheKey());
            bucket.setAsync(x);
        });
        deleteExtraCache(list, finalTransaction);

        if(needCommit){
			finalTransaction.execute();
		}
    }

    private void deleteExtraCache(List<SystemPropertiesCacheDTO> systemPropertiesCacheDTOList, RBatch transaction) {
        RKeys keys = redissonClient.getKeys();
        Iterable<String> iterable = keys.getKeysByPattern(ENCachePrefix.T_SYSTEM_PROPERTIES.getValue() + "*" ,10000);
        Set<String> set = new HashSet<>();
        iterable.forEach(set::add);
        // 寻找存在于redis中但是不存在于现在将要刷新的列表中的key
        systemPropertiesCacheDTOList.forEach(x -> set.remove(ENCachePrefix.T_SYSTEM_PROPERTIES.getValue() + x.getCacheKey()));
        //移除现有配置中不存在的key
        set.forEach(x -> transaction.getBucket(x).deleteAsync());
    }

    public void initCacheForSingle(Map<String, Object> configMap){
        List<SystemPropertiesCacheDTO> allCache = systemPropertiesMapper.getAllCache();
        allCache.forEach(cache ->
                configMap.put(ENMemoryConfigPrefix.M_SYSTEM_PROPERTIES.getValue() + cache.getCacheKey(), cache));
    }

    public List<SystemPropertiesDTO> getAllBatchToSingleQueue(){
        return systemPropertiesMapper.getAllBatchToSingleQueue();
    }

    public int save(SystemPropertiesDTO systemPropertiesDTO) {
    	int insertCount = systemPropertiesMapper.insert(systemPropertiesDTO);
    	if(insertCount == 1) {
    		String cacheKey = ENCachePrefix.T_SYSTEM_PROPERTIES.getValue() + systemPropertiesDTO.getKey();
    		RBucket<SystemPropertiesCacheDTO> bucket = redissonClient.getBucket(cacheKey);
    		SystemPropertiesCacheDTO systemPropertiesCacheDTO = new SystemPropertiesCacheDTO();
            systemPropertiesCacheDTO.setCacheKey(cacheKey);
            systemPropertiesCacheDTO.setValue(systemPropertiesDTO.getValue());
    		bucket.set(systemPropertiesCacheDTO);
    	}
    	return insertCount;
    }

    /**
     * 从数据库中获取所有系统参数缓存
     * @return
     */
    public List<SystemPropertiesCacheDTO> getAllCache() {
        return systemPropertiesMapper.getAllCache();
    }
}
