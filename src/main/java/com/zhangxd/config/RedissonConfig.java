package com.zhangxd.config;

import com.zhangxd.enums.ENRedisProfile;
import com.zhangxd.util.MyProperties;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SingleServerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class RedissonConfig {
    private final MyProperties myProperties = MyProperties.getMyPropertiesInstance();

    /**
     * 注入 redisson的 client
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() throws Exception {
        String cacheProfile = myProperties.getProperty("redis.cache.profile");
        String codeMode = myProperties.getProperty("redis.code.mode", "org.redisson.codec.JsonJacksonCodec");
        boolean useScriptCache = myProperties.getProperty3("redis.config.useScriptCache", true);
        Integer retryInterval = myProperties.getProperty2("redis.retry.interval", 100);
        Integer retryAttempts = myProperties.getProperty2("redis.retry.attempts", 0);

        Config config = new Config();
        //脚本缓存
        config.setUseScriptCache(useScriptCache);
        //序列化方式
        config.setCodec((Codec) Class.forName(codeMode).newInstance());


        if (cacheProfile != null && cacheProfile.startsWith(ENRedisProfile.PROFILE_CLUSTER.getValue())) {
            //集群模式
            ClusterServersConfig clusterServersConfig = config.useClusterServers();
            String address1 = myProperties.getProperty("redis.cluster.address1");
            String address2 = myProperties.getProperty("redis.cluster.address2");
            String address3 = myProperties.getProperty("redis.cluster.address3");
            String address4 = myProperties.getProperty("redis.cluster.address4");
            String address5 = myProperties.getProperty("redis.cluster.address5");
            String address6 = myProperties.getProperty("redis.cluster.address6");
            clusterServersConfig
                    .setReadMode(ReadMode.MASTER)
                    .setRetryInterval(retryInterval)
                    .setRetryAttempts(retryAttempts)
                    .addNodeAddress(address1, address2, address3, address4, address5, address6);

            if (ENRedisProfile.PROFILE_CLUSTER_AUTH.getValue().equals(cacheProfile)) {
                //集群带密码模式
                String password = myProperties.getProperty("redis.password");
                clusterServersConfig.setPassword(password);
            }

        } else {
            //单机模式
            SingleServerConfig singleServerConfig = config.useSingleServer();
            String address = myProperties.getProperty("redis.address");
            singleServerConfig
                    .setAddress(address)
                    .setRetryInterval(retryInterval)
                    .setRetryAttempts(retryAttempts);

            if (ENRedisProfile.PROFILE_SINGLE_AUTH.getValue().equals(cacheProfile)) {
                //单机-带密码模式
                String password = myProperties.getProperty("redis.password");
                singleServerConfig.setPassword(password);
            }
        }

        return Redisson.create(config);
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient concurrencyRedissonClient() throws Exception {
        String cacheProfile = myProperties.getProperty("redis.cache.profile");
        String codeMode = myProperties.getProperty("redis.code.mode", "org.redisson.codec.JsonJacksonCodec");
        boolean useScriptCache = myProperties.getProperty3("redis.config.useScriptCache", true);
        Integer retryInterval = myProperties.getProperty2("redis.retry.interval.concurrency", 100);
        Integer retryAttempts = myProperties.getProperty2("redis.retry.attempts.concurrency", 0);

        Config config = new Config();
        //脚本缓存
        config.setUseScriptCache(useScriptCache);
        //序列化方式
        config.setCodec((Codec) Class.forName(codeMode).newInstance());

        if (cacheProfile != null && cacheProfile.startsWith(ENRedisProfile.PROFILE_CLUSTER.getValue())) {
            //集群模式
            ClusterServersConfig clusterServersConfig = config.useClusterServers();

            String address1 = myProperties.getProperty("redis.cluster.address1.concurrency");
            String address2 = myProperties.getProperty("redis.cluster.address2.concurrency");
            String address3 = myProperties.getProperty("redis.cluster.address3.concurrency");
            String address4 = myProperties.getProperty("redis.cluster.address4.concurrency");
            String address5 = myProperties.getProperty("redis.cluster.address5.concurrency");
            String address6 = myProperties.getProperty("redis.cluster.address6.concurrency");
            clusterServersConfig
                    .setReadMode(ReadMode.MASTER)
                    .setRetryInterval(retryInterval)
                    .setRetryAttempts(retryAttempts)
                    .addNodeAddress(address1, address2, address3, address4, address5, address6);

            if (ENRedisProfile.PROFILE_CLUSTER_AUTH.getValue().equals(cacheProfile)) {
                //集群带密码模式
                String password = myProperties.getProperty("redis.password.concurrency");
                clusterServersConfig.setPassword(password);
            }

        } else {
            //单机模式
            SingleServerConfig singleServerConfig = config.useSingleServer();
            String address = myProperties.getProperty("redis.address.concurrency");
            singleServerConfig
                    .setAddress(address)
                    .setRetryInterval(retryInterval)
                    .setRetryAttempts(retryAttempts);

            if (ENRedisProfile.PROFILE_SINGLE_AUTH.getValue().equals(cacheProfile)) {
                //单机-带密码模式
                String password = myProperties.getProperty("redis.password.concurrency");
                singleServerConfig.setPassword(password);
            }
        }

        return Redisson.create(config);
    }
}
