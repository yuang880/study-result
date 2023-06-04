package com.zhangxd.util;

import lombok.Getter;
import lombok.Setter;

/**
 * description: 头部参数 <br>
 */
@Getter
@Setter
public class SheetHeadParam {
    /**
     * 开始行
     */
    private int firstRow;
    /**
     * 结束行
     */
    private int lastRow;
    /**
     * 开始列
     */
    private int firstCol;
    /**
     * 结束列
     */
    private int lastCol;

    /**
     * 合并标记，是否需要合并，true表示合并，false表示不需要合并
     */
    private boolean mergeSign=true;

    /**
     * 标题
     */
    private String title;


}
