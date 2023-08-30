package com.aining.mall.wishlist.service;

import com.aining.mall.wishlist.entity.SharedWishlistEntity;
import com.aining.mall.wishlist.vo.CollaboratorVo;
import com.aining.mall.wishlist.vo.PersonalWishlistVo;
import com.aining.mall.wishlist.vo.WishlistVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 22:07
 */
public interface SharedWishlistService extends IService<SharedWishlistEntity> {
    List<SharedWishlistEntity> getSharedWishlist();

    List<CollaboratorVo> showCollaborator(Long wlId);

    void updateSharedWishlist(Long codeId);

    List<WishlistVo> getAllWishlist();
}
