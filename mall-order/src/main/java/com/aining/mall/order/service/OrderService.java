package com.aining.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aining.common.utils.PageUtils;
import com.aining.mall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-17 16:52:09
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

