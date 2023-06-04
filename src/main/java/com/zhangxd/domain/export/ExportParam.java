package com.zhangxd.domain.export;

import com.zhangxd.enums.ENDownloadType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhangxd
 * @version 1.0 2023/06/04
 */
@Getter
@Setter
@ApiModel(value = "导出参数")
public class ExportParam implements Serializable {

    private static final long serialVersionUID = 6416891572374825816L;

    /**
     * 列表显示字段、显示字段对应字段名、是否格式化
     */
    @ApiModelProperty(value = "列表显示字段、显示字段对应字段名、是否格式化", required = true)
    private List<String> columnTitles;

    /**
     * 操作用户id
     */
    @ApiModelProperty(value = "操作用户id", required = true)
    private String userId;

    /**
     * 登录名
     */
    @ApiModelProperty(value = "登录名", required = true)
    private String loginId;

    /**
     * sheet页名字
     */
    @ApiModelProperty(value = "sheet页名字", required = false)
    private String sheetName;

    /**
     * 生成的文件名
     */
    @ApiModelProperty(value = "生成的文件名", required = true)
    private String ftpFileName;

    /**
     * ftp保存路径
     */
    @ApiModelProperty(value = "ftp保存路径", required = true)
    private String ftpFilePath;

    /**
     * 列表类型
     */
    @ApiModelProperty(value = "列表类型", required = true)
    private ENDownloadType downloadType;

    /**
     * 导出的文件格式
     * 默认为excel 2007
     */
    @ApiModelProperty(value = "导出格式", required = true)
    private String exportFormat = "EXCEL";

    /**
     * 需要查询的数据库字段列表
     * 默认为excel 2007
     */
    @ApiModelProperty("需要查询的数据库字段列表")
    private List<String> dataBaseFieldList;
}
