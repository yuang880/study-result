package com.zhangxd.domain;


import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@ApiModel(value = "基础查询参数")
public class BaseQueryParams implements Serializable {

    private static final long serialVersionUID = 2227634848787001565L;

    @ApiModelProperty("页码")
    private Integer pageNum = 1;

    @ApiModelProperty("每页记录数量")
    private Integer pageSize = 20;

    @ApiModelProperty("表格排序字段名")
    private String sortName;

    @ApiModelProperty("表格排序类型 desc,asc")
    private String sortType;


    public String getOrderBy() {
        String sortBy = null;
        if (StrUtil.isNotBlank(sortName) && StrUtil.isNotBlank(sortType)) {
            sortBy = sortName + StrUtil.SPACE + sortType;
        }
        return sortBy;
    }

    public String getDtoOrderBy() {
        String sortBy = null;
        if (StrUtil.isNotBlank(sortName) && StrUtil.isNotBlank(sortType)) {
            sortBy = sortName + StrUtil.SPACE + sortType;
        }
        return sortBy;
    }
}
