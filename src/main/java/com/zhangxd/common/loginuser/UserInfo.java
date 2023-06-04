package com.zhangxd.common.loginuser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Setter
@Getter
public class UserInfo implements Serializable {
    private static final long serialVersionUID = -5165209452487667711L;
    private String userId;
    private String loginId;
    private String userName;
    private String tenantIds;

    @JsonIgnore
    public List<String> getTenantIdList() {
        if (tenantIds != null) {
            return Arrays.asList(tenantIds.split(","));
        } else {
            return new ArrayList<>();
        }

    }
}
