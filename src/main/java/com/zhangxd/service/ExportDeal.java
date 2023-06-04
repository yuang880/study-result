package com.zhangxd.service;


import com.zhangxd.domain.BaseQueryParams;
import com.zhangxd.domain.export.ExportExtraParam;

import java.util.function.Consumer;

/**
 * 数据导出接口
 * R: 查询返回的数据DTO
 * Q: 查询参数
 * @author Chengwy
 * @version 1.0  2022/4/7
 * @description
 */

public interface ExportDeal<R, Q extends BaseQueryParams & ExportExtraParam> {

    /**
     * 获取需要导出的条数
     * @param queryParam 查询参数
     * @return 条数，long
     */
    long countForExport(Q queryParam);

    /**
     * 使用数据库流式查询处理单条数据
     * @param queryParamJson 查询参数
     * @param dealOneDataHandler 对一条记录的处理
     */
    void dataExport(String queryParamJson, Consumer<R> dealOneDataHandler);
}
