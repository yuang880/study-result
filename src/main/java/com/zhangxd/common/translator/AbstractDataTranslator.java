package com.zhangxd.common.translator;

import java.util.List;
import java.util.Objects;


public abstract class AbstractDataTranslator<T> implements DataTranslator<T> {
    @Override
    public void translateList(List<T> data) {
        if (Objects.nonNull(data) && !data.isEmpty()) {
            for (T t : data) {
                translate(t);
            }
        }
    }

    @Override
    public void translateListAndClean(List<T> data) {
        try {
            this.initCache();
            this.translateList(data);
        } finally {
            this.cleanCache();
        }
    }

    public void translateOneAndClean(T data) {
        try {
            this.initCache();
            this.translate(data);
        } finally {
            this.cleanCache();
        }
    }

}
