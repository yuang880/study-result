package com.zhangxd.enums;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.Optional;

/**
 * 导出状态枚举
 */

public enum ENDataExportStatus implements LabelAndValue<String> {
    QUEUE("1", "导出等待队列中"),
    PROCESS("2", "导出中"),
    UPLOAD("3", "上传中"),
    SUCCESS("4", "导出成功"),
    EXCEPTION("5", "导出异常"),
    EXPIRE("6", "已失效");

    private final String value;
    private final String label;

    ENDataExportStatus(String value, String label) {
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
        Optional<ENDataExportStatus> optional = Arrays.stream(ENDataExportStatus.values()).filter(x -> x.value.equals(value)).findAny();
        return optional.isPresent() ? optional.get().getLabel() : (ObjectUtil.isNull(value) ? StrUtil.EMPTY : value);
    }
}
