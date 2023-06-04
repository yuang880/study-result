package com.zhangxd.domain;

/**
 * 使得查询参数有能力进行全选或者全选后反选数据得能力
 */

public interface SelectAll {

    void setSelectAllIncludeParam(SelectAllIncludeParam param);
}
