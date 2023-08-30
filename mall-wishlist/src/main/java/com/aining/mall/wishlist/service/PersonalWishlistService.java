package com.aining.mall.wishlist.service;

import com.aining.mall.wishlist.entity.PersonalWishlistEntity;
import com.aining.mall.wishlist.vo.PersonalWishlistVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 22:01
 */
public interface PersonalWishlistService extends IService<PersonalWishlistEntity> {
    List<PersonalWishlistVo> getPersonalWishlist();

    void saveWishlist(String wlName);

    void updateWishlist(Long wlId, String wlName);

    void updateWishlistStatus(Long wishlistId);
}
