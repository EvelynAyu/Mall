package com.aining.mall.product.vo;

import lombok.Data;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/30 03:13
 */

@Data
public class WishlistVo {
    private Long wishlistId;
    private String wishlistName;
    private Long memberId;
    private String memberName;
}
