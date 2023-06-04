package com.zhangxd.util;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class SheetParam {
    /**
     * 行数量,从1开始，sheet计数从0开始
     */
    private int rowCount;

    /**
     * 列数量,从1开始，sheet计数从0开始
     */
    private int colCount;

    /**
     * sheet名称
     */
    private String sheetName;
    /**
     * 头部参数
     */
    private List<SheetHeadParam> headParamList;
    /**
     * 内容参数
     */
    private List<SheetContentParam> contentParamList;
}
