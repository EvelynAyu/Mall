package com.aining.mall.wishlist.controller;

import com.aining.common.utils.R;
import com.aining.mall.wishlist.entity.PersonalWishlistEntity;
import com.aining.mall.wishlist.service.PersonalWishlistService;
import com.aining.mall.wishlist.vo.PersonalWishlistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 私人分享单的信息
 *
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 22:12
 */

@RestController
@RequestMapping("personalWishlist")
public class PersonalWishlistController {
    @Autowired
    private PersonalWishlistService personalWlService;

    /**
     * 获取个人心愿单
     */
    @GetMapping("/getPersonalWishlist")
    public Map<String, List<PersonalWishlistVo>> getPersonalWishlist(){
        List<PersonalWishlistVo> personalWishlist = personalWlService.getPersonalWishlist();
        Map<String, List<PersonalWishlistVo>> result = new HashMap<>();
        result.put("personalWishlist", personalWishlist);
        return result;
    }
}
