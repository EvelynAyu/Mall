package com.aining.mall.wishlist.dao;

import com.aining.mall.wishlist.entity.WishlistItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 21:56
 */
@Mapper
public interface WishlistItemDao extends BaseMapper<WishlistItemEntity> {
    void removeItemInWishlist(@Param("skuId") Long skuId, @Param("wlId") Long wlId);
}
