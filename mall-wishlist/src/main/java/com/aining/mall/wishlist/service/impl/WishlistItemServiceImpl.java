package com.aining.mall.wishlist.service.impl;

import com.aining.common.utils.R;
import com.aining.common.vo.MemberResponseVo;
import com.aining.mall.wishlist.dao.WishlistItemDao;
import com.aining.mall.wishlist.entity.WishlistItemEntity;
import com.aining.mall.wishlist.feign.ProductFeignService;
import com.aining.mall.wishlist.interceptor.LoginUserInterceptor;
import com.aining.mall.wishlist.service.WishlistItemService;
import com.aining.mall.wishlist.vo.SkuInfoVo;
import com.aining.mall.wishlist.vo.WishlistItemVo;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 22:10
 */

@Service("wishlistItemService")
public class WishlistItemServiceImpl extends ServiceImpl<WishlistItemDao, WishlistItemEntity> implements WishlistItemService {

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public List<WishlistItemEntity> showItemInWishlist(Long wlId) {
        List<WishlistItemEntity> wishlistItemEntities = this.baseMapper.selectList(
                new QueryWrapper<WishlistItemEntity>().eq("wl_id", wlId));
        for (WishlistItemEntity wishlistItemEntity : wishlistItemEntities) {
            //2、远程查询skuAttrValues组合信息
            Long skuId = wishlistItemEntity.getSkuId();
            List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
            wishlistItemEntity.setSkuAttrValues(skuSaleAttrValues);
        }
        return wishlistItemEntities;
    }

    @Override
    public void addToWishlist(Long skuId, Long wlId) {
        WishlistItemEntity wishlistItemEntity = new WishlistItemEntity();

        // 获取当前用户的姓名和id
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        Long memberId = memberResponseVo.getId();
        wishlistItemEntity.setCreateUserId(memberId);

        String memberName = memberResponseVo.getNickname();
        wishlistItemEntity.setCreateUserName(memberName);

        wishlistItemEntity.setWlId(wlId);
        wishlistItemEntity.setSkuId(skuId);
        //1、远程查询当前要添加商品的信息
        R productSkuInfo = productFeignService.getSkuInfo(skuId);
        SkuInfoVo skuInfo = productSkuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
        wishlistItemEntity.setTitle(skuInfo.getSkuTitle());
        wishlistItemEntity.setImage(skuInfo.getSkuDefaultImg());
        wishlistItemEntity.setPrice(skuInfo.getPrice());

        this.save(wishlistItemEntity);
    }

    @Override
    public void deleteItemInWishlist(Long skuId, Long wlId) {
        WishlistItemEntity wishlistItemEntity = this.baseMapper.selectOne(new QueryWrapper<WishlistItemEntity>().eq("sku_id", skuId).eq("wl_id", wlId));
        Long id = wishlistItemEntity.getId();
        this.removeById(id);
    }
}
