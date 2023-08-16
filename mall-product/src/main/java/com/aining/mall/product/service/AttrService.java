package com.aining.mall.product.service;

import com.aining.mall.product.entity.ProductAttrValueEntity;
import com.aining.mall.product.vo.AttrRespVo;
import com.aining.mall.product.vo.AttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aining.common.utils.PageUtils;
import com.aining.mall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-16 21:44:18
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);


    PageUtils queryAttrPage(Map<String, Object> params, Long catelogId, String attrType);

    AttrRespVo getAttrDetail(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    List<Long> selectSearchAttrIds(List<Long> attrIds);
}

