package com.zhangxd.enums;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.Optional;

/**
 *
 * @author zhangxd
 * @version 1.0 2023/06/04
 */
public enum ENExportDeal implements LabelAndValue<String> {

    USER_EXPORT("singleServiceImpl", "用户导出"),
    DATA_EXPORT("singleServiceImpl", "数据导出"),
    ;
    private final String value;
    private final String label;

    ENExportDeal(String value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public static String getLabelByValue(String value) {
        Optional<ENExportDeal> optional = Arrays.stream(ENExportDeal.values()).filter(x -> x.value.equals(value)).findAny();
        return optional.isPresent() ? optional.get().getLabel() : (ObjectUtil.isNull(value) ? StrUtil.EMPTY : value);
    }
}
