package com.hksc.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hksc.product.entity.StockLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存日志Mapper
 */
@Mapper
public interface StockLogMapper extends BaseMapper<StockLog> {
}
