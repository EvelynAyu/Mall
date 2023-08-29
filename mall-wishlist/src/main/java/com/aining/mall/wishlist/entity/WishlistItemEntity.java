package com.aining.mall.wishlist.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 21:34
 */

@Data
@TableName("wlms_wishlist_item")
public class WishlistItemEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 心愿单条目id
     */
    @TableId
    private Long id;
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
