package com.aining.mall.coupon.dao;

import com.aining.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-17 16:30:38
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
