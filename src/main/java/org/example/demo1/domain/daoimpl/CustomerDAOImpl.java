package org.example.demo1.domain.daoimpl;

import org.example.demo1.common.LogUtil;
import org.example.demo1.domain.dao.CustomerDAO;
import org.example.demo1.domain.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerDAOImpl implements CustomerDAO {
    private final Connection conn;
    private static final Logger log = LoggerFactory.getLogger(CustomerDAOImpl.class);

    public CustomerDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Customer customer = new Customer();
                customer.setId(rs.getInt("id"));
                customer.setName(rs.getString("name"));
                customer.setEmail(rs.getString("email"));
                customer.setPhone(rs.getString("phone"));
                customer.setAddress(rs.getString("address"));
                customers.add(customer);
            }
            LogUtil.logInfo(log, String.format("getAllCustomers: %d", customers.size()));
        } catch (SQLException e) {
            LogUtil.logWarn(log, String.format("getAllCustomers: %s", e.getMessage()));
        }
        return customers;
    }

    @Override
    public Customer getCustomerById(int id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer();
                    customer.setId(id);
                    customer.setName(rs.getString("name"));
                    customer.setEmail(rs.getString("email"));
                    customer.setPhone(rs.getString("phone"));
                    customer.setAddress(rs.getString("address"));

                    LogUtil.setMDC(id);
                    LogUtil.logInfo(log, String.format("Customer retrieved successfully: id=%d, name=%s, email=%s",
                            id, customer.getName(), customer.getEmail()));
                    return customer;
                } else {
                    LogUtil.logWarn(log, String.format("No customer found with id=%d", id));
                }
            }
        } catch (SQLException e) {
            LogUtil.logWarn(log, String.format("Failed to retrieve customer with id=%d", id));
        } finally {
            LogUtil.clearMDC();
        }
        return null;
    }

    @Override
    public Customer getCustomerByUserId(int userId) {
        String sql = "SELECT * FROM customers WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Customer customer = new Customer();
                customer.setId(rs.getInt("id"));
                customer.setUserId(rs.getInt("user_id"));
                customer.setName(rs.getString("name"));

                LogUtil.setMDC(userId);
                LogUtil.logInfo(log,String.format(
                        "Customer retrieved successfully by userId=%d: name=%s, email=%s",
                        userId, customer.getName(), customer.getEmail()
                ));
                return customer;
            } else{
                LogUtil.logWarn(log,String.format("No customer found with id=%d", userId));
            }
        } catch (SQLException e) {
            LogUtil.logWarn(log, String.format("Failed to retrieve customer with id=%d", userId));
        }  finally {
            LogUtil.clearMDC();
        }
        return null;
    }


    @Override
    public boolean addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (user_id, name, email, phone, address) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customer.getUserId());
            stmt.setString(2, customer.getName());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhone());
            stmt.setString(5, customer.getAddress());

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                LogUtil.setMDC(customer.getUserId());
                LogUtil.logInfo(log, String.format("Customer added successfully: userId=%d, name=%s, email=%s",
                        customer.getUserId(), customer.getName(), customer.getEmail()));
                return true;
            } else {
                LogUtil.logWarn(log, String.format("Failed to add customer with id=%d", customer.getId()));
                return false;
            }
        } catch (SQLException e) {
            LogUtil.logWarn(log, String.format("Failed to add customer: userId=%d, name=%s, email=%s",
                    customer.getUserId(), customer.getName(), customer.getEmail()));
            return false;
        } finally {
            LogUtil.clearMDC();
        }
    }

    @Override
    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, address = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhone());
            stmt.setString(4, customer.getAddress());
            stmt.setInt(5, customer.getId());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                LogUtil.setMDC(customer.getUserId());
                LogUtil.logInfo(log, String.format("Customer updated successfully: id=%d, name=%s, email=%s",
                        customer.getId(), customer.getName(), customer.getEmail()));
                return true;
            } else {
                LogUtil.logWarn(log, String.format("No customer found to update with id=%d", customer.getId()));
                return false;
            }
        } catch (SQLException e) {
            LogUtil.logWarn(log, String.format("Failed to update customer with id=%d", customer.getId()));
            return false;
        } finally {
            LogUtil.clearMDC();
        }
    }


    @Override
    public boolean deleteCustomer(int id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                LogUtil.setMDC(id);
                LogUtil.logInfo(log, String.format("Customer deleted successfully: id=%d", id));
                return true;
            } else {
                LogUtil.logWarn(log, String.format("Failed to delete customer with id=%d", id));
                return false;
            }
        } catch (SQLException e) {
            LogUtil.logError(log,String.format("Failed to delete customer with id=%d", id),e);
            return false;
        }  finally {
            LogUtil.clearMDC();
        }
    }


    @Override
    public boolean deleteCustomerByUserId(int userId) {
        String sql = "DELETE FROM customers WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                LogUtil.setMDC(userId);
                LogUtil.logInfo(log, String.format("Customer deleted successfully: userId=%d", userId));
                return true;
            } else {
                LogUtil.logWarn(log, String.format("Failed to delete customer with id=%d", userId));
                return false;
            }
        } catch (SQLException e) {
            LogUtil.logError(log,String.format("Failed to delete customer with id=%d", userId),e);
            return false;
        } finally {
            LogUtil.clearMDC();
        }
    }
}
