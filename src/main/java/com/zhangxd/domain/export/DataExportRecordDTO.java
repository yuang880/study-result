package com.zhangxd.domain.export;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel(value = "导出记录DTO")
@Getter
@Setter
public class DataExportRecordDTO extends DataExportRecordPO {
    private static final long serialVersionUID = 5832727384332281068L;

    /**
     * 创建人
     */
    private String creatorShow;

    /**
     * 状态
     * ENDataExportStatus
     */
    private String statusShow;

    /**
     * 执行进度百分比
     */
    private String progressShow;

    /**
     * 数据源
     * ENExportDeal
     */
    private String dataSourceShow;
}
