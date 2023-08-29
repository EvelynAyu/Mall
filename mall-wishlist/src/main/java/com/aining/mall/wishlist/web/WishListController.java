package com.aining.mall.wishlist.web;

import com.aining.common.vo.MemberResponseVo;
import com.aining.mall.wishlist.entity.PersonalWishlistEntity;
import com.aining.mall.wishlist.service.PersonalWishlistService;
import com.aining.mall.wishlist.vo.PersonalWishlishVo;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

import static com.aining.common.constant.AuthServerConstant.LOGIN_USER;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/29 02:47
 */

@Controller
public class WishListController {

    @Autowired
    PersonalWishlistService personalWlService;

    /**
     * 获取私人愿望清单
     */
    @GetMapping("/showWishlist")
    public String wishListPage(Model model) {
        List<PersonalWishlistEntity> personalWishlist = personalWlService.getPersonalWishlist();

        model.addAttribute("personalWishlist",personalWishlist);
        return "index";
    }

    /**
     * 创建私人愿望清单,创建完成之后跳转到showWishlist页面
     * 参考登录页
     */
    @PostMapping("/createWishlist")
    public String createWishlist(@RequestParam("wlName")String wlName){
        personalWlService.saveWishlist(wlName);
        // 重定向到获取私人清单页面，刷新数据
        return "redirect:http://wishlist.mall.com/showWishlist";
    }
}
