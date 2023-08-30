package com.aining.mall.wishlist.vo;

import lombok.Data;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/29 17:45
 */

@Data
public class WishlistItemVo {
    /**
     * 心愿单id
     */
    private Long wlId;
    /**
     * spu_id
     */
    private Long spuId;
    /**
     * spu_name
     */
    private String spuName;
    /**
     * spu_pic
     */
    private String spuPic;
    /**
     * 品牌
     */
    private String spuBrand;
    /**
     * 商品分类id
     */
    private Long categoryId;
    /**
     * 添加该条目的用户id
     */
    private String createUserName;
}
