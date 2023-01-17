package com.aining.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aining.common.utils.PageUtils;
import com.aining.mall.product.entity.SkuSaleAttrValueEntity;

import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-16 21:44:19
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

