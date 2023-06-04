package com.zhangxd.enums;

public enum ENDownloadType {
    /**
     * 1-普通列表下载
     */
    GRID("1"),

    /**
     * 2-树形列表下载
     */
    GROUP_GRID("2");

    private String value;

    ENDownloadType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
