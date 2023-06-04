package com.zhangxd.domain.export;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Chengwy
 * @version 1.0  2022/4/7
 * @description
 */

@Getter
@Setter
public class DataExportTask implements Serializable {

    private static final long serialVersionUID = -1836867241597146229L;

    /**
     * 任务ID，任务的唯一标识号，以及之后生成的 zip 文件名
     */
    String recordId;

    /**
     * 导出处理类
     */
    String exportDeal;

    /**
     * 查询参数
     * Json字符串
     */
    String queryParamJson;
}
