package com.aining.mall.wishlist.service.impl;

import com.aining.common.vo.MemberResponseVo;
import com.aining.mall.wishlist.dao.SharedWishlistDao;
import com.aining.mall.wishlist.entity.InviteEntity;
import com.aining.mall.wishlist.entity.SharedWishlistEntity;
import com.aining.mall.wishlist.interceptor.LoginUserInterceptor;
import com.aining.mall.wishlist.service.InviteService;
import com.aining.mall.wishlist.service.PersonalWishlistService;
import com.aining.mall.wishlist.service.SharedWishlistService;
import com.aining.mall.wishlist.vo.CollaboratorVo;
import com.aining.mall.wishlist.vo.PersonalWishlistVo;
import com.aining.mall.wishlist.vo.WishlistVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 22:07
 */
@Service("sharedWishlistService")
public class SharedWishlistServiceImpl extends ServiceImpl<SharedWishlistDao, SharedWishlistEntity> implements SharedWishlistService {
    @Autowired
    PersonalWishlistService personalWishlistService;

    @Autowired
    InviteService inviteService;

    @Override
    public List<SharedWishlistEntity> getSharedWishlist() {
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        Long collaboratorId = memberResponseVo.getId();

        List<SharedWishlistEntity> sharedWishlist = this.baseMapper.selectList(new QueryWrapper<SharedWishlistEntity>().eq("collaborator_id", collaboratorId));
        return sharedWishlist;
    }

    @Override
    public List<CollaboratorVo> showCollaborator(Long wlId) {
        List<SharedWishlistEntity> sharedWishlists = this.baseMapper.selectList(new QueryWrapper<SharedWishlistEntity>().eq("wl_id",wlId));
        List<CollaboratorVo> collaboratorVos = sharedWishlists.stream().map((sharedWishlist) -> {
            CollaboratorVo collaboratorVo = new CollaboratorVo();
            collaboratorVo.setCollaboratorId(sharedWishlist.getCollaboratorId());
            collaboratorVo.setCollaboratorName(sharedWishlist.getCollaboratorName());
            return collaboratorVo;
        }).collect(Collectors.toList());
        return collaboratorVos;
    }

    @Override
    public void updateSharedWishlist(Long codeId) {
        InviteEntity inviteEntity = inviteService.getById(codeId);
        // 取出inviteEntity中的属性
        Long wishlistId = inviteEntity.getWlId();
        String wishlistName = inviteEntity.getWlName();
        Long ownerId = inviteEntity.getOwnerId();
        String ownerName = inviteEntity.getOwnerName();

        // 查询shared表中是否已经有owner的数据
        Integer count = this.baseMapper.selectCount(new QueryWrapper<SharedWishlistEntity>()
                                                    .eq("wl_id", wishlistId)
                                                    .eq("collaborator_id", ownerId));
        if(count == 0){
            // 将创建者的信息添加进去
            SharedWishlistEntity ownerSharedWishlist = new SharedWishlistEntity();
            ownerSharedWishlist.setWlId(wishlistId);
            ownerSharedWishlist.setWlName(wishlistName);
            ownerSharedWishlist.setCollaboratorId(ownerId);
            ownerSharedWishlist.setCollaboratorName(ownerName);
            this.save(ownerSharedWishlist);
        }
        // 将参与者的信息加入sharedWishlist中
        SharedWishlistEntity coSharedWishlist = new SharedWishlistEntity();

        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        Long coId = memberResponseVo.getId();
        String coName = memberResponseVo.getNickname();

        coSharedWishlist.setWlId(wishlistId);
        coSharedWishlist.setWlName(wishlistName);
        coSharedWishlist.setCollaboratorId(coId);
        coSharedWishlist.setCollaboratorName(coName);
        this.save(coSharedWishlist);

        // 更新personalWishlist中的Status为shared
        personalWishlistService.updateWishlistStatus(wishlistId);
    }

    /**
     * 获取全部的心愿单
     */
    @Override
    public List<WishlistVo> getAllWishlist(){
        // 查询私人的愿望清单
        List<PersonalWishlistVo> personalWishlists = personalWishlistService.getPersonalWishlist();
        List<WishlistVo> wishlistVoList = personalWishlists.stream().map((personalWishlist) -> {
            WishlistVo wishlistVo = new WishlistVo();
            wishlistVo.setWishlistId(personalWishlist.getWlId());
            wishlistVo.setWishlistName(personalWishlist.getWlName());
            wishlistVo.setMemberId(personalWishlist.getCreateUserId());
            wishlistVo.setMemberName(personalWishlist.getCreateUserName());
            return wishlistVo;
        }).collect(Collectors.toList());

        // 查询共享愿望清单
        List<SharedWishlistEntity> sharedWishlists = getSharedWishlist();
        for (SharedWishlistEntity sharedWishlist : sharedWishlists) {
            WishlistVo wishlistVo = new WishlistVo();
            wishlistVo.setWishlistId(sharedWishlist.getWlId());
            wishlistVo.setWishlistName(sharedWishlist.getWlName());
            wishlistVo.setMemberId(sharedWishlist.getCollaboratorId());
            wishlistVo.setMemberName(sharedWishlist.getCollaboratorName());
            wishlistVoList.add(wishlistVo);
        }
        // 返回全部的wishlist
        return wishlistVoList;
    }
}
