package org.example.demo1.domain.daoimpl;

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
            log.info("Customers found: {}", customers);
        } catch (SQLException e) {
            log.error("Error while fetching customers", e);
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

                    log.info("Customer retrieved successfully: id={}, name={}, email={}",
                            id, customer.getName(), customer.getEmail());
                    return customer;
                } else {
                    log.warn("No customer found with id={}", id);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to retrieve customer with id={}", id, e);
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

                log.info("Customer retrieved successfully by userId={}: name={}, email={}",
                        userId, customer.getName(), customer.getEmail());

                return customer;
            } else{
                log.warn("No customer found with id={}", userId);
            }
        } catch (SQLException e) {
            log.error("Failed to retrieve customer by userId={}", userId, e);
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
                log.info("Customer added successfully: userId={}, name={}, email={}",
                        customer.getUserId(), customer.getName(), customer.getEmail());
                return true;
            } else {
                log.warn("Customer insertion failed for userId={}", customer.getUserId());
                return false;
            }
        } catch (SQLException e) {
            log.error("Failed to add customer: userId={}, name={}, email={}",
                    customer.getUserId(), customer.getName(), customer.getEmail(), e);
            return false;
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
                log.info("Customer updated successfully: id={}, name={}, email={}",
                        customer.getId(), customer.getName(), customer.getEmail());
                return true;
            } else {
                log.warn("No customer found to update with id={}", customer.getId());
                return false;
            }
        } catch (SQLException e) {
            log.error("Failed to update customer: id={}, name={}, email={}",
                    customer.getId(), customer.getName(), customer.getEmail(), e);
            return false;
        }
    }


    @Override
    public boolean deleteCustomer(int id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                log.info("Customer deleted successfully: id={}", id);
                return true;
            } else {
                log.warn("No customer found to delete with id={}", id);
                return false;
            }
        } catch (SQLException e) {
            log.error("Failed to delete customer with id={}", id, e);
            return false;
        }
    }

    @Override
    public boolean deleteCustomerByUserId(int userId) {
        String sql = "DELETE FROM customers WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                log.info("Customer deleted successfully by userId={}", userId);
                return true;
            } else {
                log.warn("No customer found to delete with userId={}", userId);
                return false;
            }
        } catch (SQLException e) {
            log.error("Failed to delete customer by userId={}", userId, e);
            return false;
        }
    }
}
