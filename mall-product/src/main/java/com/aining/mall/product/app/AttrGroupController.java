package com.aining.mall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.aining.mall.product.entity.AttrEntity;
import com.aining.mall.product.service.AttrAttrgroupRelationService;
import com.aining.mall.product.service.AttrService;
import com.aining.mall.product.service.CategoryService;
import com.aining.mall.product.vo.AttrGroupRelationVo;
import com.aining.mall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.aining.mall.product.entity.AttrGroupEntity;
import com.aining.mall.product.service.AttrGroupService;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.R;



/**
 * 属性分组
 *
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-16 22:11:17
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    /**
     * 列表
     */
    @GetMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId){
        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }


    /**
     * 修改时回显信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        // 根据当前属性所属的分类找到分类路径
        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 获取group关联的baseAttr
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R groupRelationAttr(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> attrEntities = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", attrEntities);
    }

    /**
     * 获取group未关联的baseAttr
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R groupNoRelationAttr(@PathVariable("attrgroupId") Long attrgroupId,
                                 @RequestParam Map<String, Object> params){
        PageUtils page = attrService.getNoRelationAttr(params, attrgroupId);
        return R.ok().put("page", page);
    }

    /**
     * 获取当前分类下所有的属性分组和分组中的属性
     */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttr(@PathVariable("catelogId") Long catelogId){
        List<AttrGroupWithAttrsVo> attrGroupWithAttrsVos = attrGroupService.getAttrGroupWithattrsByCatelogId(catelogId);
        return R.ok().put("data", attrGroupWithAttrsVos);
    }

    /**
     * 添加分组与属性的关联
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> relationVos){
        relationService.saveBatchRelation(relationVos);
        return R.ok();
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除分组
     */
    @PostMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    /**
     * 删除分组关联:可以分组也可以单个删除
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelationAttr(@RequestBody AttrGroupRelationVo[] relationVos){
        relationService.deleteRelation(relationVos);
        return R.ok();
    }
}
