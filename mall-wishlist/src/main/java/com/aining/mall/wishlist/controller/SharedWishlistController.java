package com.aining.mall.wishlist.controller;

import com.aining.common.utils.R;
import com.aining.mall.wishlist.entity.SharedWishlistEntity;
import com.aining.mall.wishlist.service.SharedWishlistService;
import com.aining.mall.wishlist.vo.PersonalWishlistVo;
import com.aining.mall.wishlist.vo.WishlistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分享状态的wishlist
 *
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 22:15
 */

@RestController
@RequestMapping("sharedWishlist")
public class SharedWishlistController {

    @Autowired
    private SharedWishlistService sharedWishlistService;

    /**
     * 获取共享心愿单
     */
    @GetMapping("/getSharedWishlist")
    public R getPersonalWishlist(){
        List<SharedWishlistEntity> sharedWishlist = sharedWishlistService.getSharedWishlist();
        return R.ok().put("sharedWishlist",sharedWishlist);
    }
}
