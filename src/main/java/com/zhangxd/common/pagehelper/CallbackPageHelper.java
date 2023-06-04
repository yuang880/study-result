package com.zhangxd.common.pagehelper;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

/**
 * @description     调用github分页插件
 * @author cuiwx
 * @versoin 1.0  2019/9/19
 */

public class CallbackPageHelper {

    public static <E> Page<E> callBackPageHelper(int pageNum, int pageSize) {
        return PageHelper.startPage(pageNum,pageSize);
    }

    public static <E> Page<E> callBackPageHelper(int pageNum, int pageSize, boolean count) {
        return PageHelper.startPage(pageNum, pageSize, count, (Boolean)null, (Boolean)null);
    }

    public static <E> Page<E> callBackPageHelper(int pageNum, int pageSize, boolean count, Boolean reasonable, Boolean pageSizeZero) {
        return PageHelper.startPage(pageNum, pageSize, count, reasonable, pageSizeZero);
    }

    public static <E> Page<E> callBackOffsetPage(int offset, int limit, boolean count) {
        return PageHelper.offsetPage(offset, limit, count);
    }
}
