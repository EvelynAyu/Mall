package com.aining.mall.product.service.impl;

import com.aining.mall.product.service.CategoryBrandRelationService;
import com.aining.mall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.Query;

import com.aining.mall.product.dao.CategoryDao;
import com.aining.mall.product.entity.CategoryEntity;
import com.aining.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询数据库中的分类，并且以树形结构组装父子分类并返回
     * @return
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. 查询所有分类数据
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);
        // 2. 组装父子树形结构
        // 2.1 找到所有的一级分类
        List<CategoryEntity> levelMenu1 = categoryEntityList.stream().filter(categoryEntity ->
            categoryEntity.getParentCid() == 0
        ).map((categoryCur)->{
            // 将当前菜单的自分类保存进去
            categoryCur.setChildren(getChildrens(categoryCur, categoryEntityList));
            return categoryCur;
        }).sorted((categoryCur1, categoryCur2)->{
            return (categoryCur1.getSort() == null?0:categoryCur1.getSort()) - (categoryCur2.getSort() == null?0:categoryCur2.getSort());
        }).collect(Collectors.toList());


        return  levelMenu1;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 检查当前删除的菜单是否被别的地方引用

        // 批量删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        findParentPath(catelogId, paths);
        Collections.reverse(paths);
        return (Long[]) paths.toArray(new Long[paths.size()]);
    }
    private void findParentPath(Long catelogId, List<Long> paths){
        // 1. 收集当前节点的id
        paths.add(catelogId);
        CategoryEntity category = this.getById(catelogId);
        if(category.getParentCid()!= 0){
            findParentPath(category.getParentCid(), paths);
        }
//        return paths;
    }

    /**
     * 级联更新所有关联的数据
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        // 更新category表格
        this.updateById(category);
        // 同步更新其他关联表中的数据
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    /**
     * 递归查找当前菜单的子菜单
     */
    public List<CategoryEntity> getChildrens(CategoryEntity categoryCur, List<CategoryEntity> categoryEntityList){
        List<CategoryEntity> childrens = categoryEntityList.stream().filter(categoryEntity -> {
            // 当菜单的父id为categoryCur的id(catId),则该菜单为categoryCurd的子菜单
            return categoryEntity.getParentCid().equals(categoryCur.getCatId());
        }).map(categoryEntity -> {
            // 找到子菜单的子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, categoryEntityList));
            return categoryEntity;
        }).sorted((categoryCur1, categoryCur2) -> {
            return (categoryCur1.getSort() == null?0:categoryCur1.getSort()) - (categoryCur2.getSort() == null?0:categoryCur2.getSort());
        }).collect(Collectors.toList());

        return childrens;
    }


    /**
     * 首页web查询所有的一级分类
     * @return
     */
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid",0L));
    }

    /**
     * 根据一级分类查询二三级分类
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        System.out.println("查询了数据库");

        //将数据库的多次查询变为一次
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        //1、查出所有分类
        //1、1）查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //封装数据
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1、每一个的一级分类,查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            //2、封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());

                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());

                    if (level3Catelog != null) {
                        List<Catelog2Vo.Category3Vo> category3Vos = level3Catelog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(category3Vos);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));

        return parentCid;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList,Long parentCid) {
        List<CategoryEntity> categoryEntities = selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return categoryEntities;
        // return this.baseMapper.selectList(
        //         new QueryWrapper<CategoryEntity>().eq("parent_cid", parentCid));
    }

}