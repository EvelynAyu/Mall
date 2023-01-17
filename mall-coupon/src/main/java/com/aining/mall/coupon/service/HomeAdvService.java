package com.aining.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aining.common.utils.PageUtils;
import com.aining.mall.coupon.entity.HomeAdvEntity;

import java.util.Map;

/**
 * 首页轮播广告
 *
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-17 16:30:38
 */
public interface HomeAdvService extends IService<HomeAdvEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

