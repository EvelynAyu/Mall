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
@TableName("wlms_shared_wishlist")
public class SharedWishlistEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分享状态的Wishlist id
     */
    @TableId
    private Long wlId;

    /**
     * wishlish 名称
     */
    private String wlName;

    /**
     * 分享人id
     */
    private Long collaboratorId;

    /**
     * 分享人姓名
     */
    private String collaboratorName;
}
