package com.aining.mall.wishlist.service;

import com.aining.mall.wishlist.entity.InviteEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/29 19:32
 */

public interface InviteService extends IService<InviteEntity> {

    String generateInviteCode(Long wlId);

    InviteEntity getWishlistByInviteCode(String inviteCode);
}
