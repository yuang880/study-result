package com.zhangxd.service;

import com.zhangxd.enums.ENExportDeal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Component
public class ExportDealFactory {
    private final Map<String, ExportDeal<?,?>> exportDealMap;

    @Autowired
    public ExportDealFactory(Map<String, ExportDeal<?,?>> exportDealMap) {
        this.exportDealMap = exportDealMap;
        if (this.exportDealMap.size() != ENExportDeal.values().length
                || Arrays.stream(ENExportDeal.values()).anyMatch(x -> !exportDealMap.containsKey(x.getValue()))) {
            String errMsg = "exportDealMap注入进来的ExportDeal实例数和ENExportDeal中的枚举数不一致,或者枚举Value定义错误,请检查ENExportDeal";
            throw new RuntimeException(errMsg);
        }
    }

    public ExportDeal<?, ?> getExportDeal(String exportDealValue) {
        return exportDealMap.get(exportDealValue);
    }

    public ExportDeal<?, ?> getExportDeal(ENExportDeal enExportDeal) {
        return getExportDeal(enExportDeal.getValue());
    }

}
