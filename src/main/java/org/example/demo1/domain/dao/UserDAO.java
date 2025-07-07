package org.example.demo1.domain.dao;

import org.example.demo1.domain.model.User;

public interface UserDAO {
    User getUserByUsername(String username);
    User getUserById(int id);
    boolean addUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(int id);
    boolean userExists(String username);
}
