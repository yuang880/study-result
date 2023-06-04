package com.zhangxd.domain.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@ApiModel(value = "导出记录PO")
@Getter
@Setter
public class DataExportRecordPO implements Serializable {
    private static final long serialVersionUID = 1829166701531883681L;

    /**
     * URID
     */
    @ApiModelProperty(value = "null", required = true)
    private String urid;

    /**
     * 创建人
     * CREATOR
     */
    @ApiModelProperty(value = "创建人", required = false)
    private String creator;

    /**
     * 创建时间
     * CREATE_DATE
     */
    @ApiModelProperty(value = "创建时间", required = false)
    private Date createDate;

    /**
     * 更新时间
     * UPDATE_DATE
     */
    @ApiModelProperty(value = "更新时间", required = false)
    private Date updateDate;

    /**
     * 导出状态
     * STATUS
     */
    @ApiModelProperty(value = "导出状态", required = false)
    private String status;

    /**
     * 存储路径
     * STORAGE_PATH
     */
    @ApiModelProperty(value = "存储路径", required = false)
    private String storagePath;

    /**
     * 异常信息
     * EXCEPTION_MESSAGE
     */
    @ApiModelProperty(value = "异常信息", required = false)
    private String exceptionMessage;

    /**
     * 执行进度
     * PROGRESS
     */
    @ApiModelProperty(value = "执行进度", required = false)
    private Double progress;

    /**
     * 数据来源/导出界面名称
     * DATA_SOURCE
     */
    @ApiModelProperty(value = "数据来源/导出界面名称", required = false)
    private String dataSource;

    /**
     * 导出记录数量
     * EXPORT_NUM
     */
    @ApiModelProperty(value = "导出记录数量", required = false)
    private Long exportNum;

    /**
     * 导出参数
     * EXPORT_PARAM
     */
    @ApiModelProperty(value = "导出参数", required = false)
    private String exportParam;
}
