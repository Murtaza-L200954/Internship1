package org.example.demo1.domain.dao;

import org.example.demo1.domain.model.OrderItem;

import java.util.List;

public interface OrderItemDAO {
    boolean addOrderItem(OrderItem item);
    List<OrderItem> getOrderItemsByOrderId(int orderId);
    boolean deleteOrderItemsByOrderId(int orderId);
}
