package com.aining.mall.order.service.impl;

import com.aining.common.exception.NoStockException;
import com.aining.common.to.OrderTo;
import com.aining.common.utils.R;
import com.aining.common.vo.MemberResponseVo;
import com.aining.mall.order.constant.PayConstant;
import com.aining.mall.order.entity.OrderItemEntity;
import com.aining.mall.order.entity.PaymentInfoEntity;
import com.aining.mall.order.enume.OrderStatusEnum;
import com.aining.mall.order.feign.CartFeignService;
import com.aining.mall.order.feign.MemberFeignService;
import com.aining.mall.order.feign.ProductFeignService;
import com.aining.mall.order.feign.WmsFeignService;
import com.aining.mall.order.interceptor.LoginUserInterceptor;
import com.aining.mall.order.service.OrderItemService;
import com.aining.mall.order.service.PaymentInfoService;
import com.aining.mall.order.to.OrderCreateTo;
import com.aining.mall.order.vo.*;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.Query;

import com.aining.mall.order.dao.OrderDao;
import com.aining.mall.order.entity.OrderEntity;
import com.aining.mall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import static com.aining.mall.order.constant.OrderConstant.USER_ORDER_TOKEN_PREFIX;
import static sun.plugin2.os.windows.Windows.CREATE_NEW;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    PaymentInfoService paymentInfoService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        // 从拦截器中获取当前登陆的用户
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        // 构建OrderConfirmVo
        OrderConfirmVo confirmVo = new OrderConfirmVo();

        //TODO :获取当前线程请求头信息(解决Feign异步调用丢失请求头问题)
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        /** 1. 查询会员地址的列表 */
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            //每一个线程都来共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);

            List<MemberAddressVo> addressList = memberFeignService.getAddresses(memberResponseVo.getId());
            confirmVo.setMemberAddressVos(addressList);
        }, threadPoolExecutor);


        /** 2. 获取购物车所有选中的购物项目 */

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            //每一个线程都来共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);

            //feign在远程调用之前要构造请求，调用很多的拦截器
            List<OrderItemVo> currentCartItems = cartFeignService.getCurrentCartItems();
            confirmVo.setItems(currentCartItems);
        }, threadPoolExecutor).thenRunAsync(()->{
            List<OrderItemVo> items = confirmVo.getItems();
            //获取全部商品的id
            List<Long> skuIds = items.stream()
                    .map((itemVo -> itemVo.getSkuId()))
                    .collect(Collectors.toList());

            //远程查询商品库存信息
            R skuHasStock = wmsFeignService.getSkusStock(skuIds);
            List<SkuStockVo> skuStockVos = skuHasStock.getData("data", new TypeReference<List<SkuStockVo>>() {});

            if (skuStockVos != null && skuStockVos.size() > 0) {
                //将skuStockVos集合转换为map
                Map<Long, Boolean> skuHasStockMap = skuStockVos.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(skuHasStockMap);
            }

        },threadPoolExecutor);


        /** 3. 查询用户积分 */
        Integer integration = memberResponseVo.getIntegration();
        confirmVo.setIntegration(integration);

        /** 4. 查询库存 */

        /** 5. 计算总额: 总额在OrderConfirmVo中自动计算 */

        //TODO 5、防重令牌(防止表单重复提交)
        //为用户设置一个token，三十分钟过期时间（存在redis）
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(USER_ORDER_TOKEN_PREFIX+memberResponseVo.getId(),token,30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(addressFuture,cartFuture).get();

        return confirmVo;
    }

    /**
     * 提交订单
     * @param vo
     * @return
     */
    // @Transactional(isolation = Isolation.READ_COMMITTED) 设置事务的隔离级别
    // @Transactional(propagation = Propagation.REQUIRED)   设置事务的传播级别
