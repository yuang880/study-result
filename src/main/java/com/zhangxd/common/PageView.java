package com.zhangxd.common;

import com.github.pagehelper.PageInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class PageView<T> extends ResultContext<T> implements Serializable {

    private static final long serialVersionUID = -4596656718420140378L;

    /**
     * 列表内容
     */
    private List<T> rows;

    /**
     * 总条数
     */
    private Long total;
    /**
     * 当前页码
     */
    private int currentPage;
    // 总页码
    private int totalPage;


    public void covertPageInfoToPageView(PageInfo pageInfo) {
        this.setRows(pageInfo.getList());
        this.setTotal(pageInfo.getTotal());
        this.setCurrentPage(pageInfo.getPageNum());
        this.setTotalPage(pageInfo.getPages());
    }

    public static <K extends Serializable> PageView<K> convertPageView(PageView<?> pageView) {
        PageView<K> target = new PageView<>();
        target.setTotal(pageView.getTotal());
        target.setCurrentPage(pageView.getCurrentPage());
        target.setTotalPage(pageView.getTotalPage());
        return target;
    }

    public static <T> PageView<T> getPageView(PageInfo<T> pageInfo) {
        PageView<T> pageView = new PageView<>();
        pageView.setRows(pageInfo.getList());
        pageView.setTotal(pageInfo.getTotal());
        return pageView;
    }
}
