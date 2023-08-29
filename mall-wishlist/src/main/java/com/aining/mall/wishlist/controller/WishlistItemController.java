package com.aining.mall.wishlist.controller;

import com.aining.mall.wishlist.service.WishlistItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分享单中的商品信息
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 22:16
 */

@RestController
@RequestMapping("wishlist/wishlistItem")
public class WishlistItemController {
    @Autowired
    private WishlistItemService wishlistItemService;
}
