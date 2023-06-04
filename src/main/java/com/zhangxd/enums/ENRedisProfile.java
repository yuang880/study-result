package com.zhangxd.enums;

/**
 * @author Michael
 * @date 2022/12/02 16:01
 */
public enum ENRedisProfile implements LabelAndValue<String> {
    /**
     *redis配置模式
     */
    PROFILE_SINGLE("single", "单机无密码模式"),

    PROFILE_SINGLE_AUTH("single-auth", "单机带密码模式"),

    PROFILE_CLUSTER("cluster", "集群无密码模式"),

    PROFILE_CLUSTER_AUTH("cluster-auth", "集群带密码模式"),
    ;

    private String value;

    private String label;

    ENRedisProfile(String value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
