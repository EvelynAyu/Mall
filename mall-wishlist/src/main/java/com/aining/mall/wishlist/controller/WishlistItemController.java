package com.aining.mall.wishlist.controller;

import com.aining.common.utils.R;
import com.aining.mall.wishlist.service.WishlistItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

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
