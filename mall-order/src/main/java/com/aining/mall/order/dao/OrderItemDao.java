package com.aining.mall.order.dao;

import com.aining.mall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-17 16:52:09
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
