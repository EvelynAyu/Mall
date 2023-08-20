package com.aining.mall.product.service.impl;

import com.aining.mall.product.entity.AttrEntity;
import com.aining.mall.product.service.AttrService;
import com.aining.mall.product.vo.AttrGroupWithAttrsVo;
import com.aining.mall.product.vo.voForItem.SpuItemAttrGroupVo;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.Query;

import com.aining.mall.product.dao.AttrGroupDao;
import com.aining.mall.product.entity.AttrGroupEntity;
import com.aining.mall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        // key是用于多字段模糊检索
        String key = (String)params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if(!StringUtils.isNullOrEmpty(key)){
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        // 前端规定，如果没有传三级分类id，则三级分类的id传0（查所有）
        if(catelogId == 0){
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }else{
            // 如果有分类id，则按照三级分类id查询
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
    }

    /**
     * 根据分类id查出属性分组及分组里的属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithattrsByCatelogId(Long catelogId) {
        // 1.查询分组信息
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        // 2. 通过每个分组查出分组中的属性
        List<AttrGroupWithAttrsVo> attrGroupWithAttrsVos = groupEntities.stream().map((groupEntity) -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(groupEntity, attrGroupWithAttrsVo);
            // 查询分组下的所有属性
            List<AttrEntity> attrs = attrService.getRelationAttr(groupEntity.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(attrs);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());
        return attrGroupWithAttrsVos;

    }

    /**
     * 查询当前Spu对应所有属性的分组和对应的值
     *
     * @param spuId
     * @param catalogId
     * @return
     */
    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithattrsBySpuId(Long spuId, Long catalogId) {
        /**
         * 1. 当前spu对应的属性分组：spuId->catalogId->attr_group_id
         * 2. 查询分组对应的属性值
         */
        AttrGroupDao baseMapper = this.getBaseMapper();
        List<SpuItemAttrGroupVo> vos = baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        return vos;

    }
}