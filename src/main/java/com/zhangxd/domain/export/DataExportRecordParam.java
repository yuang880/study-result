package com.zhangxd.domain.export;

import com.zhangxd.domain.BaseQueryParams;
import com.zhangxd.domain.SelectAll;
import com.zhangxd.domain.SelectAllIncludeParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * @author Chengwy
 * @version 1.0  2022/4/8
 * @description
 */

@Getter
@Setter
public class DataExportRecordParam extends BaseQueryParams implements SelectAll, ExportExtraParam {
    private static final long serialVersionUID = -999960792098518399L;

    private List<String> userIdList;

    /**
     * ENDataExportStatus
     */
    private List<String> statusList;

    /**
     * 数据源
     * ENExportDeal
     */
    private List<String> dataSourceList;

    /**
     * 过期时间
     * 用于进行无效记录的检索
     */
    private Date expireDate;

    /**
     * 界面查询开始日期
     */
    private String createDateBegin;

    /**
     * 界面查询结束日期
     */
    private String createDateEnd;


    /**
     * 转换后的起始时间
     */
    private Date createDateBeginDate;
    /**
     * 转换后的结束时间
     */
    private Date createDateEndDate;

    /**
     * 全选内部包含或不包含关键字段
     */
    @ApiModelProperty(value = "全选/反选标记",required = false)
    private SelectAllIncludeParam selectAllIncludeParam;

    /**
     * 要查询的数据库字段
     */
    private String dataBaseFields;
}
