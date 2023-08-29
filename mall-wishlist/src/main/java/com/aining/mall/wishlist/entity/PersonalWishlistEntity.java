package com.aining.mall.wishlist.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/28 21:33
 */

@Data
@TableName("wlms_personal_wishlist")
public class PersonalWishlistEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Wishlist id
     */
    @TableId
    private Long wlId;

    /**
     * wishlish 名称
     */
    private String wlName;

    /**
     * wish list状态：0-private,1-shared
     */
    private Integer wlStatus;

    /**
     * 创建人id
     */
    private Long createUserId;

    /**
     *创建人姓名
     */
    private String createUserName;
}
