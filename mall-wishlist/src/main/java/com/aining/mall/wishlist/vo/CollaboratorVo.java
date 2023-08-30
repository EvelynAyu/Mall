package com.aining.mall.wishlist.vo;

import lombok.Data;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/29 16:00
 */

@Data
public class CollaboratorVo {
    /**
     * 分享人id
     */
    private Long collaboratorId;

    /**
     * 分享人姓名
     */
    private String collaboratorName;
}
