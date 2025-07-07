package org.example.demo1.domain.dao;

import org.example.demo1.domain.model.Orders;

import java.util.List;

public interface OrdersDAO {
    List<Orders> getAllOrders();
    Orders getOrderById(int id);
    int addOrder(Orders orders);
    boolean updateOrderStatus(int orderId, String newStatus);
    boolean deleteOrder(int id);
    int getLatestOrderId();
}
