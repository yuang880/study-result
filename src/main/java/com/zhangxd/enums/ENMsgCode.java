package com.zhangxd.enums;

import cn.hutool.core.util.StrUtil;

/**
 * 消息代码
 */
public enum ENMsgCode {
    /**
     * 请求操作成功【0】
     */
    SUCCESS("0"),
    /**
     * 业务错误【1】
     */
    BUSINESS_ERROR("1"),
    /**
     * 系统异常【2】
     */
    SYSTEM_EXCEPTION("2"),
    /**
     * 未登录【3】
     */
    NOT_LOG_IN("3"),
    /**
     * session超时【4】
     */
    SESSION_TIMEOUT("4"),
    /**
     * 没有权限【5】
     */
    WITHOUT_RIGHT("5"),
    /**
     * 配置异常【6】
     */
    CONFIG_ERROR("6");

    private String value;

    ENMsgCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    /**
     * 操作是否为成功
     * @param value
     * @return
     */
    public static boolean isSuccess(String value) {
        return StrUtil.equals(SUCCESS.value, value);
    }
}
