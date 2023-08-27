package com.aining.mall.ware.service.impl;

import com.aining.common.exception.NoStockException;
import com.aining.common.to.OrderTo;
import com.aining.common.to.mq.StockDetailTo;
import com.aining.common.to.mq.StockLockedTo;
import com.aining.common.utils.R;
import com.aining.mall.ware.entity.WareOrderTaskDetailEntity;
import com.aining.mall.ware.entity.WareOrderTaskEntity;
import com.aining.mall.ware.feign.OrderFeignService;
import com.aining.mall.ware.feign.ProductFeignService;
import com.aining.mall.ware.service.WareOrderTaskDetailService;
import com.aining.mall.ware.service.WareOrderTaskService;
import com.aining.mall.ware.vo.OrderItemVo;
import com.aining.mall.ware.vo.OrderVo;
import com.aining.mall.ware.vo.SkuStockVo;
import com.aining.mall.ware.vo.WareSkuLockVo;
import com.alibaba.fastjson.TypeReference;
import lombok.Data;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.Query;

import com.aining.mall.ware.dao.WareSkuDao;
import com.aining.mall.ware.entity.WareSkuEntity;
import com.aining.mall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@RabbitListener(queues = "stock.release.stock.queue")
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * skuId: 1
         * wareId: 2
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId)){
            queryWrapper.eq("sku_id",skuId);
        }

        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId)){
            queryWrapper.eq("ware_id",wareId);
        }


        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 1. 如果没有该商品的库存，则需要新增，如果有则更新
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(wareSkuEntities == null || wareSkuEntities.size() == 0){
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);

            // 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");

                if(info.getCode() == 0){
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){

            }
            wareSkuDao.insert(skuEntity);
        }else{
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }
    }

    @Override
    public List<SkuStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuStockVo> skuHasStockVos = skuIds.stream().map((skuId) -> {
            SkuStockVo skuHasStockVo = new SkuStockVo();
            // 根据skuId查stock
            Long stock = this.baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(stock == null? false : stock > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return skuHasStockVos;
    }

    /**
     * 为某个订单锁定库存
     *
     * 库存解锁的场景：
     * 1. 订单超时未支付，被系统自动取消
     * 2. 用户手动取消订单
     * 3. 下单成功 + 锁定库存成功，但后续业务调用失败导致订单回滚，之前锁定的库存需要自动解锁(seata解锁太慢)
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean orderLockStock(WareSkuLockVo vo) {
        /**
         * 保存库存工作单详情信息
         * 用于追溯
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskEntity.setCreateTime(new Date());
        wareOrderTaskService.save(wareOrderTaskEntity);


        List<OrderItemVo> orderItemVos = vo.getLocks();
        //1. 找到商品在哪些仓库有库存
        List<SkuWareHasStock> wareHasStocks = orderItemVos.stream().map((orderItemVo) -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            // 设置skuId和数量
            Long skuId = orderItemVo.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setNum(orderItemVo.getCount());

            // 查询有库存的仓库
            List<Long> wareIdList = wareSkuDao.listWareIdHasSkuStock(skuId);
            skuWareHasStock.setWareId(wareIdList);

            return skuWareHasStock;
        }).collect(Collectors.toList());

        // 2. 锁定库存:遍历每一个sku，根据有库存的仓库进行锁库存操作
        for (SkuWareHasStock wareHasStock : wareHasStocks) {
            // 锁库存成功与否的标识位
            boolean skuStocked = false;

            Long skuId = wareHasStock.getSkuId();
            List<Long> wareIds = wareHasStock.getWareId();

            // 判断查询到的仓库是否为空
            if(StringUtils.isEmpty(wareIds)){
                //没有任何仓库有这个商品的库存
                throw new NoStockException(skuId);
            }
            /*
             * 如果该商品有库存：则挨个仓库执行锁库存操作，只要有一个锁成功了则结束
             * 如果每一个商品都锁定成功,将当前商品锁定了几件的工作单记录发给MQ
             *  锁定失败。前面保存的工作单信息都回滚了。发送出去的消息，即使要解锁库存，由于在数据库查不到指定的id，所有就不用解锁
             */
            for (Long wareId : wareIds) {
                //锁定成功就返回1(有1行受到影响)，失败就返回0
                Long count = wareSkuDao.lockSkuStock(skuId,wareId,wareHasStock.getNum());
                if(count == 1){
                    // 说明锁成功
                    skuStocked = true;

                    skuStocked = true;
                    WareOrderTaskDetailEntity taskDetailEntity = WareOrderTaskDetailEntity.builder()
                            .skuId(skuId)
                            .skuName("")
                            .skuNum(wareHasStock.getNum())
                            .taskId(wareOrderTaskEntity.getId())
                            .wareId(wareId)
                            .lockStatus(1)
                            .build();
                    wareOrderTaskDetailService.save(taskDetailEntity);

                    // TODO 告诉MQ库存锁定成功
                    // 如果每一个商品都锁定成功，将当前商品锁定了几件都工作单记录发送给MQ
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetailTo detailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity,detailTo);
                    stockLockedTo.setDetailTo(detailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",stockLockedTo);

                    // 锁成功了之后就退出当前sku的锁库存操作
                    break;

                }else{
                    // 锁库存失败，for循环锁下一个仓库
                }
            }
            if(skuStocked == false){
                //当前商品所有仓库都没有锁住
                throw new NoStockException(skuId);
            }
        }
        //3、肯定全部都是锁定成功的
        return true;
    }

    @Override
    public void unlockStock(StockLockedTo stockLockedTo) {
        //库存工作单的id
        StockDetailTo detail = stockLockedTo.getDetailTo();
        Long detailId = detail.getId();

        /**
         * 解锁
         * 1、查询数据库关于这个订单锁定库存信息
         *   有：证明库存锁定成功了
         *      解锁：订单状况
         *          1、没有这个订单，必须解锁库存
         *          2、有这个订单，不一定解锁库存
         *              订单状态：已取消：解锁库存
         *                      已支付：不能解锁库存
         */
        WareOrderTaskDetailEntity taskDetailInfo = wareOrderTaskDetailService.getById(detailId);
        if (taskDetailInfo != null) {
            //查出wms_ware_order_task工作单的信息
            Long id = stockLockedTo.getId();
            WareOrderTaskEntity orderTaskInfo = wareOrderTaskService.getById(id);
            //获取订单号查询订单状态
            String orderSn = orderTaskInfo.getOrderSn();
            //远程查询订单信息
            R orderData = orderFeignService.getOrderStatus(orderSn);

            if (orderData.getCode() == 0) {
                //订单数据返回成功
                OrderVo orderInfo = orderData.getData("data", new TypeReference<OrderVo>() {});

                // 判断订单状态是否已取消或者支付或者订单不存在(null)
                // 订单状态【0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单】
                if (orderInfo == null || orderInfo.getStatus() == 4) {
                    //订单已被取消，才能解锁库存
                    // 1-锁定,2-解锁，3-已扣减
                    if (taskDetailInfo.getLockStatus() == 1) {
                        //当前库存工作单详情状态1，已锁定，但是未解锁才可以解锁
                        unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum(),detailId);
                    }
                }
            } else {
                //消息拒绝以后重新放在队列里面，让别人继续消费解锁
                //远程调用服务失败
                throw new RuntimeException("远程调用服务失败");
            }
        } else {
            // 没有这个工作单都信息，库存锁定失败
            // 无需解锁
        }
    }

    /**
     * 解锁库存的方法
     * @param skuId
     * @param wareId
     * @param num
     * @param taskDetailId
     */
    public void unLockStock(Long skuId,Long wareId,Integer num,Long taskDetailId) {

        //库存解锁
        wareSkuDao.unLockStock(skuId,wareId,num);

        //更新工作单的状态
        WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
        taskDetailEntity.setId(taskDetailId);
        // 变为已解锁
        // 1-锁定,2-解锁，3-已扣减
        taskDetailEntity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(taskDetailEntity);

    }

    /**
     * 防止订单服务卡顿，导致订单状态消息一直改不了，库存优先到期，查订单状态新建，什么都不处理
     * 导致卡顿的订单，永远都不能解锁库存
     * @param orderTo
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unlockStock(OrderTo orderTo) {

        String orderSn = orderTo.getOrderSn();
        //查一下最新的库存解锁状态，防止重复解锁库存
        WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);

        //按照工作单的id找到所有 没有解锁的库存，进行解锁
        Long id = orderTaskEntity.getId();
        //lock_status: 1-锁定,2-解锁，3- 扣减
        List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", id).eq("lock_status", 1));

        for (WareOrderTaskDetailEntity taskDetailEntity : list) {
            unLockStock(taskDetailEntity.getSkuId(),
                    taskDetailEntity.getWareId(),
                    taskDetailEntity.getSkuNum(),
                    taskDetailEntity.getId());
        }

    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }


}