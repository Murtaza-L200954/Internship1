package org.example.demo1.resources;

import org.example.demo1.common.DBUtil;
import org.example.demo1.common.security.JWTUtil;
import org.example.demo1.domain.dao.OrderItemDAO;
import org.example.demo1.domain.dao.OrdersDAO;
import org.example.demo1.domain.daoimpl.OrderDAOImpl;
import org.example.demo1.domain.daoimpl.OrderItemDAOImpl;
import org.example.demo1.domain.model.OrderItem;
import org.example.demo1.domain.model.Orders;
import org.example.demo1.domain.model.OrderRequest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @GET
    public Response getAllOrders(@HeaderParam("Authorization")  String authHeader) {

        String role = JWTUtil.extractRoleFromHeader(authHeader);

        if (role == null || !(role.equals("admin") || role.equals("staff"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied: Admins and staff only").build();
        }

        try (Connection conn = DBUtil.getConnection()) {
            OrdersDAO dao = new OrderDAOImpl(conn);
            List<Orders> orders = dao.getAllOrders();
            return Response.ok(orders).build();
        } catch (Exception e) {
            return Response.serverError().entity("Failed to fetch orders").build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getOrderById(@PathParam("id") int id, @HeaderParam("Authorization")  String authHeader) {

        String role = JWTUtil.extractRoleFromHeader(authHeader);

        if (role == null || !(role.equals("admin") || role.equals("staff"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied: Admins and staff only").build();
        }

        try (Connection conn = DBUtil.getConnection()) {
            OrdersDAO dao = new OrderDAOImpl(conn);
            Orders order = dao.getOrderById(id);
            return (order != null)
                    ? Response.ok(order).build()
                    : Response.status(Response.Status.NOT_FOUND).entity("Order not found").build();
        } catch (Exception e) {
            return Response.serverError().entity("Failed to fetch order").build();
        }
    }

    @GET
    @Path("/{id}/items")
    public Response getOrderItems(@PathParam("id") int id, @HeaderParam("Authorization")  String authHeader) {

        String role = JWTUtil.extractRoleFromHeader(authHeader);

        if (role == null || !(role.equals("admin") || role.equals("staff"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied: Admins and staff only").build();
        }

        try (Connection conn = DBUtil.getConnection()) {
            OrderItemDAO dao = new OrderItemDAOImpl(conn);
            List<OrderItem> items = dao.getOrderItemsByOrderId(id);
            return Response.ok(items).build();
        } catch (Exception e) {
            return Response.serverError().entity("Failed to fetch order items").build();
        }
    }

    @POST
    public Response placeOrder(OrderRequest request) {
        if (request.getCustomerId() <= 0 ||
                request.getStatus() == null ||
                request.getItems() == null || request.getItems().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid order input").build();
        }

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            OrdersDAO orderDAO = new OrderDAOImpl(conn);
            OrderItemDAO itemDAO = new OrderItemDAOImpl(conn);

            double total = 0.0;

            // stock aur price
            for (OrderItem item : request.getItems()) {
                int productId = item.getProductId();
                int quantity = item.getQuantity();

                String productQuery = "SELECT price, stock FROM products WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(productQuery)) {
                    stmt.setInt(1, productId);
                    ResultSet rs = stmt.executeQuery();

                    if (!rs.next()) {
                        conn.rollback();
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Product with ID " + productId + " not found").build();
                    }

                    double price = rs.getDouble("price");
                    int stock = rs.getInt("stock");

                    if (quantity > stock) {
                        conn.rollback();
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Insufficient stock for product ID " + productId).build();
                    }

                    item.setUnitPrice(price); // Use DB price
                    total += price * quantity;
                }
            }

            // Create Order
            Orders order = new Orders();
            order.setCustomerId(request.getCustomerId());
            order.setStatus(request.getStatus());
            order.setTotal_amount(total);

            int orderId = orderDAO.addOrder(order);
            if (orderId <= 0) {
                conn.rollback();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to create order / Invalid Customer ID").build();
            }

            //int orderId = orderDAO.getLatestOrderId();

            // Insert Order Items
            for (OrderItem item : request.getItems()) {
                item.setOrderId(orderId);
                boolean itemCreated = itemDAO.addOrderItem(item);
                if (!itemCreated) {
                    conn.rollback();
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Failed to add order item").build();
                }
            }

            conn.commit();
            return Response.status(Response.Status.CREATED)
                    .entity("Order placed successfully").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error placing order").build();
        }
    }

    @PUT
    @Path("/{id}/status")
    public Response updateOrderStatus(@PathParam("id") int id, @QueryParam("status") String newStatus, @HeaderParam("Authorization")  String authHeader ) {

        String role = JWTUtil.extractRoleFromHeader(authHeader);

        if (role == null || !(role.equals("admin") || role.equals("staff"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied: Admins and staff only").build();
        }

        List<String> validStatuses = Arrays.asList("Pending", "Processing", "Shipped", "Delivered", "Cancelled");

        if (newStatus == null || newStatus.isEmpty() || !validStatuses.contains(newStatus)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid status value").build();
        }

        try (Connection conn = DBUtil.getConnection()) {
            OrdersDAO dao = new OrderDAOImpl(conn);
            boolean updated = dao.updateOrderStatus(id, newStatus);
            return updated
                    ? Response.ok("Order status updated").build()
                    : Response.status(Response.Status.NOT_FOUND).entity("Order not found").build();
        } catch (Exception e) {
            return Response.serverError().entity("Failed to update status").build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteOrder(@PathParam("id") int id, @HeaderParam("Authorization")  String authHeader) {

        String role = JWTUtil.extractRoleFromHeader(authHeader);

        if (role == null || !(role.equals("admin"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied: Admins only").build();
        }

        try (Connection conn = DBUtil.getConnection()) {
            OrdersDAO dao = new OrderDAOImpl(conn);
            boolean deleted = dao.deleteOrder(id);
            return deleted
                    ? Response.ok("Order deleted").build()
                    : Response.status(Response.Status.NOT_FOUND).entity("Order not found").build();
        } catch (Exception e) {
            return Response.serverError().entity("Failed to delete order").build();
        }
    }
}
