package com.aining.mall.wishlist.service.impl;

import com.aining.common.vo.MemberResponseVo;
import com.aining.mall.wishlist.dao.PersonalWishlistDao;
import com.aining.mall.wishlist.entity.PersonalWishlistEntity;
import com.aining.mall.wishlist.interceptor.LoginUserInterceptor;
import com.aining.mall.wishlist.service.PersonalWishlistService;
import com.aining.mall.wishlist.vo.PersonalWishlistVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 22:03
 */
@Service("personalWishlistService")
public class PersonalWishlistServiceImpl extends ServiceImpl<PersonalWishlistDao,PersonalWishlistEntity> implements PersonalWishlistService {
    @Override
    public List<PersonalWishlistVo> getPersonalWishlist() {

        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        Long createUserId = memberResponseVo.getId();

        // 根据人员id和wlStatus状态，查出私人的wishlist
        //TODO 可能出错
        List<PersonalWishlistEntity> personalWishlists = this.baseMapper.selectList(new QueryWrapper<PersonalWishlistEntity>()
                .eq("create_user_id", createUserId).eq("wl_status",0));

        List<PersonalWishlistVo> personalWishlistVos = personalWishlists.stream().map((personalWishlist) -> {
            PersonalWishlistVo personalWishlistVo = new PersonalWishlistVo();
            BeanUtils.copyProperties(personalWishlist, personalWishlistVo);
            return personalWishlistVo;
        }).collect(Collectors.toList());

        return personalWishlistVos;
    }

    @Override
    public void saveWishlist(String wlName) {
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        PersonalWishlistEntity personalWishlistEntity = new PersonalWishlistEntity();

        // 保存愿望清单的名字
        personalWishlistEntity.setWlName(wlName);
        personalWishlistEntity.setWlStatus(0);
        // 保存创建人的id
        Long createUserId = memberResponseVo.getId();
        personalWishlistEntity.setCreateUserId(createUserId);

        // 保存创建人名称
        String createUserName = memberResponseVo.getNickname();
        personalWishlistEntity.setCreateUserName(createUserName);

        // 保存
        this.save(personalWishlistEntity);
    }

    /**
     * 更新愿望清单
     * @param wlId
     * @param wlName
     */
    @Override
    public void updateWishlist(Long wlId, String wlName) {
        PersonalWishlistEntity personalWishlist = new PersonalWishlistEntity();
        personalWishlist.setWlId(wlId);
        personalWishlist.setWlName(wlName);
        this.updateById(personalWishlist);
    }

    @Override
    public void updateWishlistStatus(Long wishlistId) {
        PersonalWishlistEntity personalWishlistEntity = this.getById(wishlistId);
        personalWishlistEntity.setWlStatus(1);
        this.updateById(personalWishlistEntity);
    }
}