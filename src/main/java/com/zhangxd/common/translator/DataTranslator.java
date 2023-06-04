package com.zhangxd.common.translator;

import java.util.List;

/**
 * 数据翻译接口
 * 1. 要求实现的子类将翻译所需要的缓存放置在ThreadLocal中
 * 翻译之前调用initCache将缓存对象放置到ThreadLocal中
 * 完成翻译之后调用cleanCache将缓存清空
 * <p>
 * 2. translate和translateList方法都仅完成翻译的工作
 * 3. translateListAndClean 方法只完成进行一个列表的数据翻译，会自动完成翻译缓存的初始化和缓存清理
 */
public interface DataTranslator<T> {
    /**
     * 初始化缓存
     */
    void initCache();

    /**
     * 清空翻译缓存
     */
    void cleanCache();

    /**
     * 传入一个待翻译对象，翻译器完成数据翻译
     *
     * @param data 待翻译对象
     */
    void translate(T data);

    /**
     * 翻译一个列表
     *
     * @param data 待翻译数据
     */
    void translateList(List<T> data);

    /**
     * 翻以前做数据初始化，翻译完成后将缓存清空
     *
     * @param data 待翻译数据
     */
    void translateListAndClean(List<T> data);
}
