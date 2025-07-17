package org.example.demo1.domain.daoimpl;

import org.example.demo1.common.LogUtil;
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
            LogUtil.logInfo(log, "getAll orders result " + rs);
        } catch (SQLException e) {
            LogUtil.logError(log,"getAll orders error ", e);
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
                    LogUtil.setMDC(id);
                    LogUtil.logInfo(log, "getOrderById result " + order);
                    return order;
                }
            }
        } catch (SQLException e) {
            LogUtil.logError(log,"getOrderById error ", e);
        } finally {
            LogUtil.clearMDC();
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
                    LogUtil.setMDC(orderId);
                    LogUtil.logInfo(log,
                            String.format("Order added successfully: orderId=%d, customerId=%d, totalAmount=%.2f, status=%s",
                                    orderId, orders.getCustomerId(), orders.getTotal_amount(), orders.getStatus()));
                    return orderId;
                }
            } else {
                LogUtil.logWarn(log, "addOrder failed, no generated key found");
            }
        } catch (SQLException e) {
            LogUtil.logError(log,"addOrder error ", e);
        } finally {
            LogUtil.clearMDC();
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
                LogUtil.setMDC(orderId);
                LogUtil.logInfo(log, String.format(
                        "Order status updated: orderId=%d, newStatus=%s", orderId, newStatus
                ));
                return true;
            } else {
                LogUtil.logWarn(log, String.format(
                        "No order found with id=%d to update status", orderId
                ));
                return false;
            }
        } catch (SQLException e) {
            LogUtil.logError(log,String.format(
                    "Failed to update status for orderId=%d, newStatus=%s", orderId, newStatus
            ), e);
            return false;
        } finally {
            LogUtil.clearMDC();
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





