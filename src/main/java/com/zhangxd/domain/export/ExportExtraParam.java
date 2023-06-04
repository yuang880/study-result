package com.zhangxd.domain.export;

/**
 * @author zhangxd
 * @version 1.0 2023/6/4
 */
public interface ExportExtraParam {
    /**
     * 数据库带查询的字段，以逗号分隔
     * @return 数据库带查询的字段，以逗号分隔
     */
    String getDataBaseFields();

    /**
     * setter
     * @param fields 字段
     * 数据库带查询的字段，以逗号分隔
     */
    void setDataBaseFields(String fields);
}
