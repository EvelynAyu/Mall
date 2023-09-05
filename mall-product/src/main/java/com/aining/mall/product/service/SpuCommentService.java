package com.aining.mall.product.service;

import com.aining.mall.product.vo.CommentVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aining.common.utils.PageUtils;
import com.aining.mall.product.entity.SpuCommentEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品评价
 *
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-16 21:44:19
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveCommnet(Long spuId, String content, Integer commentType);

    List<CommentVo> getSpuAllComment(Long skuId);

    void saveReply(Long commentId, String content, Integer commentType);
}

