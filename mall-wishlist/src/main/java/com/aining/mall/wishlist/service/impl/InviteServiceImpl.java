package com.aining.mall.wishlist.service.impl;
import com.aining.common.vo.MemberResponseVo;
import com.aining.mall.wishlist.dao.InviteDao;
import com.aining.mall.wishlist.entity.InviteEntity;
import com.aining.mall.wishlist.entity.PersonalWishlistEntity;
import com.aining.mall.wishlist.interceptor.LoginUserInterceptor;
import com.aining.mall.wishlist.service.InviteService;
import com.aining.mall.wishlist.service.PersonalWishlistService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/29 19:32
 */

@Service("inviteService")
public class InviteServiceImpl extends ServiceImpl<InviteDao, InviteEntity> implements InviteService{

    @Autowired
    PersonalWishlistService personalWishlistService;

    /**
     * 生成邀请码，保存并返回邀请码
     * @param wlId
     * @return
     */
    @Override
    public String generateInviteCode(Long wlId) {
        // 查询邀请码表，是否已经有该愿望清单的邀请码存在
        String inviteCode = "";
        InviteEntity inviteEntity = this.baseMapper.selectOne(new QueryWrapper<InviteEntity>().eq("wl_id", wlId));
        if(inviteEntity != null){
            inviteCode = inviteEntity.getInviteCode();
        }else{
            // 如果邀请码表中不存在该愿望清单的邀请码,则随机生成8为邀请码
            inviteCode = RandomStringUtils.random(8,false,true);
            // 保存到邀请码表中
            InviteEntity inviteEntitySave = new InviteEntity();
            inviteEntitySave.setInviteCode(inviteCode);
            inviteEntitySave.setWlId(wlId);

            // 查询该愿望清单对应到名字和主人
            PersonalWishlistEntity personalWishlist = personalWishlistService.getById(wlId);
            inviteEntitySave.setWlName(personalWishlist.getWlName());
            inviteEntitySave.setOwnerId(personalWishlist.getCreateUserId());
            inviteEntitySave.setOwnerName(personalWishlist.getCreateUserName());
            // 保存邀请码
            this.save(inviteEntitySave);
        }
        return inviteCode;
    }

    @Override
    public InviteEntity getWishlistByInviteCode(String inviteCode) {

        InviteEntity inviteEntity = this.baseMapper.selectOne(
                new QueryWrapper<InviteEntity>().eq("invite_code", inviteCode));
        return inviteEntity;
    }
}
