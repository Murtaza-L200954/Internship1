package org.example.demo1.domain.daoimpl;

import org.example.demo1.common.LogUtil;
import org.example.demo1.domain.dao.OrderItemDAO;
import org.example.demo1.domain.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderItemDAOImpl implements OrderItemDAO {

    private final Connection conn;
    private static final Logger log = LoggerFactory.getLogger(OrderItemDAOImpl.class);

    public OrderItemDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public boolean addOrderItem(OrderItem item) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getProductId());
            stmt.setInt(3, item.getQuantity());
            stmt.setDouble(4, item.getUnitPrice());

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                LogUtil.setMDC(item.getOrderId());

                LogUtil.logInfo(
                        log,
                        String.format(
                                "Order item added: orderId=%d, productId=%d, quantity=%d, unitPrice=%.2f",
                                item.getOrderId(),
                                item.getProductId(),
                                item.getQuantity(),
                                item.getUnitPrice()
                        )
                );
                return true;
            } else {
                LogUtil.logWarn(log, String.format("No order item was inserted for orderId=%d", item.getOrderId()));
                return false;
            }
        } catch (SQLException e) {
            LogUtil.logError(log, "Failed to insert order item into database for orderId=" + item.getOrderId(), e);
            return false;
        }
    }

    @Override
    public List<OrderItem> getOrderItemsByOrderId(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id")); // Set after fetch, not before insert
                item.setOrderId(rs.getInt("order_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                items.add(item);
            }
            LogUtil.setMDC(orderId);
            LogUtil.logInfo(log, "getOrderItemsByOrderId: orderId=" + orderId);
        } catch (SQLException e) {
            LogUtil.logError(log, "Failed to getOrderItemsByOrderId", e);
        } finally {
            LogUtil.clearMDC();
        }
        return items;
    }

    @Override
    public boolean deleteOrderItemsByOrderId(int orderId) {
        String sql = "DELETE FROM order_items WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                LogUtil.setMDC(orderId);
                LogUtil.logInfo(log, "deleteOrderItemsByOrderId: orderId=" + orderId);
                return true;
            } else {
                LogUtil.logWarn(log, String.format("No order item was deleted for orderId=%d", orderId));
                return false;
            }
        } catch (SQLException e) {
            LogUtil.logError(log, "Failed to deleteOrderItemsByOrderId", e);
            return false;
        } finally {
            LogUtil.clearMDC();
        }
    }
}
