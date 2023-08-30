package com.aining.mall.wishlist.web;

import com.aining.mall.wishlist.entity.PersonalWishlistEntity;
import com.aining.mall.wishlist.service.PersonalWishlistService;
import com.aining.mall.wishlist.vo.PersonalWishlistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/29 02:47
 */

@Controller
public class PersonalWishListWebController {

    @Autowired
    PersonalWishlistService personalWlService;

    /**
     * 获取私人愿望清单
     */
    @GetMapping("/showPersonalWishlist")
    public String wishListPage(Model model) {
        List<PersonalWishlistVo> personalWishlists = personalWlService.getPersonalWishlist();
        model.addAttribute("personalWishlists",personalWishlists);
        return "personalWishlist";
    }

    /**
     * 创建私人愿望清单,创建完成之后跳转到showWishlist页面
     * 参考登录页
     */
    @PostMapping("/createPersonalWishlist")
    public String createWishlist(@RequestParam("wlName")String wlName){
        personalWlService.saveWishlist(wlName);
        // 重定向到获取私人清单页面，刷新数据
        return "redirect:http://wishlist.mall.com/showPersonalWishlist";
    }

    /**
     * 更新私人愿望清单
     */
    @GetMapping("/updatePersonalWishlist")
    public String updateWishlist(@RequestParam("wlId") Long wlId,
                                 @RequestParam("wlName") String wlName){
        personalWlService.updateWishlist(wlId,wlName);
        return "redirect:http://wishlist.mall.com/showPersonalWishlist";
    }

    /**
     * 删除私人愿望清单，需要删除共享信息和清单中的物品？
     */
    @GetMapping("/deletePersonalWishlist")
    public String deleteWishlist(@RequestParam("wlId") Long wlId){
        personalWlService.removeById(wlId);
        return "redirect:http://wishlist.mall.com/showPersonalWishlist";
    }
}
