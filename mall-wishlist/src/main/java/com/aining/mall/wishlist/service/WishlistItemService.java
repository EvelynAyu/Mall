package com.aining.mall.wishlist.service;

import com.aining.mall.wishlist.entity.WishlistItemEntity;
import com.aining.mall.wishlist.vo.WishlistItemVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 22:09
 */
public interface WishlistItemService extends IService<WishlistItemEntity> {

    List<WishlistItemEntity> showItemInWishlist(Long wlId);
    void addToWishlist(Long skuId, Long wlId);
    void deleteItemInWishlist(Long skuId, Long wlId);
}
