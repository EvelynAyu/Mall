package com.aining.mall.ware.service.impl;

import com.aining.common.constant.WareConstant;
import com.aining.mall.ware.entity.PurchaseDetailEntity;
import com.aining.mall.ware.service.PurchaseDetailService;
import com.aining.mall.ware.service.WareSkuService;
import com.aining.mall.ware.vo.MergeVo;
import com.aining.mall.ware.vo.PurchaseDoneVo;
import com.aining.mall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.Query;

import com.aining.mall.ware.dao.PurchaseDao;
import com.aining.mall.ware.entity.PurchaseEntity;
import com.aining.mall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    PurchaseDetailService detailService;
    @Autowired
    WareSkuService wareSkuService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        // status = 0:刚新建，status=1：刚分配但还未被
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0).or().eq("status",1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if(purchaseId == null){
            //1、没有指定采购单时会新建一个采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();

            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        //TODO 确认采购需求状态是0,1才可以合并

        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();

            detailEntity.setId(i);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return detailEntity;
        }).collect(Collectors.toList());


        detailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    /**
     *
     * @param ids 采购单id
     */
    @Override
    public void received(List<Long> ids) {
        //1、确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item->{
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        //2、改变采购单的状态
        this.updateBatchById(collect);

        //3、改变采购项的状态
        collect.forEach((item)->{
            List<PurchaseDetailEntity> entities = detailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> detailEntities = entities.stream().map(entity -> {
                PurchaseDetailEntity entity1 = new PurchaseDetailEntity();
                entity1.setId(entity.getId());
                entity1.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return entity1;
            }).collect(Collectors.toList());
            detailService.updateBatchById(detailEntities);
        });
    }

    /**
     * 完成采购
     * @param purchaseDoneVo
     */
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        // 如果所有采购需求都成功则采购单成功，有采购需求不成功则采购单有异常,因此需要一个采购单标识位
        Boolean flag = true;

        // 采购单id
        Long purchaseId = purchaseDoneVo.getId();

        // 1. 改变采购需求的状态
        // 拿到采购单中的采购需求列表
        List<PurchaseItemDoneVo> purchaseItemDoneVos = purchaseDoneVo.getItems();

        List<PurchaseDetailEntity> purchaseDetailList = new ArrayList<>();

        // 遍历采购需求列表
        for(PurchaseItemDoneVo item : purchaseItemDoneVos){
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if(item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()){
                flag = false;
                purchaseDetailEntity.setStatus(item.getStatus());
            }
            else{
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode());
                // 将成功采购完成的采购项目入库
                PurchaseDetailEntity detailEntity = detailService.getById(item.getItemId());
                wareSkuService.addStock(detailEntity.getSkuId(),detailEntity.getWareId(),detailEntity.getSkuNum());
            }
            purchaseDetailEntity.setId(item.getItemId());
            purchaseDetailList.add(purchaseDetailEntity);
        }
        detailService.updateBatchById(purchaseDetailList);

        // 改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setStatus(flag?WareConstant.PurchaseStatusEnum.FINISHED.getCode():WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}