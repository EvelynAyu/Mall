package com.aining.mall.wishlist.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 邀请码
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/29 19:04
 */

@Data
@TableName("wlms_invite")
public class InviteEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 邀请码id
     */
    @TableId
    private Long id;

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * Wishlist id
     */
    private Long wlId;

    /**
     * wishlist 名称
     */
    private String wlName;

    /**
     * 创建人id
     */
    private Long ownerId;

    /**
     *创建人姓名
     */
    private String ownerName;

}
