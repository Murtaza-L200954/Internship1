package org.example.demo1.domain.daoimpl;

import org.example.demo1.common.LogUtil;
import org.example.demo1.domain.dao.UserDAO;
import org.example.demo1.domain.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class UserDAOImpl implements UserDAO {
    private final Connection conn;
    private static final Logger log = LoggerFactory.getLogger(UserDAOImpl.class);

    public UserDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));

                    LogUtil.setMDC(user.getId());
                    LogUtil.logInfo(log,"getUserByUsername Successful for username" + username);
                    return user;
                }
            }
        } catch (SQLException e) {
            LogUtil.logError(log, "Failed to get user by username" + username, e);
        } finally{
            LogUtil.clearMDC();
        }
        return null;
    }

    @Override
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            LogUtil.logError(log, "Failed to get user by userId" + id, e);
        } finally {
            LogUtil.clearMDC();
        }
        return null;
    }


    @Override
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            int rowsInserted = stmt.executeUpdate();

            LogUtil.setMDC(user.getId());
            LogUtil.logInfo(log,"addUser successful for username" + user.getUsername());

            return rowsInserted > 0;
        } catch (SQLException e) {
            LogUtil.logError(log, "Failed to add user " + user.getUsername(), e);
            return false;
        }  finally {
            LogUtil.clearMDC();
        }
    }

    @Override
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, role = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.setInt(4, user.getId());
            int rowsUpdated = stmt.executeUpdate();

            LogUtil.setMDC(user.getId());
            LogUtil.logInfo(log,"updateUser successful for username" + user.getUsername());

            return rowsUpdated > 0;
        } catch (SQLException e) {
            LogUtil.logError(log, "Failed to update user " + user.getUsername(), e);
            return false;
        }   finally {
            LogUtil.clearMDC();
        }
    }

    @Override
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();

            LogUtil.setMDC(id);
            LogUtil.logInfo(log,"deleteUser successful for userId" + id);

            return rowsDeleted > 0;
        } catch (SQLException e) {
            LogUtil.logError(log, "Failed to delete user " + id, e);
            return false;
        }   finally {
            LogUtil.clearMDC();
        }
    }

    @Override
    public boolean userExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LogUtil.logError(log, "Failed to check user exists for username" + username, e);
            return false;
        }
    }
}
