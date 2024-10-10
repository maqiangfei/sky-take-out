package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.sky.constant.MessageConstant;
import com.sky.constant.MoneyConstant;
import com.sky.constant.RedisConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.service.AddressBookService;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author maqiangfei
 * @since 2024/10/9 下午6:41
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

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
        // 后端重新计算金额
        BigDecimal amount = new BigDecimal("0");
        for (ShoppingCart shoppingCart : shoppingCarts) {
            amount = amount.add(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())));
        }
        amount = amount.add(new BigDecimal(MoneyConstant.PACK_AMOUNT + MoneyConstant.DELIVERY_AMOUNT));
        ordersSubmitDTO.setAmount(amount);

        // 向订单表插入一条数据
        Orders orders = buildOrders(ordersSubmitDTO);
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

    public Orders buildOrders(OrdersSubmitDTO ordersSubmitDTO) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        // 获取收货信息
        AddressBook addressBook = addressBookService.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            // 地址为空不能下单
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        String address = addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail();
        orders.setNumber(String.valueOf(System.currentTimeMillis()))
                .setStatus(Orders.PENDING_PAYMENT)
                .setUserId(BaseContext.getCurrentId())
                .setOrderTime(LocalDateTime.now())
                .setPayStatus(Orders.UN_PAID)
                .setAmount(ordersSubmitDTO.getAmount())
                .setPhone(addressBook.getPhone())
                .setAddress(address)
                .setConsignee(addressBook.getConsignee());
        return orders;
    }
}
