package com.aining.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aining.common.utils.PageUtils;
import com.aining.mall.product.entity.AttrEntity;

import java.util.Map;

/**
 * 商品属性
 *
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-16 21:44:18
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

