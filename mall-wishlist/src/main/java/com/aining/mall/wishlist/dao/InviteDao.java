package com.aining.mall.wishlist.dao;

import com.aining.mall.wishlist.entity.InviteEntity;
import com.aining.mall.wishlist.entity.PersonalWishlistEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/29 19:24
 */
@Mapper
public interface InviteDao extends BaseMapper<InviteEntity> {
}
