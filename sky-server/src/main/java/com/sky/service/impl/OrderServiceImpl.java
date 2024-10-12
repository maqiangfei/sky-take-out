package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.MoneyConstant;
import com.sky.constant.RedisConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.PageResult;
import com.sky.service.AddressBookService;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.task.WebSocketTask;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author maqiangfei
 * @since 2024/10/9 下午6:41
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Value("${sky.shop.address}")
    private String shopAddress;
    @Value("${sky.baidu.ak}")
    private String ak;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 获取购物车中的订单明细
        String key = RedisConstant.CART_USER_KEY + BaseContext.getCurrentId();
        String json = stringRedisTemplate.opsForValue().get(key);
        List<ShoppingCart> shoppingCarts = JSON.parseArray(json, ShoppingCart.class);
        if (shoppingCarts == null || shoppingCarts.isEmpty()) {
            // 购物车为空不能下单
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        // 获取收货信息
        AddressBook addressBook = addressBookService.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            // 地址为空不能下单
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        String address = addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail();
        // 校验收货地址是否超出配送距离
        // checkOutOfRange(address);
        // 后端重新计算金额
        BigDecimal amount = new BigDecimal("0");
        for (ShoppingCart shoppingCart : shoppingCarts) {
            amount = amount.add(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())));
        }
        amount = amount.add(new BigDecimal(ordersSubmitDTO.getPackAmount() + MoneyConstant.DELIVERY_AMOUNT));
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setNumber(String.valueOf(System.currentTimeMillis()))
                .setAmount(amount)
                .setAddress(address)
                .setPhone(addressBook.getPhone())
                .setConsignee(addressBook.getConsignee())
                .setStatus(Orders.PENDING_PAYMENT)
                .setPayStatus(Orders.UN_PAID)
                .setUserId(BaseContext.getCurrentId())
                .setOrderTime(LocalDateTime.now());
        // 向订单表插入一条数据
        orderMapper.insert(orders);

        // 向订单明细表中插入多条数据
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);

        // 清空购物车
        stringRedisTemplate.delete(key);

        // 封装VO返回
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // User user = userMapper.getById(BaseContext.getCurrentId());
        //
        // // 调用微信支付接口，生成预支付交易单
        // JSONObject jsonObject = weChatPayUtil.pay(
        //         ordersPaymentDTO.getOrderNumber(),
        //         new BigDecimal("0.01"),
        //         "快了么外卖订单",
        //         user.getOpenid()
        // );
        // if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
        //     throw new OrderBusinessException(MessageConstant.ORDER_PAID_ERROR);
        // }
        //
        // OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        // vo.setPackageStr(jsonObject.getString("package"));
        //
        // return vo;
        Orders orders = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
        if (orders == null || !orders.getStatus().equals(Orders.PENDING_PAYMENT)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        paySuccess(ordersPaymentDTO.getOrderNumber());
        return null;
    }

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    @Override
    public void paySuccess(String outTradeNo) {
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Long orderId = orderMapper.getIdByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(orderId)
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);

        // 通过websocket向客户端浏览器推送消息
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1); // 1表示来单提醒 2表示客户催单
        map.put("orderId", orderId);
        map.put("content", "订单号：" + outTradeNo);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 订单条件分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOS = page.getResult();
        orderVOS.forEach(orderVO -> {
            List<String> dishes = orderVO.getOrderDetailList().stream()
                    .map(orderDetail -> orderDetail.getName() + "*" + orderDetail.getNumber())
                    .collect(Collectors.toList());
            orderVO.setOrderDishes(String.join(";", dishes));
        });
        return new PageResult(page.getTotal(), orderVOS);
    }

    /**
     * 查询订单详细信息
     * @param id
     * @return
     */
    @Override
    public OrderVO getWithDetailById(Long id) {
        return orderMapper.getWithDetailById(id);
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void reject(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        // 订单只有存在且状态为2（待接单）才可以拒单
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus.equals(Orders.PAID)) {
            //用户已支付，需要退款
            // String refund = weChatPayUtil.refund(
            //         ordersDB.getNumber(),
            //         ordersDB.getNumber(),
            //         new BigDecimal(0.01),
            //         new BigDecimal(0.01));
            orders.setPayStatus(Orders.REFUND);
            log.info("拒单，退款申请：{}", ordersRejectionDTO.getId());
        }

        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 取消订单
     *
     * @param ordersCancelDTO
     */
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        Orders orders = new Orders();
        if (payStatus.equals(Orders.PAID)) {
            //用户已支付，需要退款
            // String refund = weChatPayUtil.refund(
            //         ordersDB.getNumber(),
            //         ordersDB.getNumber(),
            //         new BigDecimal(0.01),
            //         new BigDecimal(0.01));
            orders.setPayStatus(Orders.REFUND);
            log.info("订单取消，退款申请：{}", ordersCancelDTO.getId());
        }

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        // 根据状态，分别查询出待接单、待派送、派送中的订单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        // 将查询出的数据封装到orderStatisticsVO中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     * @return
     */
    @Override
    public void repetition(Long id) {
        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // 将购物车对象批量添加到Redis
        orderDetailList.forEach(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());

            shoppingCartService.add(shoppingCart);
        });
    }

    /**
     * 用户取消订单
     *
     * @param id
     */
    public void userCancelById(Long id) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            // weChatPayUtil.refund(
            //         ordersDB.getNumber(), //商户订单号
            //         ordersDB.getNumber(), //商户退款单号
            //         new BigDecimal(0.01),//退款金额，单位 元
            //         new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 用户催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号" + ordersDB.getNumber());
        String json = JSON.toJSONString(map);
        // 通过websocket向客户端浏览器推送消息
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 检查客户的收货地址是否超出配送范围
     * @param address
     */
    private void checkOutOfRange(String address) {
        Map<String, String> map = new HashMap();
        map.put("address", shopAddress);
        map.put("output", "json");
        map.put("ak", ak);

        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address",address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if(distance > 5000){
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }

}
