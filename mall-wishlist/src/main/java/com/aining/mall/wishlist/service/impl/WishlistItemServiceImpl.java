package com.aining.mall.wishlist.service.impl;

import com.aining.mall.wishlist.dao.WishlistItemDao;
import com.aining.mall.wishlist.entity.WishlistItemEntity;
import com.aining.mall.wishlist.service.WishlistItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 22:10
 */

@Service("wishlistItemService")
public class WishlistItemServiceImpl extends ServiceImpl<WishlistItemDao, WishlistItemEntity> implements WishlistItemService {
}
