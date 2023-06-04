package com.zhangxd.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class SelectAllIncludeParam implements Serializable {

    private static final long serialVersionUID = 227101002349720155L;

    /**
     * 包含的urid集合
     */
    private List<List<Long>> includeUridList;

    /**
     * 不包含的urid集合
     */
    private List<List<Long>> excludeUridList;


}
