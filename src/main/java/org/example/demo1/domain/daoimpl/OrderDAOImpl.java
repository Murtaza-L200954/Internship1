package org.example.demo1.domain.daoimpl;

import org.example.demo1.domain.dao.OrdersDAO;
import org.example.demo1.domain.model.Orders;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderDAOImpl implements OrdersDAO {
    private Connection conn;
    private static final Logger log = LoggerFactory.getLogger(OrderDAOImpl.class);

    public OrderDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public List<Orders> getAllOrders() {
        List<Orders> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Orders order = new Orders();
                order.setId(rs.getInt("id"));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setTotal_amount(rs.getFloat("total_amount"));
                order.setStatus(rs.getString("status"));
                orders.add(order);
            }
            log.info("getAllOrders result: {}", orders);
        } catch (SQLException e) {
            log.error("getAllOrders error", e);
        }
        return orders;
    }

    @Override
    public Orders getOrderById(int id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Orders order = new Orders();
                    order.setId(rs.getInt("id"));
                    order.setCustomerId(rs.getInt("customer_id"));
                    order.setTotal_amount(rs.getFloat("total_amount"));
                    order.setStatus(rs.getString("status"));
                    log.info("getOrderById result: {}", order);
                    return order;
                }
            }
        } catch (SQLException e) {
            log.error("getOrderById error", e);
        }
        return null;
    }


    @Override
    public int addOrder(Orders orders) {
        String sql = "INSERT INTO orders (customer_id, total_amount, status) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, orders.getCustomerId());
            stmt.setDouble(2, orders.getTotal_amount());
            stmt.setString(3, orders.getStatus());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    log.info("Order added successfully: orderId={}, customerId={}, totalAmount={}, status={}",
                            orderId, orders.getCustomerId(), orders.getTotal_amount(), orders.getStatus());
                    return orderId;
                }
            } else {
                log.warn("No order was inserted for customerId={}", orders.getCustomerId());
            }
        } catch (SQLException e) {
            log.error("Failed to insert order for customerId={}, totalAmount={}, status={}",
                    orders.getCustomerId(), orders.getTotal_amount(), orders.getStatus(), e);
        }
        return -1;  // signal failure
    }




    @Override
    public boolean updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                log.info("Order status updated: orderId={}, newStatus={}", orderId, newStatus);
                return true;
            } else {
                log.warn("No order found with id={} to update status", orderId);
                return false;
            }
        } catch (SQLException e) {
            log.error("Failed to update status for orderId={}, newStatus={}", orderId, newStatus, e);
            return false;
        }
    }


    @Override
    public boolean deleteOrder(int id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                log.info("Order deleted successfully: orderId={}", id);
                return true;
            } else {
                log.warn("No order found to delete with orderId={}", id);
                return false;
            }
        } catch (SQLException e) {
            log.error("Failed to delete order with orderId={}", id, e);
            return false;
        }
    }


    @Override
    public int getLatestOrderId() {
        String sql = "SELECT MAX(id) AS id FROM orders";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int latestId = rs.getInt("id");
                log.info("Retrieved latest orderId: {}", latestId);
                return latestId;
            } else {
                log.warn("No orders found in the database.");
            }

        } catch (SQLException e) {
            log.error("Failed to retrieve latest orderId", e);
        }
        return -1;
    }

}





