package com.aining.mall.wishlist.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/29 22:14
 */
@Data
public class PersonalWishlistVo {
    /**
     * Wishlist id
     */
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
