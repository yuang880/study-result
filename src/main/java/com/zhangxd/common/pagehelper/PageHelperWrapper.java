package com.zhangxd.common.pagehelper;


import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageException;
import com.github.pagehelper.page.PageMethod;
import com.github.pagehelper.util.SqlSafeUtil;


/**
 * @description     金额字段(varchar2)排序没有按预期排序，考虑字段加密与不加密的情况
 *                  调用分页插件的时候，统一调用改方法进行一次过滤操作
 * @author cuiwx
 * @versoin 1.0  2019/9/19
 */
public class PageHelperWrapper extends PageMethod{

    private static final String REGEXP_PREFIX="to_number(REGEXP_REPLACE(";
    private static final String REGEXP_SUFFIX=",'[^0-9|.]',''))";
    private static final String COLUMN="AMOUNT";
    private static final int DEFAULT_LEGAL_PAGE_NUM = 1;

    /**
     * 分页查询时，若pageSize为0，默认为在第一页查询出全部数据
     * @param pageNum 页码
     * @param pageSize 每页显示数量
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize) {
        pageNum = pageNum <= 0 ?  DEFAULT_LEGAL_PAGE_NUM : pageNum;
        if (pageSize == 0) {
            return CallbackPageHelper.callBackPageHelper(pageNum, pageSize, DEFAULT_COUNT, null, true);
        } else {
            return CallbackPageHelper.callBackPageHelper(pageNum, pageSize, DEFAULT_COUNT, null, null);
        }
    }

    /**
     * 分页查询时，若pageSize为0，默认为在第一页查询出全部数据
     * @param pageNum 页码
     * @param pageSize 每页显示数量
     * @param count 是否进行count查询
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize, boolean count) {
        pageNum = pageNum <= 0 ?  DEFAULT_LEGAL_PAGE_NUM : pageNum;
        if (pageSize == 0) {
            return CallbackPageHelper.callBackPageHelper(pageNum, pageSize, count, null, true);
        } else {
            return CallbackPageHelper.callBackPageHelper(pageNum, pageSize, count, null, null);
        }
    }

    /**
     * 分页查询时，若pageSize为0，默认为在第一页查询出全部数据
     * @param pageNum 页码
     * @param pageSize 每页显示数量
     * @param orderBy 排序
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize, String orderBy) {
        pageNum = pageNum <= 0 ?  DEFAULT_LEGAL_PAGE_NUM : pageNum;
        Page<E> page;
        if (pageSize == 0) {
            page = CallbackPageHelper.callBackPageHelper(pageNum, pageSize, DEFAULT_COUNT, null, true);
        } else {
            page = CallbackPageHelper.callBackPageHelper(pageNum, pageSize);
        }
        orderByWrapper(page, orderBy);
        return page;
    }

    /**
     * 分页插件扩展，支持跨库查询
     * http://172.18.12.50:8888/browse/RHZL-2113
     * @param offset 偏移量
     * @param limit 每库查询量
     * @param count 是否需要进行count查询
     * @param orderBy 排序字段
     * @param <E>
     * @return
     */
    public static <E> Page<E> offsetPage(int offset, int limit, boolean count,String orderBy) {
        Page<E> page = CallbackPageHelper.callBackOffsetPage(offset, limit, count);
        orderByWrapper(page, orderBy);
        return page;
    }

    private static void orderByWrapper(Page<?> page, String orderBy) {
        if (StrUtil.isNotBlank(orderBy)) {
            StringBuilder orderByBuilder = new StringBuilder(orderBy.length());
            boolean isUnSafePage = false;
            String[] orderByArray = orderBy.split(StrUtil.COMMA);
            for (String orderByItem : orderByArray) {
                String[] orderByItemArray = orderByItem.trim().split(StrUtil.SPACE, 2);
                String sortName = orderByItemArray[0].trim();
                String sortType = orderByItemArray.length > 1 ? StrUtil.SPACE + orderByItemArray[1].trim() : StrUtil.EMPTY;
                if (COLUMN.equalsIgnoreCase(orderByItemArray[0].trim())) {
                    orderByBuilder.append(REGEXP_PREFIX)
                            .append(sortName)
                            .append(REGEXP_SUFFIX)
                            .append(sortType);
                    isUnSafePage = true;
                } else {
                    orderByBuilder.append(sortName)
                            .append(sortType);
                }
                orderByBuilder.append(StrUtil.COMMA).append(StrUtil.SPACE);
            }
            orderByBuilder.delete(orderByBuilder.length() - 2, orderByBuilder.length());
            if (isUnSafePage) {
                if (SqlSafeUtil.check(orderBy)) {
                    throw new PageException("order by [" + orderBy + "] 存在 SQL 注入风险, 如想避免 SQL 注入校验，可以调用 Page.setUnsafeOrderBy");
                }
                page.setUnsafeOrderBy(orderByBuilder.toString());
            } else {
                page.setOrderBy(orderByBuilder.toString());
            }
        } else {
            page.setOrderBy(orderBy);
        }
    }
}
