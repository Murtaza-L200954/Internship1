package org.example.demo1.domain.dao;

import org.example.demo1.domain.model.Customer;

import java.util.List;

public interface CustomerDAO {
    List<Customer> getAllCustomers();
    Customer getCustomerById(int id);
    boolean addCustomer(Customer customer);
    boolean updateCustomer(Customer customer);
    boolean deleteCustomer(int id);
    Customer getCustomerByUserId(int userId);
    boolean deleteCustomerByUserId(int userId);
}