//    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        confirmVoThreadLocal.set(vo);

        SubmitOrderResponseVo submitOrderResponseVo = new SubmitOrderResponseVo();
        submitOrderResponseVo.setCode(0);

        //获取当前登陆的用户
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        //1、验证令牌是否合法【令牌的对比和删除必须保证原子性】
        // 脚本返回的是0-校验失败，1-校验成功
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        // 前端返回的令牌
        String orderToken = vo.getOrderToken();
        // 前端令牌与redis中的令牌做对比
        Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Arrays.asList(USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()),
                orderToken);

        if(result == 0L){
            //令牌验证失败
            submitOrderResponseVo.setCode(1);
            return submitOrderResponseVo;
        }else{
            // 校验成功
            //去创建、下订单、验令牌、验价格、锁定库存...
            OrderCreateTo orderCreateTo = createOrder();

            // 校验价格
            BigDecimal payAmount = orderCreateTo.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();

            //金额对比
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //3、保存订单
                saveOrder(orderCreateTo);

                // TODO 库存锁定只要有异常，回滚订单数据
                // 需要的数据订单号、所有订单项信息(skuId,skuNum,skuName)
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();

                // 获取订单编号
                String orderSn = orderCreateTo.getOrder().getOrderSn();
                wareSkuLockVo.setOrderSn(orderSn);

                //获取出要锁定的商品数据信息
                List<OrderItemEntity> orderItems = orderCreateTo.getOrderItems();
                List<OrderItemVo> orderItemVos = orderItems.stream().map((orderItemEntity -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(orderItemEntity.getSkuId());
                    orderItemVo.setCount(orderItemEntity.getSkuQuantity());
                    orderItemVo.setTitle(orderItemEntity.getSkuName());
                    return orderItemVo;
                })).collect(Collectors.toList());
                wareSkuLockVo.setLocks(orderItemVos);

                //TODO 调用远程锁定库存的方法
                //出现的问题：扣减库存成功了，但是由于网络原因超时，出现异常，导致订单事务回滚，库存事务不回滚(解决方案：seata)
                // 不推荐使用seata，因为是加锁，串行化，提升不了效率,可以发消息给库存服务
                // 为了保证高并发，库存服务自己回滚，可以发消息给库存服务
                // 库存服务也可以使用自动解锁模式：消息队列
                R r = wmsFeignService.orderLockStock(wareSkuLockVo);
                if(r.getCode() == 0){
                    // 锁库存成功
                    submitOrderResponseVo.setOrder(orderCreateTo.getOrder());

                    // 制造异常，测试事务回滚
                    // int i = 10 / 0;

                    /** 订单创建成功，发送消息给MQ */
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",orderCreateTo.getOrder());
                    return submitOrderResponseVo;
                }else {
                    // 锁库存失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
//                    submitOrderResponseVo.setCode(3);
//                    return submitOrderResponseVo;
                }
            } else {
                submitOrderResponseVo.setCode(2);
                return submitOrderResponseVo;
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity orderEntity = this.baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return orderEntity;
    }

    private OrderCreateTo createOrder() {

        OrderCreateTo createTo = new OrderCreateTo();

        //1、生成订单号
        String orderSn = IdWorker.getTimeId();

        // 1.1 创建和保存订单
        OrderEntity orderEntity = builderOrder(orderSn);
        createTo.setOrder(orderEntity);

        //2、获取到所有的订单项+保存订单项数据
        List<OrderItemEntity> orderItemEntities = builderOrderItems(orderSn);
        createTo.setOrderItems(orderItemEntities);

        //3、验价(计算价格、积分等信息)
        computePrice(orderEntity,orderItemEntities);

        return createTo;
    }

    /**
     * 构建订单数据
     * @param orderSn
     * @return
     */
    private OrderEntity builderOrder(String orderSn) {

        //获取当前用户登录信息
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setMemberId(memberResponseVo.getId());
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberUsername(memberResponseVo.getUsername());

        // 得到前台提交的订单vo
        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();

        //远程获取收货地址和运费信息
        R fareAddressVo = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = fareAddressVo.getData("data", new TypeReference<FareVo>() {});

        //获取到运费信息
        BigDecimal fare = fareResp.getFare();
        orderEntity.setFreightAmount(fare);

        //获取到收货地址信息
        MemberAddressVo memberAddress = fareResp.getAddress();
        //设置收货人信息
        orderEntity.setReceiverName(memberAddress.getName());
        orderEntity.setReceiverPhone(memberAddress.getPhone());
        orderEntity.setReceiverPostCode(memberAddress.getPostCode());
        orderEntity.setReceiverProvince(memberAddress.getProvince());
        orderEntity.setReceiverCity(memberAddress.getCity());
        orderEntity.setReceiverRegion(memberAddress.getRegion());
        orderEntity.setReceiverDetailAddress(memberAddress.getDetailAddress());

        //设置订单相关的状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        // 自动确认时间
        orderEntity.setAutoConfirmDay(7);
        // 未收货
        orderEntity.setConfirmStatus(0);
        return orderEntity;
    }

    public List<OrderItemEntity> builderOrderItems(String orderSn) {

        List<OrderItemEntity> orderItemEntityList = new ArrayList<>();

        //最后确定每个购物项的价格：已经勾选的商品
        List<OrderItemVo> currentCartItems = cartFeignService.getCurrentCartItems();
        if (currentCartItems != null && currentCartItems.size() > 0) {
            // List<OrderItemVo>封装成List<OrderItemEntity>
            orderItemEntityList = currentCartItems.stream().map((orderItemVo) -> {
                //构建订单项数据
                OrderItemEntity orderItemEntity = builderOrderItem(orderItemVo);
                orderItemEntity.setOrderSn(orderSn);

                return orderItemEntity;
            }).collect(Collectors.toList());
        }

        return orderItemEntityList;
    }

    /**
     * 构建某一个订单项的数据
     * @param orderItemVo
     * @return
     */
    private OrderItemEntity builderOrderItem(OrderItemVo orderItemVo) {

        OrderItemEntity orderItemEntity = new OrderItemEntity();

        //1、商品的spu信息
        Long skuId = orderItemVo.getSkuId();
        //获取spu的信息
        R spuInfo = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoData = spuInfo.getData("data", new TypeReference<SpuInfoVo>() {});

        orderItemEntity.setSpuId(spuInfoData.getId());
        orderItemEntity.setSpuName(spuInfoData.getSpuName());
        orderItemEntity.setSpuBrand(spuInfoData.getBrandName());
        orderItemEntity.setCategoryId(spuInfoData.getCatalogId());

        //2、商品的sku信息
        orderItemEntity.setSkuId(skuId);
        orderItemEntity.setSkuName(orderItemVo.getTitle());
        orderItemEntity.setSkuPic(orderItemVo.getImage());
        orderItemEntity.setSkuPrice(orderItemVo.getPrice());
        orderItemEntity.setSkuQuantity(orderItemVo.getCount());

        //使用StringUtils.collectionToDelimitedString将list集合转换为String
        String skuAttrValues = StringUtils.collectionToDelimitedString(orderItemVo.getSkuAttrValues(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttrValues);

        //3、商品的优惠信息

        //4、商品的成长值giftGrowth和赠送积分giftIntegration
        orderItemEntity.setGiftGrowth(orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount())).intValue());
        orderItemEntity.setGiftIntegration(orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount())).intValue());

        //5、订单项的价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);

        //当前订单项的实际金额.总额 - 各种优惠价格
        //原来的价格
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        //原价减去优惠价得到最终的价格
        BigDecimal subtract = origin.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);

        return orderItemEntity;
    }

    /**
     * 验价格：计算价格的方法
     * @param orderEntity
     * @param orderItemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {

        //总价
        BigDecimal total = new BigDecimal("0.0");
        //优惠价
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal intergration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");

        //积分、成长值
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        //订单总额，叠加每一个订单项的总额信息
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            //优惠价格信息
            coupon = coupon.add(orderItemEntity.getCouponAmount());
            promotion = promotion.add(orderItemEntity.getPromotionAmount());
            intergration = intergration.add(orderItemEntity.getIntegrationAmount());

            //总价
            total = total.add(orderItemEntity.getRealAmount());

            //积分信息和成长值信息
            integrationTotal += orderItemEntity.getGiftIntegration();
            growthTotal += orderItemEntity.getGiftGrowth();

        }
        //1、订单价格相关的
        orderEntity.setTotalAmount(total);
        //设置应付总额(总额+运费)
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(intergration);

        //设置积分成长值信息
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //设置删除状态(0-未删除，1-已删除)
        orderEntity.setDeleteStatus(0);

    }

    /**
     * 保存订单所有数据
     * @param orderCreateTo
     */
    private void saveOrder(OrderCreateTo orderCreateTo) {

        //获取订单信息
        OrderEntity order = orderCreateTo.getOrder();
        order.setModifyTime(new Date());
        order.setCreateTime(new Date());
        //保存订单
        this.baseMapper.insert(order);

        //获取订单项信息
        List<OrderItemEntity> orderItems = orderCreateTo.getOrderItems();
        //批量保存订单项数据
        orderItemService.saveBatch(orderItems);
    }


    /**
     * 关闭订单
     * @param orderEntity
     */
    @Override
    public void closeOrder(OrderEntity orderEntity) {
        // 关闭之前，检查订单是否是未付款状态
        OrderEntity orderInfo = this.baseMapper.selectOne(new QueryWrapper<OrderEntity>()
                .eq("order_sn", orderEntity.getOrderSn()));

        if(orderInfo.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())){
            // 待付款状态进行关闭订单操作
            OrderEntity orderUpdate = new OrderEntity();
            orderUpdate.setId(orderInfo.getId());
            orderUpdate.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(orderUpdate);

            // 订单发送消息给MQ，解锁库存
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderInfo, orderTo);

            try {
                /**
                 * 确保每个消息发送成功，给每个消息做好日志记录，(给数据库保存每一个详细信息)保存每个消息的详细信息
                 */
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                /** 定期扫描数据库，重新发送失败的消息*/
            }
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity orderInfo = this.getOrderByOrderSn(orderSn);

        //保留两位小数点，向上取值
        BigDecimal payAmount = orderInfo.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(payAmount.toString());
        payVo.setOut_trade_no(orderInfo.getOrderSn());

        //查询订单项的数据
        List<OrderItemEntity> orderItemInfo = orderItemService.list(
                new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity orderItemEntity = orderItemInfo.get(0);
        payVo.setBody(orderItemEntity.getSkuAttrsVals());

        payVo.setSubject(orderItemEntity.getSkuName());

        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {

        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
                        .eq("member_id",memberResponseVo.getId()).orderByDesc("create_time")
        );

        //遍历所有订单集合
        List<OrderEntity> orderEntityList = page.getRecords().stream().map(order -> {
            //根据订单号查询订单项里的数据
            List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>()
                    .eq("order_sn", order.getOrderSn()));
            order.setOrderItemEntityList(orderItemEntities);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(orderEntityList);

        return new PageUtils(page);
    }

    /**
     * 处理支付宝的支付结果
     * @param asyncVo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String handlePayResult(PayAsyncVo asyncVo) {

        //保存交易流水信息
        PaymentInfoEntity paymentInfo = new PaymentInfoEntity();
        paymentInfo.setOrderSn(asyncVo.getOut_trade_no());
        paymentInfo.setAlipayTradeNo(asyncVo.getTrade_no());
        paymentInfo.setTotalAmount(new BigDecimal(asyncVo.getBuyer_pay_amount()));
        paymentInfo.setSubject(asyncVo.getBody());
        paymentInfo.setPaymentStatus(asyncVo.getTrade_status());
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setCallbackTime(asyncVo.getNotify_time());
        //添加到数据库中
        this.paymentInfoService.save(paymentInfo);

        //修改订单状态
        //获取当前状态
        String tradeStatus = asyncVo.getTrade_status();

        if (tradeStatus.equals("TRADE_SUCCESS") || tradeStatus.equals("TRADE_FINISHED")) {
            //支付成功状态
            String orderSn = asyncVo.getOut_trade_no(); //获取订单号
            this.updateOrderStatus(orderSn,OrderStatusEnum.PAYED.getCode(), PayConstant.ALIPAY);
        }

        return "success";
    }

    /**
     * 修改订单状态
     * @param orderSn
     * @param code
     */
    private void updateOrderStatus(String orderSn, Integer code,Integer payType) {

        this.baseMapper.updateOrderStatus(orderSn,code,payType);
    }


}