package com.aining.mall.wishlist.web;

import com.aining.common.utils.R;
import com.aining.mall.wishlist.entity.InviteEntity;
import com.aining.mall.wishlist.entity.SharedWishlistEntity;
import com.aining.mall.wishlist.service.SharedWishlistService;
import com.aining.mall.wishlist.vo.CollaboratorVo;
import com.aining.mall.wishlist.vo.WishlistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/29 15:50
 */

@Controller
public class ShareWishlistWebController {
    @Autowired
    SharedWishlistService sharedWishlistService;

    /**
     * 获取共享愿望清单
     */
    @GetMapping("/showSharedWishlist")
    public String wishListPage(Model model) {
        List<SharedWishlistEntity> sharedWishlists = sharedWishlistService.getSharedWishlist();

        model.addAttribute("sharedWishlists",sharedWishlists);
        return "sharedWishlist";
    }

    /**
     * 查看共享愿望清单的成员
     */
    @GetMapping("/showCollaborator/{wlId}")
    public String showCollaborator(@PathVariable("wlId") Long wlId, Model model){
        // 查询当前共享清单中的成员，以表格形式展现人员姓名（或弹窗？）
        List<CollaboratorVo> CollaboratorVo = sharedWishlistService.showCollaborator(wlId);
        model.addAttribute("CollaboratorVo", CollaboratorVo);
        return "collaborator";
    }

    /**
     * 有成员接受邀请
     */
    @GetMapping("/joinWishlist/{codeId}")
    public String joinWishlist(@PathVariable("codeId") Long codeId){
        // 成员加入，更新sharedWishlist数据库中的内容
        sharedWishlistService.updateSharedWishlist(codeId);

        return "redirect:http://wishlist.mall.com/showSharedWishlist";
    }

    /**
     * 获取全部的愿望清单
     */
    /**
     * 查看全部的愿望清单,在"加入愿望清单"处调用
     */
    @GetMapping("/getAllWishlists/{skuId}")
    public String  getAllWishlist(@PathVariable("skuId") Long skuId, Model model){
        System.out.println("调用了获取全部心愿单的代码");
        List<WishlistVo> allWishlists = sharedWishlistService.getAllWishlist();
        model.addAttribute("skuId",skuId);
        model.addAttribute("allWishlists",allWishlists);
        return "allWishlist";
    }
}
