package com.aining.mall.wishlist.controller;

import com.aining.mall.wishlist.service.SharedWishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分享状态的wishlist
 *
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 22:15
 */

@RestController
@RequestMapping("wishlist/sharedWishlist")
public class SharedWishlistController {

    @Autowired
    private SharedWishlistService sharedWishlistService;
}
