package com.aining.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aining.common.utils.PageUtils;
import com.aining.mall.product.entity.SpuImagesEntity;

import java.util.Map;

/**
 * spu图片
 *
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-16 21:44:19
 */
public interface SpuImagesService extends IService<SpuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

