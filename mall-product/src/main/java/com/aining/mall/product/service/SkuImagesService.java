package com.aining.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aining.common.utils.PageUtils;
import com.aining.mall.product.entity.SkuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * sku图片
 *
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-16 21:44:19
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuImagesEntity> getImagesBySkuId(Long skuId);
}

