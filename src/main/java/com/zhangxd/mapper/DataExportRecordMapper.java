package com.zhangxd.mapper;

import com.zhangxd.domain.export.DataExportRecordDTO;
import com.zhangxd.domain.export.DataExportRecordParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.ResultSetType;

import java.util.List;

@Mapper
public interface DataExportRecordMapper {

    int insert(DataExportRecordDTO record);

    DataExportRecordDTO selectByPrimaryKey(String urid);

    List<DataExportRecordDTO> queryBySelective(DataExportRecordParam dataExportRecordParam);

    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = Integer.MIN_VALUE)
    List<DataExportRecordDTO> exportBySelective(DataExportRecordParam dataExportRecordParam);

    long countBySelective(DataExportRecordParam dataExportRecordParam);


    int updateByPrimaryKeySelective(DataExportRecordDTO record);

    int updateWithPreState(@Param("record")DataExportRecordDTO record, @Param("status")String status);

    /**
     * 统计所有状态为成功，超出有效时间范围内的记录
     * @param param 查询参数
     * @return 符合条件的导出记录
     */
    int countInvalidRecord(DataExportRecordParam param);

    /**
     * 查询所有状态为成功，超出有效时间范围内的记录
     * @param dataExportRecordParam 查询参数
     * @return 符合条件的导出记录
     */
    List<DataExportRecordDTO> listInvalidRecord(DataExportRecordParam dataExportRecordParam);
}
