package com.zhangxd.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class MyProperties {

    private static String[] paths =
            new String[]{"parameter.properties"};

    private static volatile MyProperties p = null;

    private final Map<Object, Object> pMap = new ConcurrentHashMap<Object, Object>();
    private String[] originalPaths;

    private MyProperties(String[] paths) {
        originalPaths = paths;
        init(paths);
    }

    /**
     * 将paths制定多个文件全部加载到内存
     *
     * @param paths
     * @author ylc
     */
    private void init(String[] paths) {
        for (String path : paths) {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            if (is != null) {
                Properties properties = new Properties();
                try {
                    BufferedReader bf = new BufferedReader(new InputStreamReader(is, "utf-8"));
                    properties.load(bf);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                int originalSize = pMap.size();
                pMap.putAll(properties);
                if (originalSize + properties.size() > pMap.size()) {
                    log.warn("请注意Properties定义key值发生了重叠！\r\n！\r\n！");
                }
            } else {
                log.warn("无法读取系统参数文件：" + path);
            }
        }
    }

    public String getProperty(String key) {
        return this.pMap.get(key) == null ? null : pMap.get(key).toString();
    }

    public String getProperty(String key, String defaultStr) {
        return this.pMap.get(key) == null ? defaultStr : pMap.get(key).toString();
    }

    public Integer getProperty2(String key) {
        return this.getProperty(key) == null ? null : Integer.parseInt(this.getProperty(key));
    }

    public Integer getProperty2(String key, int defaultV) {
        return this.getProperty(key) == null ? defaultV : Integer.parseInt(this.getProperty(key));
    }

    public Boolean getProperty3(String key) {
        return this.getProperty(key) == null ? null : Boolean.parseBoolean(this.getProperty(key));
    }

    public Boolean getProperty3(String key, boolean defaultV) {
        return this.getProperty(key) == null ? defaultV : Boolean.parseBoolean(this.getProperty(key));
    }

    public MyProperties refresh() {
        pMap.clear();
        this.init(originalPaths);
        return getMyPropertiesInstance();
    }

    public MyProperties refresh(String[] paths) {
        pMap.clear();
        this.init(paths);
        return getMyPropertiesInstance();
    }

    /**
     * 如果内存中MyProperties是空的，则加载paths指定的文件，否则返回已有
     *
     * @param paths
     * @return
     * @author ylc
     */
    public final static MyProperties getMyPropertiesInstance(String[] paths) {
        if (paths == null || paths.length == 0) {
            throw new RuntimeException("没有属性文件");
        }
        if (p == null) {
            synchronized (MyProperties.class) {
                if (p == null) {
                    p = new MyProperties(paths);
                }
            }
        }
        return p;
    }

    /**
     * 返回默认的MyProperties.
     * 默认是 "parameter.properties", "ui.properties"文件
     *
     * @return
     * @author ylc
     */
    public final static MyProperties getMyPropertiesInstance() {
        if (p == null) {
            synchronized (MyProperties.class) {
                if (p == null) {
                    p = new MyProperties(paths);
                }
            }
        }
        return p;
    }

}
