package com.aining.mall.product.service.impl;

import com.aining.common.constant.ProductConstant;
import com.aining.mall.product.dao.AttrAttrgroupRelationDao;
import com.aining.mall.product.dao.AttrGroupDao;
import com.aining.mall.product.dao.CategoryDao;
import com.aining.mall.product.entity.*;
import com.aining.mall.product.service.CategoryService;
import com.aining.mall.product.vo.AttrRespVo;
import com.aining.mall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.Query;

import com.aining.mall.product.dao.AttrDao;
import com.aining.mall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        // 将页面传回VO封装到PO(Entity)中
        BeanUtils.copyProperties(attr, attrEntity);
        // 保存attrEntity数据
        this.save(attrEntity);

        // 保存Group和Attr之间的关联:只有baseAttr有分组，即只有baseAttr才需要进行关联
        if(attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null){
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryAttrPage(Map<String, Object> params, Long catelogId, String attrType) {
        String key = (String) params.get("key");
        // AttrType的枚举值
        int baseCode = ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode();
        int saleCode = ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode();
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                                            .eq("attr_type", "base".equalsIgnoreCase(attrType)?baseCode:saleCode);

        // catelogId == 0查询所有,catelogId != 0则需要对wrapper添加条件
        if(catelogId != 0){
            wrapper.eq("catelog_id", catelogId);
        }
        // 模糊查询
        if (StringUtils.isNotBlank(key)) {
            wrapper.and((obj)->{
                obj.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params), wrapper);

        // 从page中得到AttrEntity
        List<AttrEntity> attrEntityList = page.getRecords();

        // 封装attrRespVos，返回给前端
        List<AttrRespVo> attrRespVos = attrEntityList.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            // 将attrEntityList中的每个attrEntity的属性值拷贝到AttrRespVo中
            BeanUtils.copyProperties(attrEntity, attrRespVo);

            // 此时attrRespVo还缺少catelogName和groupName属性,需要对他们进行设置
            // 设置groupName:得到attr_id -> 在attr_group_relation中获取groupId -> 在group表中获取groupName
            // baseAttr才需要设置分组信息，saleAttr没有分组信息
            if(attrType.equalsIgnoreCase("base")){
                Long attrId = attrEntity.getAttrId();
                AttrAttrgroupRelationEntity attrgroupRelation = attrAttrgroupRelationDao.selectOne(
                        new QueryWrapper<AttrAttrgroupRelationEntity>()
                                .eq("attr_id", attrId));
                if (attrgroupRelation != null && attrgroupRelation.getAttrGroupId() != null) {
                    Long groupId = attrgroupRelation.getAttrGroupId();
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(groupId);
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            // 设置catelogName:得到attr_id -> 在attr表中获取catelogId -> 在category表中获取catelogName
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                String catelogName = categoryEntity.getName();
                attrRespVo.setCatelogName(catelogName);
            }
            return attrRespVo;
        }).collect(Collectors.toList());

        PageUtils pageUtils = new PageUtils(page);
        // 将attrRespVos设置到pageUtils中，返回给前端
        pageUtils.setList(attrRespVos);

        return pageUtils;
    }

    /**
     * 修改时回显
     * @param attrId
     * @return
     */
    @Cacheable(value = "attr",key = "'attrInfo:'+#root.args[0]")
    @Override
    public AttrRespVo getAttrDetail(Long attrId) {
        // 根据attrId查处attrEntity
        AttrEntity attrEntity = this.getById(attrId);
        // 拷贝数据
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity, attrRespVo);

        // 如果修改的是baseAttr才有分组信息，saleAttr没有分组信息
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 封装所属的attrGroupId
            AttrAttrgroupRelationEntity attrgroupRelation = attrAttrgroupRelationDao.selectOne(
                    new QueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq("attr_id", attrId));
            if (attrgroupRelation != null) {
                Long attrGroupId = attrgroupRelation.getAttrGroupId();
                // 封装attrGroupId
                attrRespVo.setAttrGroupId(attrGroupId);
                // 封装attrGroupName
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
        }

        // 封装所属的分类路径
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrRespVo.setCatelogPath(catelogPath);
        // 封装分类名称
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (categoryEntity != null) {
            attrRespVo.setCatelogName(categoryEntity.getName());
        }
        return attrRespVo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAttr(AttrVo attr) {
        // 更新数据
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            Long attrId = attr.getAttrId();
            // 判断当前规格参数有没有关联分组
            Integer count = attrAttrgroupRelationDao.selectCount(
                    new QueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq("attr_id", attrId));

            // 更新attr与attrGroup的关联表
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            // 设置attrGroupId和attrId
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attr.getAttrId());

            // 原先的规格参数关联了分组:更新
            if(count > 0){
                // 更新关联表中的attrGroupId
                attrAttrgroupRelationDao.update(relationEntity,
                        new UpdateWrapper<AttrAttrgroupRelationEntity>()
                                .eq("attr_id", attrId));
            }else{
                // 原先的规格参数没有关联分组:新增(没有关联则关系表中没有与该attr_id对应的记录，无法update,只能用新增)
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }
    }

    /**
     * 根据分组groupid查找相关联的基本属性baseAttr
     * 由 attrgroupId 查关联表得到 attrIds -> 查询 attr 表得到attrList
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(
                                                    new QueryWrapper<AttrAttrgroupRelationEntity>()
                                                            .eq("attr_group_id", attrgroupId));
        // 由 attrgroupId 查关联表得到 attrIds
        List<Long> attrIds = relationEntities.stream().map((relationEntity) -> {
            return relationEntity.getAttrId();
        }).collect(Collectors.toList());
        if(attrIds == null || attrIds.size() == 0){
            return null;
        }
        Collection<AttrEntity> attrEntities = this.listByIds(attrIds);
        return (List<AttrEntity>) attrEntities;
    }

    /**
     * 获取当前分组没有关联的所有属性
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        // 1. 当前分组只能关联自己所属分类下的所有属性
        // 根据groupId获取所属分类catelogId
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        // 2. 当前分组只能关联没有被别的分组关联的属性
        // 找到当前分类下的其他分组(剔除当前分组)
        List<AttrGroupEntity> groupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>()
                                            .eq("catelog_id", catelogId));
        // 获取其他分组的groupIds
        List<Long> groupIds = groupEntities.stream().map((groupEntity) -> {
            return groupEntity.getAttrGroupId();
        }).collect(Collectors.toList());

        // 找到其他分组关联的属性
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(
                                                new QueryWrapper<AttrAttrgroupRelationEntity>()
                                                        .in("attr_group_id", groupIds));
        // 获取关联的属性的id
        List<Long> attrIds = relationEntities.stream().map((relationEntity) -> {
            return relationEntity.getAttrId();
        }).collect(Collectors.toList());

        // 从当前分类的所有属性中移除被其他分组关联的baseAttr
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("catelog_id", catelogId)
                .eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if(attrIds != null && attrIds.size() != 0){
            wrapper.notIn("attr_id", attrIds);
        }

        String key = (String) params.get("key");
        if(StringUtils.isNotBlank(key)){
            wrapper.and((obj)->{
                obj.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        return this.baseMapper.selectSearchAttrIds(attrIds);

    }

}