package com.aining.mall.wishlist.web;

import com.aining.common.utils.R;
import com.aining.mall.wishlist.entity.InviteEntity;
import com.aining.mall.wishlist.service.InviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/29 19:31
 */

@Controller
public class InviteWebController {
    @Autowired
    InviteService inviteService;

    /**
     * 创建新的邀请码
     */
    @GetMapping("/generateInviteCode")
    @ResponseBody
    public R generateInviteCode(@RequestParam("wishlistId") Long wishlistId, Model model){
        // 为被选择的愿望清单创建邀请码
        String inviteCode = inviteService.generateInviteCode(wishlistId);
        return R.ok().put("inviteCode", inviteCode);
    }

    /**
     * 接受邀请
     */
    @GetMapping("/invite/{inviteCode}")
    public String invite(Model model, @PathVariable String inviteCode) {
        // 通过邀请码找到对应的愿望清单和创建者
        InviteEntity inviteEntity = inviteService.getWishlistByInviteCode(inviteCode);
        model.addAttribute("inviteEntity",inviteEntity);
        return "invite";
    }
}
