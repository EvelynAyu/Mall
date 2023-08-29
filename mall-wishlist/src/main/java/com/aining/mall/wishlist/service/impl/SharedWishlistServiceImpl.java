package com.aining.mall.wishlist.service.impl;

import com.aining.mall.wishlist.dao.SharedWishlistDao;
import com.aining.mall.wishlist.entity.SharedWishlistEntity;
import com.aining.mall.wishlist.service.SharedWishlistService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 22:07
 */
@Service("sharedWishlistService")
public class SharedWishlistServiceImpl extends ServiceImpl<SharedWishlistDao, SharedWishlistEntity> implements SharedWishlistService {
}
