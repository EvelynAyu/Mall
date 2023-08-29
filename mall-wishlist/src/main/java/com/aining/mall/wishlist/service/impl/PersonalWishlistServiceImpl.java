package com.aining.mall.wishlist.service.impl;

import com.aining.common.vo.MemberResponseVo;
import com.aining.mall.wishlist.dao.PersonalWishlistDao;
import com.aining.mall.wishlist.entity.PersonalWishlistEntity;
import com.aining.mall.wishlist.interceptor.LoginUserInterceptor;
import com.aining.mall.wishlist.service.PersonalWishlistService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 22:03
 */
@Service("personalWishlistService")
public class PersonalWishlistServiceImpl extends ServiceImpl<PersonalWishlistDao,PersonalWishlistEntity> implements PersonalWishlistService {
    @Override
    public List<PersonalWishlistEntity> getPersonalWishlist() {

        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        Long createUserId = memberResponseVo.getId();

        List<PersonalWishlistEntity> personalWishlist = this.baseMapper.selectList(new QueryWrapper<PersonalWishlistEntity>().eq("create_user_id", createUserId));
        return personalWishlist;
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
}