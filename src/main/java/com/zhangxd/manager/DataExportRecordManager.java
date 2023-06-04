package com.zhangxd.manager;

import com.github.pagehelper.PageInfo;
import com.zhangxd.common.pagehelper.PageHelperWrapper;
import com.zhangxd.domain.export.DataExportRecordDTO;
import com.zhangxd.domain.export.DataExportRecordParam;
import com.zhangxd.mapper.DataExportRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author zhangxd
 * @version 1.0 2023/6/4
 */

@Component
public class DataExportRecordManager {
    @Autowired
    private DataExportRecordMapper dataExportRecordMapper;

    public int insert(DataExportRecordDTO dataExportRecordDTO) {
        return dataExportRecordMapper.insert(dataExportRecordDTO);
    }

    public DataExportRecordDTO getById(String urid) {
        return dataExportRecordMapper.selectByPrimaryKey(urid);
    }

    public PageInfo<DataExportRecordDTO> pageQuery(DataExportRecordParam dataExportRecordParam) {
        PageHelperWrapper.startPage(dataExportRecordParam.getPageNum(), dataExportRecordParam.getPageSize(), dataExportRecordParam.getOrderBy());
        return new PageInfo<>(dataExportRecordMapper.queryBySelective(dataExportRecordParam));
    }

    public List<DataExportRecordDTO> exportSelective(DataExportRecordParam dataExportRecordParam) {
        PageHelperWrapper.startPage(dataExportRecordParam.getPageNum(), dataExportRecordParam.getPageSize(), false);
        return dataExportRecordMapper.exportBySelective(dataExportRecordParam);
    }

    public long countQuery(DataExportRecordParam dataExportRecordParam) {
        return dataExportRecordMapper.countBySelective(dataExportRecordParam);
    }

    public int update(DataExportRecordDTO dataExportRecordDTO) {
        dataExportRecordDTO.setUpdateDate(new Date());
        return dataExportRecordMapper.updateByPrimaryKeySelective(dataExportRecordDTO);
    }

    public int updateWithPreState(DataExportRecordDTO dataExportRecordDTO, String preState) {
        dataExportRecordDTO.setUpdateDate(new Date());
        return dataExportRecordMapper.updateWithPreState(dataExportRecordDTO, preState);
    }

    /**
     * 统计已经失效的记录数
     *
     * @param dataExportRecordParam 导出参数
     * @return 返回统计数量
     */
    public Integer countInvalidRecord(DataExportRecordParam dataExportRecordParam) {
        return dataExportRecordMapper.countInvalidRecord(dataExportRecordParam);
    }

    /**
     * 可支持分页的查询已经失效的导出记录
     * 分页的目的是为了防止加载数据量过大，导致内存崩溃
     *
     * @param dataExportRecordParam 导出参数
     * @return 返回当前页的已失效的导出记录
     */
    public List<DataExportRecordDTO> listInvalidRecord(DataExportRecordParam dataExportRecordParam) {
        return dataExportRecordMapper.listInvalidRecord(dataExportRecordParam);
    }
}

