package com.aining.mall.wishlist.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

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
     * skuId
     */
    private Long skuId;
    /**
     * sku名称
     */
    private String title;
    /**
     * sku图片
     */
    private String image;
    /**
     * 价格
     */
    private BigDecimal price;
    /**
     * 添加该条目的用户id
     */
    private Long createUserId;
    /**
     *  添加该条目的用户姓名
     */
    private String createUserName;

    /**
     * 商品套餐属性
     */
    @TableField(exist = false)
    private List<String> skuAttrValues;
}
