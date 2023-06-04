package com.zhangxd.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.zhangxd.common.translator.AbstractDataTranslator;
import com.zhangxd.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhangxd
 * @version 1.0 2023/2/17
 */
@Service
public class UserTranslator extends AbstractDataTranslator<User> {

    @Autowired
    private BankLocationManager bankLocationManager;

    private final ThreadLocal<ITempMemoryCache> bankLocationCache = new ThreadLocal<>();

    @Override
    public void initCache() {
        bankLocationCache.set(new TempMemoryCache(key -> DatabaseRoute.doWithDatabaseKey(DatabaseRoute.DEFAULT_DATABASE_KEY, () -> bankLocationManager.getByCode(key))));
    }

    @Override
    public void cleanCache() {
        bankLocationCache.remove();
    }

    @Override
    public void translate(DifferentBankHistorySuccessDTO data) {
        BankLocationDTO dto = bankLocationCache.get().get(data.getMatchBankLocationCode());
        if (ObjectUtil.isNull(dto)) {
            data.setMatchBankLocationName(data.getMatchBankLocationCode());
        } else {
            data.setMatchBankLocationName(dto.getName());
        }
    }
}
