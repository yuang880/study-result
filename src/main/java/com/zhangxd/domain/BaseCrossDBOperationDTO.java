/*
 * @(#)CrossDBOperationDTO.java      1.0   2018年8月6日
 *
 * Copyright (c) 2009 fingard System Engineering Co., Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * fingard System Engineering Co., Ltd. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered
 * into with fingard.
 */
package com.zhangxd.domain;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * CrossDBOperationDTO
 * 跨库查询的通用DTO
 * P 需要继承基础查询参数
 * C 实际传入的单选或者反选数据结构
 *
 * @author guopeng
 */
@ApiModel(value = "CrossDBOperationDTO 跨库查询的通用DTO； P-需要继承基础查询参数；C-实际传入的单选或者反选数据结构")
public class BaseCrossDBOperationDTO<Q extends BaseQueryParams & SelectAll, C, P> implements Serializable {
    private static final long serialVersionUID = -7173947534799604925L;
    /**
     * 是否全选
     */
    @ApiModelProperty("是否全选")
    private Boolean allSelect;

    /**
     * 查询参数，就是页面查询时用的入参
     */
    @ApiModelProperty("查询参数，就是页面查询时用的入参")
    private Q queryParam;
    /**
     * 选择or反选的数据
     */
    @ApiModelProperty("选择or反选的数据")
    private List<C> selectData;
    /**
     * 操作参数
     */
    @ApiModelProperty("操作参数")
    private P operationParam;
    /**
     * 是否要解除查询数量的限制
     * 默认不接触，需要进行查询数量限制
     */
    @ApiModelProperty("是否要解除查询数量的限制 默认不接触，需要进行查询数量限制")
    private  boolean limitLock = true;

    public Boolean getAllSelect() {
        return allSelect;
    }

    public void setAllSelect(Boolean allSelect) {
        this.allSelect = allSelect;
    }

    public Q getQueryParam() {
        return queryParam;
    }

    public void setQueryParam(Q queryParam) {
        this.queryParam = queryParam;
    }

    public List<C> getSelectData() {
        return selectData;
    }

    public void setSelectData(List<C> selectData) {
        this.selectData = selectData;
    }

    public P getOperationParam() {
        return operationParam;
    }

    public void setOperationParam(P operationParam) {
        this.operationParam = operationParam;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public boolean isLimitLock() {
        return limitLock;
    }

    public void setLimitLock(boolean limitLock) {
        this.limitLock = limitLock;
    }

    /**
     * 参数的通用检查
     * @param checkHandler 对明细进行参数检查的消费函数
     */
    public void commonParamCheck(Consumer<C> checkHandler) {
        if (!isValid()) {
            throw new RuntimeException("非全选状态下至少选中一条数据");
        }
        // 进行通用数据的校验检查
        if (selectData!= null && !selectData.isEmpty()) {
            selectData.forEach(checkHandler);
        }
    }

    /**
     * 根据全选标记对数据进行基础数据量检查
     * 1、要求在非全选状态下，selectData至少需要一条数据用于操作
     *
     * @return 如果检查成功返回true，否则返回false
     */
    public boolean isValid() {
        return !(!allSelect && selectData != null && selectData.isEmpty());
    }

    /**
     * 进行多选数据的通用处理
     * 如果传入数据的全选标记是全选，且选择有数据的情况，这种情况是反选，需要将数据设置到反选列表中
     * 如果传入数据的全选标记是单选，且有数据的情况，说明正选
     *
     * @param handle 该函数方法的目标是根据传入的{data}取出urid，需要调用的时候自己实现
     * @return 返回包含或不包含的数据集合
     * @author guopeng
     * @date 2020年11月2日
     */
    public SelectAllIncludeParam selectAllIncludeParam(Function<C, Long> handle) {
        SelectAllIncludeParam selectAllIncludeParam = new SelectAllIncludeParam();
        List<Long> uridList = new ArrayList<>();
        // 从数据中转换出对应的ID列表
        if (CollectionUtil.isNotEmpty(selectData)) {
            uridList = selectData.stream().map(handle).collect(Collectors.toList());
        }
        if (allSelect) {
            if (CollectionUtil.isNotEmpty(uridList)) {
                selectAllIncludeParam.setExcludeUridList(Lists.partition(uridList, 1000));
            } else {
                selectAllIncludeParam.setExcludeUridList(Collections.emptyList());
            }
            selectAllIncludeParam.setIncludeUridList(Collections.emptyList());
        } else {
            Assert.notEmpty(uridList, "非全选明细数据不能为空");
            selectAllIncludeParam.setExcludeUridList(Collections.emptyList());
            selectAllIncludeParam.setIncludeUridList(Lists.partition(uridList, 1000));
        }
        return selectAllIncludeParam;
    }
}
