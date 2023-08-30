package com.aining.mall.wishlist.web;

import com.aining.common.utils.R;
import com.aining.mall.wishlist.entity.WishlistItemEntity;
import com.aining.mall.wishlist.service.WishlistItemService;
import com.aining.mall.wishlist.vo.WishlistItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/29 17:27
 */
@Controller
public class WishlistItemWebController {
    @Autowired
    WishlistItemService wishlistItemService;

    /**
     * 展示当前wishlist中的商品
     */
    @GetMapping("/showItemInWishlist/{wlId}")
    public String showItemInWishlist(@PathVariable("wlId") Long wlId, Model model){
        List<WishlistItemEntity> wishlistItemLists = wishlistItemService.showItemInWishlist(wlId);
        System.out.println(wishlistItemLists.toString());
        model.addAttribute("wishlistItemLists",wishlistItemLists);
        model.addAttribute("wishlistId",wlId);
        return "itemInWishlist";
    }

    /**
     * 向wishlist中添加商品
     */
    @GetMapping("/addToWishlist/{wishlistId}/{skuId}")
    public String addToWishlist(@PathVariable("wishlistId") Long wishlistId,
                                @PathVariable("skuId") Long skuId,
                                RedirectAttributes attributes) {
        wishlistItemService.addToWishlist(skuId,wishlistId);
        attributes.addAttribute("skuId",skuId);
        return "redirect:http://item.mall.com//{skuId}.html";
    }

    /**
     * 删除心愿单中的物品
     * @param skuId
     * @param wlId
     * @return
     */
    @GetMapping(value = "/deleteItemInWishlist")
    public String deleteItem(@RequestParam("skuId") Long skuId,
                             @RequestParam("wlId") Long wlId) {
        wishlistItemService.deleteItemInWishlist(skuId,wlId);
        return "redirect:http://wishlist.mall.com/showItemInWishlist/" + wlId;
    }
}
